// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.gradleJava.issues.builders

import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable

abstract class GradleBuildIssue(private val cause: Throwable) {

    fun build(): BuildIssue? {
        if (!isApplicableIssue(cause)) return null

        return object : BuildIssue {
            override val title = issueTitle
            override val description = createIssueDescription()
            override val quickFixes = issueQuickFixes.keys.toList()
            override fun getNavigatable(project: Project): Navigatable? = null
        }
    }

    protected abstract fun isApplicableIssue(cause: Throwable): Boolean
    protected abstract val issueTitle: String
    protected abstract val issueQuickFixes: Map<BuildIssueQuickFix, String>

    private fun createIssueDescription(): String {
        val quickFixes = issueQuickFixes.map { (quickFix, quickFixText) ->
            "<a href=\"${quickFix.id}\">$quickFixText</a>"
        }.joinToString("<br>")
        return """
            ${cause.message}

            $quickFixes
        """.trimIndent()
    }
}