// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleDownloadToolchainQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleOpenDaemonJvmSettingsQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

/**
 * Gradle [BuildIssue] handling Daemon JVM criteria exceptions like:
 * "Unable to download toolchain matching the requirements ({languageVersion=1.1, vendor=any vendor, implementation=vendor-specific})
 * from '<URL>', due to: No defined toolchain download url for MAC_OS on aarch64 architecture."
 */
class GradleToolchainDownloadingErrorBuildIssue(
    cause: Throwable,
    externalProjectPath: String
) : GradleBuildIssue(cause) {

    override fun isApplicableIssue(cause: Throwable) =
        cause.message?.startsWith("Unable to download toolchain matching the requirements") ?: false

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.download.error.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleDownloadToolchainQuickFix(externalProjectPath) to GradleBundle.message("gradle.build.quick.fix.install.missing.toolchain"),
        GradleOpenDaemonJvmSettingsQuickFix to GradleBundle.message("gradle.build.quick.fix.modify.gradle.jvm.criteria")
    )
}
