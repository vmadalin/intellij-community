// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

/*
 * Class StaticDescriptorImpl
 * @author Jeka
 */
package com.intellij.debugger.ui.impl.watch;

import com.intellij.debugger.JavaDebuggerBundle;
import com.intellij.debugger.engine.StackFrameContext;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluateExceptionUtil;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.impl.DebuggerUtilsImpl;
import com.intellij.debugger.ui.tree.UserExpressionDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.sun.jdi.Type;
import org.jetbrains.annotations.Nullable;

public class UserExpressionDescriptorImpl extends EvaluationDescriptor implements UserExpressionDescriptor {
  private final ValueDescriptorImpl myParentDescriptor;
  private final String myTypeName;
  private final String myName;
  private final int myEnumerationIndex;

  public UserExpressionDescriptorImpl(Project project,
                                      ValueDescriptorImpl parent,
                                      String typeName,
                                      String name,
                                      TextWithImports text,
                                      int enumerationIndex) {
    super(text, project);
    myParentDescriptor = parent;
    myTypeName = typeName;
    myName = name;
    myEnumerationIndex = enumerationIndex;
  }

  @Override
  public String getName() {
    return StringUtil.isEmpty(myName) ? myText.getText() : myName;
  }

  @Override
  public @Nullable String getDeclaredType() {
    Type type = getType();
    return type != null ? type.name() : null;
  }

  @Override
  protected PsiCodeFragment getEvaluationCode(final StackFrameContext context) throws EvaluateException {
    Pair<PsiElement, PsiType> psiClassAndType = DebuggerUtilsImpl.getPsiClassAndType(myTypeName, myProject);
    if (psiClassAndType.first == null) {
      throw EvaluateExceptionUtil.createEvaluateException(JavaDebuggerBundle.message("evaluation.error.invalid.type.name", myTypeName));
    }
    PsiCodeFragment fragment = createCodeFragment(psiClassAndType.first);
    if (fragment instanceof JavaCodeFragment) {
      ((JavaCodeFragment)fragment).setThisType(psiClassAndType.second);
    }
    return fragment;
  }

  public ValueDescriptorImpl getParentDescriptor() {
    return myParentDescriptor;
  }

  @Override
  protected EvaluationContextImpl getEvaluationContext(final EvaluationContextImpl evaluationContext) {
    return evaluationContext.createEvaluationContext(myParentDescriptor.getValue());
  }

  public int getEnumerationIndex() {
    return myEnumerationIndex;
  }
}