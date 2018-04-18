package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ObjJHasFunctionName {
    @NotNull
    String getFunctionNameAsString();

    @Nullable
    ObjJNamedElement getFunctionNameNode();
}
