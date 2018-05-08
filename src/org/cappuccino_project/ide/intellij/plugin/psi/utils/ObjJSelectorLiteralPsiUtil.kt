package org.cappuccino_project.ide.intellij.plugin.psi.utils

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJConditionalStatement

object ObjJSelectorLiteralPsiUtil {

    fun isRespondToSelector(callTarget: ObjJMethodCall): Boolean {
        if (true) {
            return false
        }
        val selectorString = callTarget.selectorString
        for (conditionalStatement in ObjJBlockPsiUtil.getParentBlockChildrenOfType(callTarget, ObjJConditionalStatement::class.java, true)) {
            if (conditionalStatement != null) {
                val expr = conditionalStatement.conditionalExpression
                while (expr != null) {
                    if (expr.leftExpr != null) {
                        if (expr.leftExpr!!.selectorLiteral != null && expr.leftExpr!!.selectorLiteral!!.selectorString == selectorString) {
                            return true
                        }
                    }

                }
            }
        }
        return false
    }
}
