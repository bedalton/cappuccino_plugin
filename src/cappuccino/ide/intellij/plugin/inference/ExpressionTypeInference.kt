package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressManager

fun inferExpressionType(expr:ObjJExpr?, tag:Long) : InferenceResult? {
    if (expr == null)
        return null
    return expr.getCachedInferredTypes {
        if (expr.tagged(tag))
            return@getCachedInferredTypes null
        internalInferExpressionType(expr, tag)
    }
}

private fun internalInferExpressionType(expr:ObjJExpr, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    val leftExpressionType = if (expr.leftExpr != null && expr.rightExprList.isEmpty()) {
        leftExpressionType(expr.leftExpr, tag)
    } else if (expr.leftExpr?.functionCall != null && expr.rightExprList.firstOrNull()?.qualifiedReferencePrime != null) {
        val components = mutableListOf<ObjJQualifiedReferenceComponent>(expr.leftExpr!!.functionCall!!)
        components.addAll(expr.rightExprList.first().qualifiedReferencePrime!!.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java))
        inferQualifiedReferenceType(components, tag)
    } else {
        null
    }
    val rightExpressionsType = if (expr.rightExprList.isNotEmpty())
        rightExpressionTypes(expr.leftExpr, expr.rightExprList, tag)
    else {
        null
    }
    val isNumeric:Boolean = IsNumericUtil.isNumeric(expr) || leftExpressionType?.isNumeric.orFalse() || rightExpressionsType?.isNumeric.orFalse()
    val isBoolean:Boolean = IsBooleanUtil.isBoolean(expr) || leftExpressionType?.isBoolean.orFalse() || rightExpressionsType?.isBoolean.orFalse()
    val isRegex:Boolean = isRegex(expr) || leftExpressionType?.isRegex.orFalse() || rightExpressionsType?.isRegex.orFalse()
    val isDictionary:Boolean = leftExpressionType?.isDictionary ?: rightExpressionsType?.isDictionary ?: false
    val isString:Boolean = isString(expr) || (leftExpressionType?.isString ?: rightExpressionsType?.isString ?: false)
    val jsObjectKeys= combine(leftExpressionType?.jsObjectKeys, rightExpressionsType?.jsObjectKeys)
    val isSelector:Boolean = isSelector(expr) || (leftExpressionType?.isSelector ?: rightExpressionsType?.isSelector ?: false)
    val classes = leftExpressionType?.classes.orEmpty() + rightExpressionsType?.classes.orEmpty()
    val arrayTypes = leftExpressionType?.arrayTypes.orEmpty() + rightExpressionsType?.arrayTypes.orEmpty()
    val functionTypes = leftExpressionType?.functionTypes.orEmpty() + rightExpressionsType?.functionTypes.orEmpty()
    val out = InferenceResult(
            isNumeric = isNumeric,
            isBoolean = isBoolean,
            isRegex = isRegex,
            isDictionary = isDictionary,
            isString = isString,
            jsObjectKeys =  jsObjectKeys,
            isSelector = isSelector,
            classes = classes,
            arrayTypes = if (arrayTypes.isNotEmpty()) arrayTypes else null,
            functionTypes = if (functionTypes.isNotEmpty()) functionTypes else null
    )
    return out
}

fun leftExpressionType(leftExpression: ObjJLeftExpr?, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    if (leftExpression == null){// || level < 0) {
        return null
    }

    if (leftExpression.functionCall != null)
        return inferFunctionCallReturnType(leftExpression.functionCall!!, tag)
    if (leftExpression.parenEnclosedExpr != null) {
        if (leftExpression.parenEnclosedExpr?.expr == null)
            return null
        return inferExpressionType(leftExpression.parenEnclosedExpr!!.expr!!, tag)
    }
    if (leftExpression.qualifiedReference != null)
        return inferQualifiedReferenceType(leftExpression.qualifiedReference!!.qualifiedNameParts, tag)
    if (leftExpression.primary != null) {
        val primary = leftExpression.primary ?: return null
        if (primary.integer != null || primary.decimalLiteral != null) {
            return InferenceResult(
                    isNumeric = true
            )
        } else if (primary.booleanLiteral != null)
            return InferenceResult(isBoolean = true)
        else if (primary.stringLiteral != null)
            return InferenceResult(isString = true)
    }
    if (leftExpression.regularExpressionLiteral != null)
        return InferenceResult(isRegex = true)
    if (leftExpression.methodCall != null)
        return inferMethodCallType(leftExpression.methodCall!!, tag)
    if (leftExpression.refExpression?.variableName != null)
        return inferVariableNameType(leftExpression.refExpression!!.variableName!!, tag)
    if (leftExpression.variableAssignmentLogical != null)
        return InferenceResult(isNumeric = true)
    if (leftExpression.typeOfExprPrime != null)
        return InferenceResult(isString = true)
    if (leftExpression.minusMinus != null || leftExpression.plusPlus != null)
        return InferenceResult(isNumeric = true)
    if (leftExpression.arrayLiteral != null) {
        val types = getInferredTypeFromExpressionArray(leftExpression.arrayLiteral!!.exprList, tag).classes
        return InferenceResult(
                classes = setOf("Array"),
                arrayTypes = if (types.isNotEmpty()) types else setOf("?")
        )
    }
    val objectLiteral = leftExpression.objectLiteral
    if (objectLiteral != null) {
        val keys = objectLiteral.toJsObjectTypeSimple().properties
        return InferenceResult(
                classes = setOf("object"),
                jsObjectKeys = keys
        )
    }
    if (leftExpression.variableDeclaration?.expr != null) {
        val variableDeclaration = leftExpression.variableDeclaration!!
        return inferExpressionType(variableDeclaration.expr!!, tag)
    }

    if (leftExpression.functionDeclaration != null) {
        return leftExpression.functionDeclaration!!.toJsFunctionTypeResult(tag)
    }
    if (leftExpression.functionLiteral != null) {
        return leftExpression.functionLiteral!!.toJsFunctionTypeResult(tag)
    }
    if (leftExpression.derefExpression?.variableName != null) {
        return inferVariableNameType(leftExpression.derefExpression!!.variableName!!, tag)
    }
    return INFERRED_ANY_TYPE
}

fun rightExpressionTypes(leftExpression: ObjJLeftExpr?, rightExpressions:List<ObjJRightExpr>, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    if (leftExpression == null)// || level < 0)
        return null
    var current = InferenceResult()
    for (rightExpr in rightExpressions) {
        if (rightExpr.comparisonExprPrime != null)
            return InferenceResult(classes = setOf(JS_BOOL.className), isBoolean = true)
        ProgressManager.checkCanceled()
        if (rightExpr.ternaryExprPrime != null) {
            val ternaryExpr = rightExpr.ternaryExprPrime!!
            val ifTrue = if (ternaryExpr.ifTrue?.expr != null) inferExpressionType(ternaryExpr.ifTrue!!.expr!!, tag) else null
            val ifFalse = if (ternaryExpr.ifFalse?.expr != null) inferExpressionType(ternaryExpr.ifFalse!!.expr!!, tag) else null
            val types = if (ifFalse != null && ifTrue != null)
                ifFalse + ifTrue
            else ifTrue ?: ifFalse
            if (types != null)
                current += types
        }
        if (rightExpr.comparisonExprPrime != null || rightExpr.instanceOfExprPrime != null || rightExpr.logicExprPrime != null) {
            current = current.copy(isBoolean = true, classes = current.classes.plus(JS_BOOL.className))
        }
        if (rightExpr.mathExprPrime != null) {
            val newTypes = inferExpressionType(rightExpr.mathExprPrime!!.expr, tag)?.classes.orEmpty() + current.classes
            if (isNotNumber(newTypes))
                return InferenceResult(
                        classes = setOf(JS_STRING.className),
                        isString = true
                )
            current = resolveToNumberType(newTypes)
        }
        if (rightExpr.arrayIndexSelector != null) {
            //current = current.copy(classes = current.classes.plus(JS_ARRAY.className))
        }
    }
    return current
}

internal fun getInferredTypeFromExpressionArray(assignments:List<ObjJExpr>, tag:Long) : InferenceResult {
    return assignments.mapNotNull { inferExpressionType(it,  tag) }.collapse()
}

private object IsNumericUtil {

    fun isNumeric(expr:ObjJExpr):Boolean {
        return if (expr.parent is ObjJAssignmentExprPrime)
            true
        else if(isPrefixExpressionNumeric(expr.prefixedExpr))
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
        if (leftExpression.variableAssignmentLogical != null)
            return true
        return false
    }

    private fun isNumber(rightExpressions:List<ObjJRightExpr>) : Boolean {
        if (rightExpressions.isEmpty())
            return false
        for(rightExpr in rightExpressions) {
            ProgressManager.checkCanceled()
            if (isNumeric(rightExpr))
                return true
        }
        return false
    }

    private fun isNumeric(rightExpr:ObjJRightExpr) : Boolean {
        return rightExpr.mathExprPrime != null
    }

    private fun isPrefixExpressionNumeric(prefixedExpr:ObjJPrefixedExpr?) : Boolean {
        return prefixedExpr?.minus != null
                || prefixedExpr?.plus != null
                || prefixedExpr?.bitNot != null
    }
}

private object IsBooleanUtil {
    internal fun isBoolean(expr:ObjJExpr?) : Boolean {
        if (expr == null) return false
        return when {
            isPrefixExpressionBoolean(expr.prefixedExpr) -> true
            expr.rightExprList.isNotEmpty() -> isBoolean(expr.rightExprList)
            expr.leftExpr != null -> isBoolean(expr.leftExpr!!)
            else -> false
        }
    }

    private fun isBoolean(expr: ObjJLeftExpr?) : Boolean {
        if (expr == null)
            return false
        if (expr.primary != null) {
            return expr.primary!!.booleanLiteral != null
        }
        if (expr.parenEnclosedExpr?.expr != null) {
            return isBoolean(expr.parenEnclosedExpr!!.expr)
        }
        return false
    }

    private fun isBoolean(rightExpressions: List<ObjJRightExpr>) : Boolean {
        for (expr in rightExpressions) {
            ProgressManager.checkCanceled()
            if (isBoolean(expr))
                return true
        }
        return false
    }

    private fun isBoolean(expr:ObjJRightExpr) : Boolean {
        if (expr.comparisonExprPrime != null)
            return true
        if (expr.parenEnclosedExpr?.expr != null){
            return isBoolean(expr.parenEnclosedExpr!!.expr!!)
        }
        if (expr.logicExprPrime != null) {
            return true
        }
        if (expr.ternaryExprPrime != null) {
            return isBoolean(expr.ternaryExprPrime!!.ifTrue) || isBoolean(expr.ternaryExprPrime!!.ifFalse)
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

private fun isString(expr:ObjJExpr) : Boolean {
    return expr.leftExpr?.primary?.stringLiteral != null
}

private fun isSelector(expr:ObjJExpr) : Boolean {
    return expr.leftExpr?.selectorLiteral != null
}

private fun isRegex(expr: ObjJExpr) : Boolean {
    return expr.leftExpr?.regularExpressionLiteral != null
}

val addsToNumberTypes = numberTypes.map { it.toLowerCase() } + "jsobject" + "object"

private fun isNotNumber(classes:Set<String>) : Boolean {
    return classes.any {
        it !in anyTypes && it.toLowerCase() !in addsToNumberTypes
    }
}

private fun resolveToNumberType(newTypes:Set<String>) : InferenceResult {
    return if (JS_DOUBLE.className in newTypes && JS_NUMBER.className !in newTypes) {
        InferenceResult(
                classes = setOf(JS_DOUBLE.className),
                isNumeric = true
        )
    } else if (JS_FLOAT.className in newTypes && JS_NUMBER.className !in newTypes) {
        InferenceResult(
                classes = setOf(JS_FLOAT.className),
                isNumeric = true
        )
    } else if ((JS_LONG.className in newTypes || JS_LONG_LONG.className in newTypes) && JS_LONG.className !in newTypes) {
        InferenceResult(
                classes = setOf(JS_LONG.className),
                isNumeric = true
        )
    } else if (JS_INT.className in newTypes && JS_NUMBER.className !in newTypes) {
        InferenceResult(
                classes = setOf(JS_INT.className),
                isNumeric = true
        )
    } else {
        InferenceResult(
                classes = setOf(JS_NUMBER.className),
                isNumeric = true
        )
    }
}