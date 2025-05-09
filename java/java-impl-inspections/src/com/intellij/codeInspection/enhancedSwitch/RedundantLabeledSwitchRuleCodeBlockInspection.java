// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInspection.enhancedSwitch;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.JavaFeature;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.psiutils.CommentTracker;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.intellij.java.JavaBundle.message;

public final class RedundantLabeledSwitchRuleCodeBlockInspection extends AbstractBaseJavaLocalInspectionTool {
  @Override
  public @NotNull Set<@NotNull JavaFeature> requiredFeatures() {
    return Set.of(JavaFeature.ENHANCED_SWITCH);
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitSwitchLabeledRuleStatement(@NotNull PsiSwitchLabeledRuleStatement statement) {
        super.visitSwitchLabeledRuleStatement(statement);

        PsiStatement body = statement.getBody();
        if (body instanceof PsiBlockStatement) {
          PsiCodeBlock codeBlock = ((PsiBlockStatement)body).getCodeBlock();
          PsiStatement bodyStatement = getSingleStatement(codeBlock);

          if (bodyStatement instanceof PsiYieldStatement yieldStatement) {
            if (yieldStatement.getExpression() != null &&
                PsiTreeUtil.getParentOfType(yieldStatement, PsiSwitchBlock.class) instanceof PsiSwitchExpression) {
              registerProblem(bodyStatement.getFirstChild());
            }
          }
          else if (bodyStatement instanceof PsiThrowStatement || bodyStatement instanceof PsiExpressionStatement) {
            registerProblem(codeBlock.getLBrace());
            if (isOnTheFly) registerProblem(codeBlock.getRBrace());
          }
        }
      }

      private void registerProblem(@Nullable PsiElement element) {
        if (element != null) {
          holder.registerProblem(element,
                                 message("inspection.labeled.switch.rule.redundant.code.block.message"),
                                 new UnwrapCodeBlockFix());
        }
      }
    };
  }

  private static @Nullable PsiStatement getSingleStatement(@NotNull PsiCodeBlock block) {
    PsiStatement firstStatement = PsiTreeUtil.getNextSiblingOfType(block.getLBrace(), PsiStatement.class);
    if (firstStatement != null && PsiTreeUtil.getNextSiblingOfType(firstStatement, PsiStatement.class) == null) {
      return firstStatement;
    }
    return null;
  }

  private static class UnwrapCodeBlockFix extends PsiUpdateModCommandQuickFix {
    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
      return message("inspection.labeled.switch.rule.redundant.code.fix.name");
    }

    @Override
    protected void applyFix(@NotNull Project project, @NotNull PsiElement element, @NotNull ModPsiUpdater updater) {
      PsiBlockStatement body = PsiTreeUtil.getParentOfType(element, PsiBlockStatement.class);
      if (body != null && body.getParent() instanceof PsiSwitchLabeledRuleStatement) {
        PsiStatement bodyStatement = getSingleStatement(body.getCodeBlock());
        if (bodyStatement instanceof PsiYieldStatement) {
          unwrapYieldValue(body, (PsiYieldStatement)bodyStatement);
        }
        else if (bodyStatement instanceof PsiThrowStatement || bodyStatement instanceof PsiExpressionStatement) {
          unwrap(body, bodyStatement);
        }
      }
    }

    private static void unwrapYieldValue(PsiStatement body, PsiYieldStatement yieldStatement) {
      PsiExpression valueExpression = yieldStatement.getExpression();
      if (valueExpression != null) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(body.getProject());
        PsiExpressionStatement statement = (PsiExpressionStatement)factory.createStatementFromText("x=1;", body);
        statement.getExpression().replace(valueExpression);

        CommentTracker tracker = new CommentTracker();
        // replaceAndRestoreComments() will work with a copy of the expression, so it won't see the original comments
        tracker.markUnchanged(valueExpression);
        tracker.replaceAndRestoreComments(body, statement);
      }
    }

    private static void unwrap(PsiStatement body, PsiStatement bodyStatement) {
      CommentTracker tracker = new CommentTracker();
      tracker.replaceAndRestoreComments(body, bodyStatement);
    }
  }
}