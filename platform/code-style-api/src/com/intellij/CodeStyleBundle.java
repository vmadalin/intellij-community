// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij;

import org.jetbrains.annotations.*;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class CodeStyleBundle {
  public static final @NonNls String BUNDLE = "messages.CodeStyleBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(CodeStyleBundle.class, BUNDLE);

  private CodeStyleBundle() {
  }

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
