// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues

import com.intellij.build.FilePosition
import com.intellij.build.events.BuildEvent
import com.intellij.build.issue.BuildIssue
import org.gradle.internal.buildconfiguration.DaemonJvmPropertiesConfigurator
import org.jetbrains.kotlin.idea.gradleJava.issues.builders.*
import org.jetbrains.plugins.gradle.issue.GradleIssueChecker
import org.jetbrains.plugins.gradle.issue.GradleIssueData
import org.jetbrains.plugins.gradle.service.execution.GradleExecutionErrorHandler.getRootCauseAndLocation
import java.util.function.Consumer

class GradleDaemonToolchainIssueChecker : GradleIssueChecker {

    override fun check(issueData: GradleIssueData): BuildIssue? {
        val rootCause = getRootCauseAndLocation(issueData.error).first
        val buildIssues = listOf(
            GradleInvalidDaemonJvmCriteriaBuildIssue(rootCause),
            GradleToolchainDownloadedMismatchCriteriaBuildIssue(rootCause, issueData.projectPath),
            GradleToolchainDownloadingErrorBuildIssue(rootCause, issueData.projectPath),
            GradleToolchainInstalledNotFoundMatchingCriteriaBuildIssue(rootCause, issueData.projectPath),
            GradleUndefinedDaemonJvmCriteriaBuildIssue(rootCause, issueData.projectPath),
            GradleUndefinedToolchainRepositoriesBuildIssue(rootCause)
        )

        return buildIssues.firstNotNullOfOrNull { it.build() }
    }

    override fun consumeBuildOutputFailureMessage(
        message: String,
        failureCause: String,
        stacktrace: String?,
        location: FilePosition?,
        parentEventId: Any,
        messageConsumer: Consumer<in BuildEvent>
    ): Boolean {
        return parentEventId == ":${DaemonJvmPropertiesConfigurator.TASK_NAME}"
    }
}
