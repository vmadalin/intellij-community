// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleOpenDaemonJvmSettingsQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

/**
 * Gradle [BuildIssue] handling Daemon JVM criteria exceptions like:
 * "Value 'invalid version' given for toolchainVersion is an invalid Java version"
 */
class GradleInvalidDaemonJvmCriteriaBuildIssue(cause: Throwable) : GradleBuildIssue(cause) {

    override fun isApplicableIssue(cause: Throwable) =
        cause.toString().endsWith("toolchainVersion is an invalid Java version")

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.invalid.criteria.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleOpenDaemonJvmSettingsQuickFix to GradleBundle.message("gradle.build.quick.fix.modify.gradle.jvm.criteria")
    )
}
