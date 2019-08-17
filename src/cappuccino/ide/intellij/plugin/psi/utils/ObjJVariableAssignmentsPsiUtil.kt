package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJVariableAssignmentLogical

object ObjJVariableAssignmentsPsiUtil {

    fun getAssignedValue(assignmentLogical: ObjJVariableAssignmentLogical): ObjJExpr? {
        return assignmentLogical.assignmentExprPrime.expr
    }

}
