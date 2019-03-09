@file:Suppress("UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*

import java.util.ArrayList
import java.util.Objects

fun allInternalExpressions(expr: ObjJExpr): List<ObjJExpr> {
    val out = ArrayList<ObjJExpr>()
    allInternalExpressions(out, expr)
    return out
}

fun isAssignmentToVariableWithName(expr: ObjJExpr?, variableNameString: String): Boolean {
    return expr != null && expr.leftExpr != null && isLeftExpressionAssignmentToVariable(expr.leftExpr, variableNameString)
}


private fun isLeftExpressionAssignmentToVariable(leftExpr: ObjJLeftExpr?, variableNameString: String): Boolean {
    if (leftExpr == null) {
        return false
    }
    if (leftExpr.variableAssignmentLogical != null) {
        val assignment = leftExpr.variableAssignmentLogical
        if (assignment?.qualifiedReference != null) {
            val qualifiedName = ObjJVariableNameUtil.getQualifiedNameAsString(assignment.qualifiedReference, null)
            return qualifiedName == variableNameString
        }
        return assignment?.qualifiedReference?.text == variableNameString
    }
    if (leftExpr.variableDeclaration != null) {
        val variableDeclaration = leftExpr.variableDeclaration
        for (qualifiedReference in variableDeclaration!!.qualifiedReferenceList) {
            val qualifiedName = ObjJVariableNameUtil.getQualifiedNameAsString(qualifiedReference, null)
            return qualifiedName == variableNameString
        }
    }
    return false
}


private fun allInternalExpressions(out: MutableList<ObjJExpr>, expr: ObjJExpr?) {
    if (expr == null) {
        return
    }
    out.add(expr)
    var loopingExpression = expr
    while (loopingExpression?.expr != null) {
        out.add(loopingExpression)
        loopingExpression = loopingExpression.expr
        allInternalExpressions(out, loopingExpression?.leftExpr)
    }
    for (loopExpr in ArrayList(out)) {
        for (right in loopExpr.rightExprList) {
            allInternalExpressions(out, right)
        }
    }
}


private fun allInternalExpressions(out: MutableList<ObjJExpr>, leftExpr: ObjJLeftExpr?) {
    if (leftExpr == null) {
        return
    }
    // @todo figure out how to reimplement this
    /*
    for (expr in leftExpr.) {
        allInternalExpressions(out, expr)
    }*/
}

private fun allInternalExpressions(out: MutableList<ObjJExpr>, exprRight: ObjJRightExpr?) {
    if (exprRight == null) {
        return
    }
    //allInternalExpressions(foldingDescriptors, exprRight.getExpr());
}

fun getAllInternalMethodCallExpressions(expr: ObjJExpr): List<ObjJMethodCall> {
    val expressions = allInternalExpressions(expr)
    val out = ArrayList<ObjJMethodCall>()
    for (currentExpression in expressions) {
        getAllInternalMethodCallExpressions(out, currentExpression.leftExpr)
    }
    return out
}

private fun getAllInternalMethodCallExpressions(out: MutableList<ObjJMethodCall>, left: ObjJLeftExpr?) {
    if (left == null) {
        return
    }
    if (left.qualifiedReference?.methodCall != null) {
        out.add(left.qualifiedReference!!.methodCall!!)
    }
    if (left.methodCall != null) {
        out.add(left.methodCall!!)
    }
}