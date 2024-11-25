// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssueQuickFix
import org.jetbrains.kotlin.idea.gradleJava.issues.quickfix.GradleAddDaemonToolchainCriteriaQuickFix
import org.jetbrains.plugins.gradle.util.GradleBundle

class GradleUndefinedDaemonJvmCriteriaBuildIssue(
    cause: Throwable,
    externalProjectPath: String
) : GradleBuildIssue(cause) {
    // TODO: No known exception since right now is an opt-in feature
    override fun isApplicableIssue(cause: Throwable) = false

    override val issueTitle: String = GradleBundle.message("gradle.build.issue.daemon.toolchain.undefined.criteria.title")

    override val issueQuickFixes: Map<BuildIssueQuickFix, String> = mapOf(
        GradleAddDaemonToolchainCriteriaQuickFix(externalProjectPath) to GradleBundle.message("gradle.build.quick.fix.add.toolchain.criteria")
    )
}
