package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJElementFactory;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJVariableName;
import org.jetbrains.annotations.NotNull;

public class ObjJNamedPsiUtil {

    public static boolean hasText(@NotNull ObjJVariableName variableNameElement, @NotNull String variableNameString) {
        return variableNameElement.getText().equals(variableNameString);
    }

    @NotNull
    public static String getName(@NotNull
                                         ObjJVariableName variableName) {
        return variableName.getText();
    }

    @NotNull
    public static PsiElement setName(@NotNull ObjJVariableName variableName, @NotNull String newName) {
        PsiElement newElement = ObjJElementFactory.createVariableName(variableName.getProject(), newName);
        variableName.replace(newElement);
        return newElement;
    }
}
