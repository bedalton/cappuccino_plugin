package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJConditionalStatement;

public class ObjJSelectorLiteralPsiUtil {

    public static boolean isRespondToSelector(ObjJMethodCall callTarget) {
        if (true) {
            return false;
        }
        String selectorString = callTarget.getSelectorString();
        for (ObjJConditionalStatement conditionalStatement : ObjJBlockPsiUtil.getParentBlockChildrenOfType(callTarget, ObjJConditionalStatement.class, true)) {
            if (conditionalStatement != null) {
                ObjJExpr expr = conditionalStatement.getConditionalExpression();
                while (expr != null) {
                    if (expr.getLeftExpr() != null) {
                        if (expr.getLeftExpr().getSelectorLiteral() != null && expr.getLeftExpr().getSelectorLiteral().getSelectorString().equals(selectorString)) {
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }
}
