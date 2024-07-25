// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemProcessingManager
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemResolveProjectTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.findDocument
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.progress.sleepCancellable
import org.gradle.internal.jvm.inspection.JvmVendor
import org.jetbrains.jps.model.java.JdkVersionDetector
import org.jetbrains.kotlin.idea.gradleCodeInsightCommon.GradleBuildScriptSupport
import org.jetbrains.kotlin.idea.gradleCodeInsightCommon.getTopLevelBuildScriptPsiFile
import org.jetbrains.kotlin.idea.gradleCodeInsightCommon.getTopLevelBuildScriptSettingsPsiFile
import org.jetbrains.kotlin.tools.projectWizard.KotlinNewProjectWizardBundle
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder
import org.jetbrains.plugins.gradle.service.GradleInstallationManager
import org.jetbrains.plugins.gradle.service.execution.GradleDaemonJvmHelper
import org.jetbrains.plugins.gradle.service.project.GradleNotification
import org.jetbrains.plugins.gradle.service.project.GradleNotificationIdsHolder
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleBundle
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.awt.Desktop
import java.net.URI

private const val IGNORE_DAEMON_TOOLCHAIN_MIGRATION = "IGNORE_DAEMON_TOOLCHAIN_MIGRATION"

// TODO maybe there's a better way to get the Gradle version used by the sync that this
class GradleNotification2: ExternalSystemTaskNotificationListener { // GradleSyncContributor.onModelFetchCompleted check KotlinDslScriptModelResolver

  override fun onSuccess(id: ExternalSystemTaskId) {
    if (GradleConstants.SYSTEM_ID.id != id.projectSystemId.id || id.type != ExternalSystemTaskType.RESOLVE_PROJECT) return
    //if (PropertiesComponent.getInstance().hasDaemonToolchainMigrationIgnored) return

    val task = ApplicationManager.getApplication().getService(ExternalSystemProcessingManager::class.java).findTask(id)
    if (task !is ExternalSystemResolveProjectTask) return

    val project = id.findProject() ?: return
    val projectSettings = GradleSettings.getInstance(project).getLinkedProjectSettings(task.externalProjectPath) ?: return
    if (!GradleDaemonJvmHelper.isDaemonJvmCriteriaSupported(projectSettings.resolveGradleVersion())) return
    if (GradleDaemonJvmHelper.isProjectUsingDaemonJvmCriteria(projectSettings)) return

    GradleNotification.gradleNotificationGroup.createNotification(
      title = GradleBundle.message("gradle.notifications.daemon.toolchain.migration.title", project.name),
      content = GradleBundle.message("gradle.notifications.daemon.toolchain.migration.description"),
      type = NotificationType.INFORMATION
    ).addAction(
      NotificationAction.create(GradleBundle.message("gradle.notifications.daemon.toolchain.migration.info.action.text")) { _, _ ->
        if (Desktop.isDesktopSupported()) {
          val url = GradleBundle.message("gradle.notifications.daemon.toolchain.migration.info.url")
          Desktop.getDesktop().browse(URI(url))
        }
      }).addAction(NotificationAction.create(
      GradleBundle.message("gradle.notifications.daemon.toolchain.migration.accept.action.text")) { _, notification ->
      PropertiesComponent.getInstance().hasDaemonToolchainMigrationIgnored = true
      notification.expire()
      // TODO Start migration
      startMigration(project, task.externalProjectPath)
    }).addAction(NotificationAction.create(
      GradleBundle.message("gradle.notifications.daemon.toolchain.migration.cancel.action.text")) { _, notification ->
      PropertiesComponent.getInstance().hasDaemonToolchainMigrationIgnored = true
      notification.expire()
    }).setDisplayId(GradleNotificationIdsHolder.daemonToolchainMigration)
      .notify(project)
  }

  private fun startMigration(project: Project, externalProjectPath: String) {
    // Check applied plugins
    // - Apply foojay plugin
    // Get current Gradle JDK configuration
    // Resolve path to obtain version and vendor
    // Invoke updateDaemonJvm task using the values

    //GradleBuildScriptBuilderUtil
    //GradleBuildScriptSupport.getManipulator(file).configureBuildScripts(
    //  - Expose add Foojay plugin
    //
    //    val file = module.getBuildScriptPsiFile()
    //

      ApplicationManager.getApplication().invokeLater {
          CommandProcessor.getInstance().executeCommand(project, {
              ApplicationManager.getApplication().runWriteAction {
                  val topLevelBuildScript = project.getTopLevelBuildScriptSettingsPsiFile()!!
                  val buildScriptSupport = GradleBuildScriptSupport.getManipulator(topLevelBuildScript)
                  buildScriptSupport.addFoojayPlugin(topLevelBuildScript)

                  GradleInstallationManager.getInstance().getGradleJvmPath(project, externalProjectPath)?.let {
                      //ExternalSystemJdkUtil.getJavaSdkType().getVersionString()
                      val test = JdkVersionDetector.getInstance().detectJdkVersionInfo(
                          GradleInstallationManager.getInstance().getGradleJvmPath(project, externalProjectPath)!!)
                      GradleDaemonJvmHelper.updateProjectDaemonJvmCriteria(
                          project,
                          externalProjectPath,
                          version = test?.version?.feature?.toString(),
                          vendor = "AZUL" //JvmVendor.KnownJvmVendor.parse(test?.variant?.displayName!!).name
                      )
                  }

                  // TODO remove gradelJVM
              }
          }, "Test migration", null)
      }
      //}

  }

  private var PropertiesComponent.hasDaemonToolchainMigrationIgnored: Boolean
    get() = getBoolean(IGNORE_DAEMON_TOOLCHAIN_MIGRATION, false)
    set(value) = setValue(IGNORE_DAEMON_TOOLCHAIN_MIGRATION, value)
}