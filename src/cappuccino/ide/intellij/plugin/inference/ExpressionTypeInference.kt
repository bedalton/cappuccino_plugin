package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.utils.orElse
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressManager

fun inferExpressionType(expr:ObjJExpr, level:Int) : InferenceResult? {
    ProgressManager.checkCanceled()
    val leftExpressionType = if (expr.leftExpr != null && expr.rightExprList.isEmpty())
        leftExpressionType(expr.leftExpr, level - 1)
    else if (expr.leftExpr?.functionCall != null && expr.rightExprList.firstOrNull()?.qualifiedReferencePrime != null) {
        LOGGER.info("Checking left expression with function name first")
        val components = mutableListOf<ObjJQualifiedReferenceComponent>(expr.leftExpr!!.functionCall!!)
        components.addAll(expr.rightExprList.first().qualifiedReferencePrime!!.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java))
        inferQualifiedReferenceType(components, false, level - 1)
    } else {
        LOGGER.info("No match for expression parse")
        null
    }
    val rightExpressionsType = if (leftExpressionType != null && expr.rightExprList.isNotEmpty())
        rightExpressionTypes(expr.leftExpr, expr.rightExprList, level - 1)
    else
        null
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
    LOGGER.info("Expr: <${expr.text}>: Types: {${out.classes}}")
    return out
}

fun leftExpressionType(leftExpression: ObjJLeftExpr?, level:Int) : InferenceResult? {
    ProgressManager.checkCanceled()
    if (leftExpression == null || level < 0) {
        LOGGER.info("Level is less than 0, returning")
        return null
    }
    LOGGER.info("Checking left expression type for Expression: ${leftExpression.text}")
    if (leftExpression.functionCall != null)
        return inferFunctionCallReturnType(leftExpression.functionCall!!, level)
    if (leftExpression.parenEnclosedExpr != null) {
        if (leftExpression.parenEnclosedExpr?.expr == null)
            return null
        return inferExpressionType(leftExpression.parenEnclosedExpr!!.expr!!, level)
    }
    if (leftExpression.qualifiedReference != null)
        return inferQualifiedReferenceType(leftExpression.qualifiedReference!!.qualifiedNameParts, false, level)
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
        return inferMethodCallType(leftExpression.methodCall!!, level)
    if (leftExpression.refExpression != null)
        return inferVariableNameType(leftExpression.refExpression!!.variableName, level)
    if (leftExpression.variableAssignmentLogical != null)
        return InferenceResult(isNumeric = true)
    if (leftExpression.typeOfExprPrime != null)
        return InferenceResult(isString = true)
    if (leftExpression.minusMinus != null || leftExpression.plusPlus != null)
        return InferenceResult(isNumeric = true)
    if (leftExpression.arrayLiteral != null) {
        val types = getInferredTypeFromExpressionArray(leftExpression.arrayLiteral!!.exprList).classes
        return InferenceResult(
                classes = setOf("Array", "object"),
                arrayTypes = if (types.isNotEmpty()) types else setOf("?")
        )
    }
    val objectLiteral = leftExpression.objectLiteral
    if (objectLiteral != null) {
        val keys:MutableMap<String, InferenceResult> = mutableMapOf()
        for (property in objectLiteral.propertyAssignmentList) {
            val types = if (property.expr != null)
                inferExpressionType(property.expr!!, level) ?: INFERRED_ANY_TYPE
            else
                INFERRED_ANY_TYPE
            keys[property.propertyName.text] = types
        }
        return InferenceResult(
                classes = setOf("object"),
                jsObjectKeys = keys
        )
    }
    if (leftExpression.variableDeclaration != null) {
        val variableDeclaration = leftExpression.variableDeclaration!!
        return inferExpressionType(variableDeclaration.expr, level)
    }

    if (leftExpression.functionDeclaration != null) {
        return leftExpression.functionDeclaration!!.toJsFunctionType(level)
    }
    if (leftExpression.functionLiteral != null) {
        return leftExpression.functionLiteral!!.toJsFunctionType(level)
    }
    if (leftExpression.derefExpression != null) {
        return inferVariableNameType(leftExpression.derefExpression!!.variableName, level)
    }
    return INFERRED_ANY_TYPE
}

private fun ObjJFunctionDeclarationElement<*>.toJsFunctionType(level:Int) : InferenceResult {
    val returnTypes = inferFunctionDeclarationReturnType(this, level - 1) ?: INFERRED_ANY_TYPE
    return InferenceResult(
            functionTypes = listOf(
                    JsFunctionType(this.parameterTypes(), returnTypes)
            )
    )
}

private fun ObjJFunctionDeclarationElement<*>.parameterTypes() : Map<String, InferenceResult> {
    ProgressManager.checkCanceled()
    val parameters = formalParameterArgList
    val out = mutableMapOf<String, InferenceResult>()
    val commentWrapper = this.docComment
    for ((i, parameter) in parameters.withIndex()) {
        ProgressManager.checkCanceled()
        val parameterName = parameter.variableName?.text ?: "$i"
        if (i < commentWrapper?.parameterComments?.size.orElse(0)) {
            val parameterType = commentWrapper?.parameterComments
                    ?.get(i)
                    ?.type
                    ?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX)
            out[parameterName] = if (parameterType != null) InferenceResult(classes = parameterType.toSet())  else INFERRED_ANY_TYPE
        } else {
            out[parameterName] = INFERRED_ANY_TYPE
        }
    }
    return out
}

fun rightExpressionTypes(leftExpression: ObjJLeftExpr?, rightExpressions:List<ObjJRightExpr>, level: Int) : InferenceResult? {
    ProgressManager.checkCanceled()
    if (leftExpression == null || level < 0)
        return null
    var current = InferenceResult()
    var didAdd = false
    for (rightExpr in rightExpressions) {
        ProgressManager.checkCanceled()
        LOGGER.info("Checking right expression type for Expression: ${rightExpr.text}")
        if (rightExpr.ternaryExprPrime != null) {
            val ternaryExpr = rightExpr.ternaryExprPrime!!
            val ifTrue = inferExpressionType(ternaryExpr.ifTrue, level)
            val ifFalse = if (ternaryExpr.ifFalse?.expr != null) inferExpressionType(ternaryExpr.ifFalse!!.expr!!, level) else null
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
            current = current.copy(isNumeric = true, classes = current.classes.plus(JS_NUMBER.className))
            didAdd = didAdd || rightExpr.mathExprPrime!!.mathOp.plus != null
        }
        if (rightExpr.arrayIndexSelector != null) {
            current = current.copy(classes = current.classes.plus(JS_ARRAY.className))
        }
    }
    if (didAdd) {
        if (!current.classes.contains(JS_STRING.className) && current.classes.contains("number"))
            current = current.copy(isNumeric = true, classes = current.classes.plus(JS_NUMBER.className))
        else if (!current.classes.contains(JS_ARRAY.className))
            current = InferenceResult(classes = setOf(JS_STRING.className))
    }
    return current
}

internal fun getInferredTypeFromExpressionArray(assignments:List<ObjJExpr>, level:Int = 4) : InferenceResult {
    return assignments.mapNotNull { inferExpressionType(it,  level - 1) }.collapse()
}

internal fun List<InferenceResult>.collapse() : InferenceResult {
    val isNumeric = this.any { it.isNumeric}
    val isDictionary = this.any { it.isDictionary }
    val isBoolean = this.any { it.isBoolean }
    val isString = this.any { it.isString }
    val isSelector = this.any { it.isSelector }
    val isRegex = this.any { it.isRegex }
    val functionTypes = this.flatMap { it.functionTypes ?: emptyList()  }
    val classes = this.flatMap { it.classes }.toSet()
    var jsObjectKeys:Map<String, InferenceResult> = emptyMap()
    this.mapNotNull { it.jsObjectKeys }.forEach {
        jsObjectKeys = combine(jsObjectKeys, it) ?: jsObjectKeys
    }
    return InferenceResult(
            isNumeric = isNumeric,
            isBoolean = isBoolean,
            isString = isString,
            isDictionary = isDictionary,
            isSelector = isSelector,
            isRegex = isRegex,
            functionTypes = if (functionTypes.isNotEmpty()) functionTypes else null,
            classes = classes,
            jsObjectKeys = if (jsObjectKeys.isNotEmpty()) jsObjectKeys else null
    )
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