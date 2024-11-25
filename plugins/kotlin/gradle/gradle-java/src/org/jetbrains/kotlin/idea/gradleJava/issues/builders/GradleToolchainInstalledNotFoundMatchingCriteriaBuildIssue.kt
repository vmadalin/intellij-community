// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleDownloadToolchainQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleOpenDaemonJvmSettingsQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

/**
 * Gradle [BuildIssue] handling Daemon JVM criteria exceptions like:
 * "Cannot find a Java installation on your machine (Mac OS X 14.7.1 aarch64) matching: Compatible with Java 20,
 * vendor matching('dasdsada') (from gradle/gradle-daemon-jvm.properties)."
 */
class GradleToolchainInstalledNotFoundMatchingCriteriaBuildIssue(
    cause: Throwable,
    externalProjectPath: String
) : GradleBuildIssue(cause) {

    override fun isApplicableIssue(cause: Throwable) =
        cause.message?.startsWith("Cannot find a Java installation on your machine") ?: false

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.not.found.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleDownloadToolchainQuickFix(externalProjectPath) to GradleBundle.message("gradle.build.quick.fix.install.missing.toolchain"),
        GradleOpenDaemonJvmSettingsQuickFix to GradleBundle.message("gradle.build.quick.fix.modify.gradle.jvm.criteria")
    )
}
