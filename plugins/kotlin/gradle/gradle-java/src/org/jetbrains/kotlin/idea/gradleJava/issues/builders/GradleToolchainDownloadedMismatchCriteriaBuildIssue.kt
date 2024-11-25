// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleOpenDaemonJvmSettingsQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleRecreateToolchainDownloadUrlsQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

/**
 * Gradle [BuildIssue] handling Daemon JVM criteria exceptions like:
 * "Toolchain provisioned from '<URL>' doesn't satisfy the specification: {languageVersion=11, vendor=any, implementation=vendor-specific}"
 */
class GradleToolchainDownloadedMismatchCriteriaBuildIssue(
    cause: Throwable,
    externalProjectPath: String
) : GradleBuildIssue(cause) {

    override fun isApplicableIssue(cause: Throwable) =
        cause.message?.startsWith("Toolchain provisioned from") ?: false

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.downloaded.mismatch.criteria.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleRecreateToolchainDownloadUrlsQuickFix(externalProjectPath) to GradleBundle.message("gradle.build.quick.fix.recreate.download.urls"),
        GradleOpenDaemonJvmSettingsQuickFix to GradleBundle.message("gradle.build.quick.fix.modify.gradle.jvm.criteria")
    )
}
