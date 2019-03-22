package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*

object ObjJVariableAssignmentsPsiUtil {

    fun getAssignedValue(assignmentLogical: ObjJVariableAssignmentLogical): ObjJExpr {
        return assignmentLogical.assignmentExprPrime.expr
    }

}
