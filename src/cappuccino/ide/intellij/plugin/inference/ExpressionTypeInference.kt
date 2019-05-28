package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.utils.orFalse

fun inferenceExpressionType(expr:ObjJExpr, level:Int) : InferenceResult? {
    val isNumeric:Boolean = IsNumericUtil.isNumeric(expr)
    val isBoolean:Boolean = IsBooleanUtil.isBoolean(expr, level - 1)
    return InferenceResult(
            isNumeric = isNumeric,
            isBoolean = isBoolean
    )
}

internal fun getInferredTypeFromExpressionArray(assignments:List<ObjJExpr>, level:Int = 4) : InferenceResult {
    val inferredTypes = assignments.mapNotNull { inferenceExpressionType(it, level - 1) }
    val isNumeric = inferredTypes.any { it.isNumeric}
    val isDictionary = inferredTypes.any { it.isDictionary }
    val isBoolean = inferredTypes.any { it.isBoolean }
    val isString = inferredTypes.any { it.isString }
    val isSelector = inferredTypes.any { it.isSelector }
    val isRegex = inferredTypes.any { it.isRegex }
    val functionTypes = inferredTypes.flatMap { it.functionTypes ?: emptyList()  }
    val arrayTypes = inferredTypes.flatMap { it.arrayTypes ?: emptyList()  }
    val classes = inferredTypes.flatMap { it.classes }
    val jsObjectKeys = inferredTypes.flatMap { it.jsObjectKeys ?: emptyList()  }
    return InferenceResult(
            isNumeric = isNumeric,
            isBoolean = isBoolean,
            isString = isString,
            isDictionary = isDictionary,
            isSelector = isSelector,
            isRegex = isRegex,
            functionTypes = if (functionTypes.isNotEmpty()) functionTypes else null,
            arrayTypes = if (arrayTypes.isNotEmpty()) arrayTypes else null,
            classes = classes,
            jsObjectKeys = if (jsObjectKeys.isNotEmpty()) jsObjectKeys else null
    )
}


private object IsNumericUtil {

    fun isNumeric(expr:ObjJExpr):Boolean {
        return if(isPrefixExpressionNumeric(expr.prefixedExpr))
            true
        else if (expr.rightExprList.isEmpty() && expr.leftExpr != null)
            isLeftExprNumber(expr.leftExpr)
        else
            isNumber(expr.rightExprList)
    }

    private fun isLeftExprNumber(leftExpression:ObjJLeftExpr?) : Boolean {
        if (leftExpression == null)
            return false
        if (leftExpression.plusPlus != null || leftExpression.minusMinus != null)
            return true
        if (leftExpression.refExpression != null) {
            return isRefVariableNumeric(leftExpression.refExpression!!.variableName)
        }
        if (leftExpression.)
    }

    private fun isNumber(rightExpressions:List<ObjJRightExpr>) : Boolean {
        if (rightExpressions.isEmpty())
            return false

    }

    private fun isRefVariableNumeric(variable:ObjJVariableName) : Boolean {

    }


    private fun isPrefixExpressionNumeric(prefixedExpr:ObjJPrefixedExpr?) : Boolean {
        return prefixedExpr?.minus != null
                || prefixedExpr?.plus != null
                || prefixedExpr?.bitNot != null
    }
}

private object IsBooleanUtil {
    internal fun isBoolean(expr:ObjJExpr?, level:Int) : Boolean {
        if (expr == null) return false
        return if (isPrefixExpressionBoolean(expr.prefixedExpr))
            true
        else if (expr.rightExprList.isNotEmpty())
            isBoolean(expr.rightExprList, level)
        else if (expr.leftExpr != null)
            isBoolean(expr.leftExpr!!, level)
        else
            false
    }

    private fun isBoolean(expr: ObjJLeftExpr?, level:Int) : Boolean {
        if (expr == null)
            return false
        if (expr.primary != null) {
            return expr.primary!!.booleanLiteral != null
        }
        if (expr.enclosedExpr?.expr != null) {
            return isBoolean(expr.enclosedExpr!!.expr, level)
        }
        if (expr.functionCall != null) {
            return inferFunctionCallReturnType(expr.functionCall!!, level)?.isBoolean.orFalse()
        }
        if (expr.methodCall != null)
            return inferMethodCallType(expr.methodCall!!, level).isBoolean
        if (expr.qualifiedReference)
            return qualifiedR
        return false
    }

    private fun isBoolean(rightExpressions: List<ObjJRightExpr>, level:Int) : Boolean {
        for (expr in rightExpressions) {
            if (isBoolean(expr, level))
                return true
        }
        return false
    }

    private fun isBoolean(expr:ObjJRightExpr, level:Int) : Boolean {
        if (expr.comparisonExprPrime != null)
            return true
        if (expr.enclosedExpr?.expr != null){
            return isBoolean(expr.enclosedExpr!!.expr!!, level)
        }
        if (expr.logicExprPrime != null) {
            return true
        }
        if (expr.ternaryExprPrime != null) {
            return isBoolean(expr.ternaryExprPrime!!.ifTrue, level) || isBoolean(expr.ternaryExprPrime!!.ifFalse, level)
        }
        if (expr.instanceOfExprPrime != null)
            return true
        if (expr.comparisonExprPrime != null)
            return true
        return false
    }

    private fun isPrefixExpressionBoolean(prefixedExpr:ObjJPrefixedExpr?) : Boolean {
        return prefixedExpr?.not != null
    }
}