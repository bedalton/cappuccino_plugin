package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObjJExprPsiUtil {

    public static List<ObjJExpr> allInternalExpressions(@NotNull ObjJExpr expr) {
        List<ObjJExpr> out = new ArrayList<>();
        allInternalExpressions(out, expr);
        return out;
    }

    public static boolean isAssignmentToVariableWithName(@Nullable ObjJExpr expr, @NotNull String variableNameString) {
        return expr != null && expr.getLeftExpr() != null && isLeftExpressionAssignmentToVariable(expr.getLeftExpr(), variableNameString);
    }


    private static boolean isLeftExpressionAssignmentToVariable(@Nullable
                                                                       ObjJLeftExpr leftExpr, @NotNull String variableNameString) {
        if (leftExpr == null) {
            return false;
        }
        if (leftExpr.getVariableAssignmentLogical() != null) {
            final ObjJVariableAssignmentLogical assignment = leftExpr.getVariableAssignmentLogical();
            if (assignment.getQualifiedReference() != null) {
                final String qualifiedName = ObjJVariableNameUtil.getQualifiedNameAsString(assignment.getQualifiedReference(), null);
                return Objects.equals(qualifiedName, variableNameString);
            }
            return assignment.getVariableName() != null && assignment.getVariableName().equals(variableNameString);
        }
        if (leftExpr.getVariableDeclaration() != null) {
            ObjJVariableDeclaration variableDeclaration = leftExpr.getVariableDeclaration();
            for (ObjJQualifiedReference qualifiedReference : variableDeclaration.getQualifiedReferenceList()) {
                final String qualifiedName = ObjJVariableNameUtil.getQualifiedNameAsString(qualifiedReference, null);
                return Objects.equals(qualifiedName, variableNameString);
            }
        }
        return false;
    }


    private static void allInternalExpressions(List<ObjJExpr> out, @Nullable ObjJExpr expr) {
        if (expr == null) {
            return;
        }
        out.add(expr);
        ObjJExpr loopingExpression = expr;
        while (loopingExpression.getExpr() != null) {
            out.add(loopingExpression);
            loopingExpression = loopingExpression.getExpr();
            allInternalExpressions(out, loopingExpression.getLeftExpr());
        }
        for (ObjJExpr loopExpr : new ArrayList<>(out)) {
            for (ObjJRightExpr right : loopExpr.getRightExprList()) {
                allInternalExpressions(out, right);
            }
        }
    }

    private static void allInternalExpressions(List<ObjJExpr> out, @Nullable ObjJLeftExpr leftExpr) {
        if (leftExpr == null) {
            return;
        }
        for (ObjJExpr expr : leftExpr.getExprList()) {
            allInternalExpressions(out, expr);
        }
    }

    private static void allInternalExpressions (List<ObjJExpr> out, @Nullable ObjJRightExpr exprRight) {
        if (exprRight == null) {
            return;
        }
        //allInternalExpressions(out, exprRight.getExpr());
    }

    public static List<ObjJMethodCall> getAllInternalMethodCallExpressions(ObjJExpr expr) {
        List<ObjJExpr> expressions = allInternalExpressions(expr);
        List<ObjJMethodCall> out = new ArrayList<>();
        for (ObjJExpr currentExpression : expressions) {
            getAllInternalMethodCallExpressions(out,currentExpression.getLeftExpr());
        }
        return out;
    }

    private static void getAllInternalMethodCallExpressions(@NotNull List<ObjJMethodCall> out, @Nullable ObjJLeftExpr left) {
        if (left == null) {
            return;
        }
        if (left.getQualifiedReference() != null && left.getQualifiedReference().getMethodCall() != null) {
            out.add(left.getQualifiedReference().getMethodCall());
        }
        if (left.getMethodCall() != null) {
            out.add(left.getMethodCall());
        }
    }
}
