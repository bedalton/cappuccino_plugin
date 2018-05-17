package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJConditionalStatement

object ObjJSelectorLiteralPsiUtil {

    /*
    fun isRespondToSelector(callTarget: ObjJMethodCall): Boolean {
        if (true) {
            return false
        }
        val selectorString = callTarget.selectorString
        for (conditionalStatement in callTarget.getParentBlockChildrenOfType(ObjJConditionalStatement::class.java, true)) {
                val expr = conditionalStatement.conditionalExpression
                while (expr != null) {
                    if (expr.leftExpr != null) {
                        if (expr.leftExpr!!.selectorLiteral != null && expr.leftExpr!!.selectorLiteral!!.selectorString == selectorString) {
                            return true
                        }
                    }

                }
        }
        return false
    }*/
}
