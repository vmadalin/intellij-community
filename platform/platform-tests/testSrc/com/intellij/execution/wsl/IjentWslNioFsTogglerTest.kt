// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.execution.wsl

import com.intellij.execution.wsl.ijent.nio.toggle.IjentWslNioFsToggler
import com.intellij.testFramework.junit5.TestApplication
import io.kotest.assertions.withClue
import io.kotest.matchers.be
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import org.junit.jupiter.api.Test

@TestApplication
class IjentWslNioFsTogglerTest {
  private infix fun Collection<Pair<String, String?>>.shouldMatch(other: Collection<String>) {
    withClue("Improving test readability: lists should be sorted") {
      other should be(other.sorted())
    }

    sortedBy { it.first }.joinToString("\n") { (k, v) -> "$k$v" } should be(other.joinToString("\n"))
  }

  @Test
  fun `enable when no options set`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = true, forceProductionOptions = true, vmOptionsReader(""))
    changedOptions shouldMatch listOf(
      "-Didea.io.use.nio2=true",
      "-Djava.nio.file.spi.DefaultFileSystemProvider=com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvider",
      "-Djava.security.manager=com.intellij.platform.core.nio.fs.CoreBootstrapSecurityManager",
    )
  }

  @Test
  fun `enable when no options set in unit test mode`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = true, forceProductionOptions = false, vmOptionsReader(""))
    changedOptions shouldMatch listOf(
      "-Didea.io.use.nio2=true",
      "-Djava.nio.file.spi.DefaultFileSystemProvider=com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvider",
      "-Djava.security.manager=com.intellij.platform.core.nio.fs.CoreBootstrapSecurityManager",
      "-Xbootclasspath/a:out/tests/classes/production/intellij.platform.core.nio.fs",
    )
  }

  @Test
  fun `disable when no options set`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = false, forceProductionOptions = true, vmOptionsReader(""))
    changedOptions should beEmpty()
  }

  @Test
  fun `enable when disabling options set`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = true, forceProductionOptions = true, vmOptionsReader("""
      -Didea.force.default.filesystem=true
      -Didea.io.use.nio2=false
      -Djava.nio.file.spi.DefaultFileSystemProvider=com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvider
      -Djava.security.manager=com.intellij.platform.core.nio.fs.CoreBootstrapSecurityManager
    """.trimIndent()))

    changedOptions shouldMatch listOf(
      "-Didea.force.default.filesystem=false",
      "-Didea.io.use.nio2=true",
    )
  }

  @Test
  fun `disable when enabling options set`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = false, forceProductionOptions = true, vmOptionsReader("""
      -Didea.io.use.nio2=true
      -Djava.nio.file.spi.DefaultFileSystemProvider=com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvider
      -Djava.security.manager=com.intellij.platform.core.nio.fs.CoreBootstrapSecurityManager
    """.trimIndent()))

    changedOptions shouldMatch listOf(
      "-Didea.force.default.filesystem=true",
      "-Didea.io.use.nio2=false",
    )
  }

  @Test
  fun `disable when enabling options set and forcing unset`() {
    val changedOptions = IjentWslNioFsToggler.ensureInVmOptionsImpl(isEnabled = false, forceProductionOptions = true, vmOptionsReader("""
        -Didea.force.default.filesystem=false
        -Didea.io.use.nio2=true
        -Djava.nio.file.spi.DefaultFileSystemProvider=com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvider
        -Djava.security.manager=com.intellij.platform.core.nio.fs.CoreBootstrapSecurityManager
      """.trimIndent()))

    changedOptions shouldMatch listOf(
      "-Didea.force.default.filesystem=true",
      "-Didea.io.use.nio2=false",
    )
  }

  private fun vmOptionsReader(data: String): (String) -> String? {
    val lines = data.lines()
    withClue("Improving test readability: vm options should be sorted") {
      lines should be(lines.sorted())
    }
    return { prefix ->
      lines.map(String::trim).firstNotNullOfOrNull { line ->
        if (line.startsWith(prefix))
          line.substring(prefix.length)
        else
          null
      }
    }
  }
}