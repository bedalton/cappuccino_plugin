package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeReference;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeResults;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJCallTargetUtil {

    private static final List<String> UNDETERMINED = Collections.singletonList(ObjJClassType.UNDETERMINED);

    @NotNull
    public static List<String> getPossibleCallTargetTypes(@Nullable ObjJCallTarget callTarget) {
        if (callTarget == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> classNames = getPossibleCallTargetTypesFromFormalVariableTypes(callTarget);
        if (classNames != null && !classNames.isEmpty()) {
            return classNames;
        }
        Project project = callTarget.getProject();
        String containingClass = ObjJPsiImplUtil.getContainingClassName(callTarget);
        switch(callTarget.getText()) {
            case "super":
                containingClass = ObjJHasContainingClassPsiUtil.getContainingSuperClassName(callTarget);
            case "this":
            case "self":
                if (containingClass != null && !DumbService.isDumb(project)) {
                    return ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project);
                }
        }

        if (!DumbService.isDumb(callTarget.getProject()) && !ObjJImplementationDeclarationsIndex.getInstance().getKeysByPattern(callTarget.getText(), project).isEmpty()) {
             return ObjJInheritanceUtil.getAllInheritedClasses(callTarget.getText(), project);
        }
        List<String> out = new ArrayList<>();
        List<String> varNameResults = getCallTargetTypeFromVarName(callTarget);
        if (varNameResults != null) {
            out.addAll(varNameResults);
        }
        return out;
    }

    @Nullable
    public static List<String> getCallTargetTypeFromVarName(ObjJCallTarget callTarget) {
        ExpressionReturnTypeResults results = new ExpressionReturnTypeResults(callTarget.getProject());
        if (callTarget.getQualifiedReference() != null) {
            if (callTarget.getQualifiedReference().getVariableNameList().size() == 1) {
                for (ObjJVariableName variableName : ObjJVariableNameUtil.getMatchingPrecedingVariableNameElements(callTarget.getQualifiedReference().getVariableNameList().get(0), callTarget.getQualifiedReference().getVariableNameList().size() - 1)) {
                    ObjJVariableDeclaration declaration = variableName.getParentOfType(ObjJVariableDeclaration.class);
                    if (declaration == null) {
                        continue;
                    }
                    ExpressionReturnTypeResults currentResults = ObjJExpressionReturnTypeUtil.getReturnTypes(declaration.getExpr());
                    if (currentResults == null || currentResults.getReferences().isEmpty()) {
                        continue;
                    }
                    results.tick(currentResults);
                }
            }
            List<String> out = new ArrayList<>();
            for (ExpressionReturnTypeReference reference : results.getReferences()) {
                if (!out.contains(reference.getType())) {
                    out.add(reference.getType());
                }
            }
            return out;
        }
        return null;

    }

    @Nullable
    public static List<String> getPossibleCallTargetTypesFromFormalVariableTypes(@NotNull  ObjJCallTarget callTarget) {
        if (callTarget.getQualifiedReference() == null || callTarget.getQualifiedReference().getVariableNameList().isEmpty()) {
            return null;
        }
        ObjJVariableName callTargetVariableName = callTarget.getQualifiedReference().getVariableNameList().get(0);
        PsiElement resolvedVariableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(callTargetVariableName, true);
        if (resolvedVariableName == null) {
            //Logger.getAnonymousLogger().log(Level.INFO, "Failed to find formal variable type for target with value: <"+callTargetVariableName.getText()+">");
            return null;
        }
        ObjJFormalVariableType formalVariableType = null;
        ObjJMethodDeclarationSelector containingSelector = ObjJTreeUtil.getParentOfType(resolvedVariableName, ObjJMethodDeclarationSelector.class);
        if (containingSelector != null) {
            formalVariableType = containingSelector.getFormalVariableType();
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(resolvedVariableName, ObjJInstanceVariableDeclaration.class);
        if (instanceVariableDeclaration != null) {
            formalVariableType = instanceVariableDeclaration.getFormalVariableType();
            //Logger.getAnonymousLogger().log(Level.INFO, "Call Target <"+callTargetVariableName.getText()+"> is an instance variable with type: <"+formalVariableType+">");
        }
        if (formalVariableType == null) {
            return null;
        }
        if (ObjJClassType.isPrimitive(formalVariableType.getText())) {
            return Collections.singletonList(formalVariableType.getText());
        }
        if (formalVariableType.getVarTypeId() != null && formalVariableType.getVarTypeId().getClassName() != null) {
            return ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.getVarTypeId().getClassName().getText(), callTarget.getProject());
        } else {
            return ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.getText(), callTarget.getProject());
        }
    }

    public static List<String> getPossibleCallTargetTypesFromMethodCall(@NotNull ObjJMethodCall methodCall) {

        List<String> classConstraints;
        ObjJCallTarget callTarget = methodCall.getCallTarget();
        String callTargetText = getCallTargetTypeIfAllocStatement(callTarget);
        switch (callTargetText) {
            case "self":
                classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(methodCall.getContainingClassName(), methodCall.getProject());
                break;
            case "super":
                String containingClass = methodCall.getContainingClassName();
                classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(containingClass, methodCall.getProject());
                if (classConstraints.size() > 1) {
                    classConstraints.remove(containingClass);
                }
                break;
            default:
                classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(callTargetText, methodCall.getProject());
        }
        return classConstraints;
    }

    @NotNull
    public static String getCallTargetTypeIfAllocStatement(ObjJCallTarget callTarget) {
        if (callTarget.getQualifiedReference() != null && callTarget.getQualifiedReference().getMethodCall() != null) {
            ObjJMethodCall subMethodCall = callTarget.getQualifiedReference().getMethodCall();
            String subMethodCallSelectorString = subMethodCall.getSelectorString();
            if (subMethodCallSelectorString.equals("alloc:") || subMethodCallSelectorString.equals("new:")) {
                return subMethodCall.getCallTargetText();
            }
        }
        return callTarget.getText();
    }


}
