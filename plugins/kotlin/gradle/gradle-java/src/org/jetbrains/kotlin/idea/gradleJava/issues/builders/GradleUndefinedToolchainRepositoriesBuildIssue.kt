// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleAddDownloadToolchainRepositoryQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

/**
 * Gradle [BuildIssue] handling Daemon JVM criteria exceptions like:
 * "Cannot find a Java installation on your machine (Mac OS X 14.7.1 aarch64) matching: {languageVersion=99, vendor=any vendor,
 * implementation=vendor-specific}. Toolchain download repositories have not been configured."
 */
class GradleUndefinedToolchainRepositoriesBuildIssue(cause: Throwable) : GradleBuildIssue(cause) {

    override fun isApplicableIssue(cause: Throwable) =
        cause.message?.contains("Toolchain download repositories have not been configured.") ?: false

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.repositories.undefined.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleAddDownloadToolchainRepositoryQuickFix to GradleBundle.message("gradle.build.quick.fix.add.toolchain.repository")
    )
}
