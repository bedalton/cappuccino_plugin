package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJVariableAssignment;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObjJVariableAssignmentsPsiUtil {

    public static List<ObjJVariableAssignment> getAllVariableAssignmentsMatchingName(@NotNull final PsiElement element, @NotNull final String fqName) {
        List<ObjJVariableAssignment> bodyVariableAssignments = getAllVariableAssignements(element);
        return ArrayUtils.filter(bodyVariableAssignments, (assignment) -> {
            for (ObjJQualifiedReference qualifiedReference : assignment.getQualifiedReferenceList()) {
                if (qualifiedReference.getVariableNameList().isEmpty()) {
                    continue;
                }
                String currentVariableFqName = ObjJVariableNameUtil.getQualifiedNameAsString(qualifiedReference, null);
                if (Objects.equals(fqName, currentVariableFqName)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static List<ObjJVariableAssignment> getAllVariableAssignements(PsiElement block) {
        List<ObjJCompositeElement> compositeElements = ObjJBlockPsiUtil.getParentBlockChildrenOfType(block, ObjJCompositeElement.class, true);
        List<ObjJVariableAssignment> declarations = new ArrayList<>();
        for (ObjJCompositeElement compositeElement : compositeElements) {
            if (compositeElement instanceof ObjJBodyVariableAssignment) {
                addVariableDeclarationFromBodyVariableAssignment(declarations,(ObjJBodyVariableAssignment)compositeElement);
            } else if (compositeElement instanceof ObjJExpr) {
                addVariableDeclarationFromExpression(declarations, (ObjJExpr)compositeElement);
            }
        }
        addVariableDeclarationsInFile(declarations, block.getContainingFile());
        return declarations;
    }

    private static void addVariableDeclarationsInFile(@NotNull List<ObjJVariableAssignment> declarations, @NotNull PsiFile file) {
        for (ObjJBodyVariableAssignment variableAssignment : ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJBodyVariableAssignment.class)) {
            addVariableDeclarationFromBodyVariableAssignment(declarations, variableAssignment);
        }
    }

    private static void addVariableDeclarationFromBodyVariableAssignment(@NotNull List<ObjJVariableAssignment> declarations, @NotNull ObjJBodyVariableAssignment bodyVariableAssignment) {
        declarations.addAll(ObjJTreeUtil.getChildrenOfTypeAsList(bodyVariableAssignment, ObjJVariableAssignment.class));
    }

    private static void addVariableDeclarationFromExpression(@NotNull List<ObjJVariableAssignment> declarations, @NotNull ObjJExpr expression) {
        if (expression.getLeftExpr() == null) {
            return;
        }
        if (expression.getLeftExpr().getVariableDeclaration() == null) {
            return;
        }
        declarations.addAll(ObjJTreeUtil.getChildrenOfTypeAsList(expression.getLeftExpr(), ObjJVariableAssignment.class));
    }

    @NotNull
    public static ObjJExpr getAssignedValue(@NotNull ObjJVariableAssignmentLogical assignmentLogical) {
        return assignmentLogical.getAssignmentExprPrime().getExpr();
    }

}
