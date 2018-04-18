package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJVariablePsiUtil {

    private static final List<ObjJVariableName> EMPTY_LIST = Collections.emptyList();

    @NotNull
    public static String toString(ObjJVariableName variableName) {
        return "ObjJ_VAR_NAME("+variableName.getText()+")";
    }

    @Nullable
    public static ObjJVariableName getInstanceVarDeclarationFromDeclarations(@NotNull
                                                                                List<ObjJInstanceVariableDeclaration> instanceVariableDeclarations, @NotNull String variableName) {
        if (!instanceVariableDeclarations.isEmpty()) {
            for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : instanceVariableDeclarations) {
                String instanceVariableVariableName = instanceVariableDeclaration.getVariableName().getText();
                if (instanceVariableVariableName.equals(variableName)) {
                    return instanceVariableDeclaration.getVariableName();
                }
            }
        }
        return null;
    }

    @NotNull
    public static ObjJInstanceVariableDeclaration setName(@NotNull ObjJInstanceVariableDeclaration instanceVariableDeclaration, @NotNull String newName) {
        ObjJVariableName oldVariableName = instanceVariableDeclaration.getVariableName();
        ObjJVariableName newVariableName = ObjJElementFactory.createVariableName(instanceVariableDeclaration.getProject(), newName);
        Logger.getInstance(ObjJVariablePsiUtil.class).assertTrue(newVariableName != null);
        if (oldVariableName != null) {
            instanceVariableDeclaration.getNode().replaceChild(oldVariableName.getNode(), newVariableName.getNode());
        //Old var name does not exist. Insert from scratch
        } else {
            //Get next psi elemet
            PsiElement after = instanceVariableDeclaration.getFormalVariableType().getNextSibling();
            //If next element is not a space, add one
            if (after == null || after.getNode().getElementType() != com.intellij.psi.TokenType.WHITE_SPACE) {
                after = ObjJElementFactory.createSpace(instanceVariableDeclaration.getProject());
                instanceVariableDeclaration.addAfter(instanceVariableDeclaration.getFormalVariableType(), after);
            }
            //If there is an @accessor statement, add space before
            if (instanceVariableDeclaration.getAtAccessors() != null) {
                instanceVariableDeclaration.addBefore(instanceVariableDeclaration.getAtAccessors(),ObjJElementFactory.createSpace(instanceVariableDeclaration.getProject()));
            }
            //Actaully add the variable name element
            instanceVariableDeclaration.addAfter(newVariableName, after);
        }
        return instanceVariableDeclaration;
    }

    /**
     * Gets the last variableName element in a fully qualified name.
     * @param qualifiedReference qualified variable name
     * @return last var name element.
     */
    @Nullable
    public static ObjJVariableName getLastVar(ObjJQualifiedReference qualifiedReference) {
        final List<ObjJVariableName> variableNames = qualifiedReference.getVariableNameList();
        final int lastIndex = variableNames.size() - 1;
        return !(variableNames.isEmpty()) ? variableNames.get(lastIndex) : null;
    }

    @NotNull
    public static List<String> getFileVariableNames(PsiFile file) {
        List<String> out = new ArrayList<>();
        for (ObjJBodyVariableAssignment bodyVariableAssignment : ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJBodyVariableAssignment.class)) {
            for (ObjJVariableDeclaration declaration : bodyVariableAssignment.getVariableDeclarationList()) {
                for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                    out.add(qualifiedReference.getPartsAsString());
                }
            }
        }
        return out;
    }

    public static boolean isNewVarDec(@NotNull PsiElement psiElement) {
        ObjJQualifiedReference reference = ObjJTreeUtil.getParentOfType(psiElement, ObjJQualifiedReference.class);
        if (reference == null)
            return false;
        if (!(reference.getParent() instanceof ObjJVariableDeclaration)  && !(reference.getParent() instanceof ObjJBodyVariableAssignment)){
            return false;
        }
        ObjJBodyVariableAssignment bodyVariableAssignment = reference.getParentOfType(ObjJBodyVariableAssignment.class);
        return bodyVariableAssignment != null && bodyVariableAssignment.getVarModifier() != null;
    }

    @Nullable
    public static String getFileName(@NotNull ObjJGlobalVariableDeclaration declaration) {
        if (declaration.getStub() != null) {
            ObjJGlobalVariableDeclarationStub stub = declaration.getStub();
            if (stub.getFileName() != null && !stub.getFileName().isEmpty()) {
                return stub.getFileName();
            }
        }
        return ObjJFileUtil.getContainingFileName(declaration);
    }

    @NotNull
    public static String getVariableNameString(@NotNull ObjJGlobalVariableDeclaration declaration) {
        if (declaration.getStub() != null) {
            ObjJGlobalVariableDeclarationStub stub = declaration.getStub();
            if (!stub.getVariableName().isEmpty()) {
                return stub.getVariableName();
            }
        }
        return declaration.getVariableName().getText();
    }

    @Nullable
    public static String getVariableType(@NotNull ObjJGlobalVariableDeclaration declaration) {
        if (declaration.getStub() != null) {
            ObjJGlobalVariableDeclarationStub stub = declaration.getStub();
            if (stub.getVariableType() != null && !stub.getVariableType().isEmpty()) {
                return stub.getVariableType();
            }
        }
        return null;
    }


}
