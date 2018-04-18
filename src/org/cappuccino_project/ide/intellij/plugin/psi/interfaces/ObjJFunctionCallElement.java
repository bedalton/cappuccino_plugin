package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJFunctionCallElement extends ObjJCompositeElement {

    @NotNull
    List<ObjJExpr> getExprList();

    @Nullable
    PsiElement getCloseParen();

    @NotNull
    PsiElement getOpenParen();
}
