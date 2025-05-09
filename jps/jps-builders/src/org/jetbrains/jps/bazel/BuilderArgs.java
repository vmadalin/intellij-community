// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.bazel;

import java.util.List;

public interface BuilderArgs {
  List<String> getJavaCompilerArgs();
  
  List<String> getKotlinCompilerArgs();
}
