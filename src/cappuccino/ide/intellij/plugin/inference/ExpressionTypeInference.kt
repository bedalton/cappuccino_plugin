package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListArrayType
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressIndicatorProvider
import javafx.scene.control.ProgressIndicator

fun inferExpressionType(expr: ObjJExpr?, tag: Long): InferenceResult? {
    if (expr == null)
        return null
    return expr.getCachedInferredTypes(tag) {
        if (expr.tagged(tag))
            return@getCachedInferredTypes null
        internalInferExpressionType(expr, tag)
    }
}

private fun internalInferExpressionType(expr: ObjJExpr, tag: Long): InferenceResult? {
    ProgressIndicatorProvider.checkCanceled()
    if (expr.text == "self") {
        val parentClass = expr.getParentOfType(ObjJClassDeclarationElement::class.java)
        if (parentClass != null)
            return setOf(parentClass.classNameString).toInferenceResult()
    }

    if (expr.text == "super") {
        val parentClass = expr.getParentOfType(ObjJClassDeclarationElement::class.java)
        if (parentClass is ObjJImplementationDeclaration) {
            return setOf(parentClass.superClassName ?: parentClass.classNameString).toInferenceResult()
        } else if (parentClass != null)
            return setOf(parentClass.classNameString).toInferenceResult()
    }

    if (expr.text == "this") {
        return InferenceResult(types = setOf("Object").toJsTypeList())
    }

    //ProgressManager.checkCanceled()
    val leftExpressionType = if (expr.leftExpr != null && expr.rightExprList.isEmpty()) {
        leftExpressionType(expr.leftExpr, tag)
    } else if (
            (expr.leftExpr?.functionCall != null || expr.leftExpr?.methodCall != null)
            //&& expr.rightExprList.firstOrNull()?.qualifiedReferencePrime != null
    ) {
        val qualifiedNameParts = expr.rightExprList.firstOrNull()?.qualifiedReferencePrime?.qualifiedNameParts.orEmpty();
        val qualifiedReferenceResult = inferQualifiedReferenceType(qualifiedNameParts, tag) ?: return null
        InferenceResult(types = qualifiedReferenceResult.types)
    } else {
        null
    }
    val rightExpressionsType = if (expr.rightExprList.isNotEmpty())
        rightExpressionTypes(expr.leftExpr, expr.rightExprList, tag)
    else {
        null
    }
    val isNumeric: Boolean = IsNumericUtil.isNumeric(expr) || leftExpressionType?.isNumeric.orFalse() || rightExpressionsType?.isNumeric.orFalse()
    val isBoolean: Boolean = IsBooleanUtil.isBoolean(expr) || leftExpressionType?.isBoolean.orFalse() || rightExpressionsType?.isBoolean.orFalse()
    val isRegex: Boolean = isRegex(expr) || leftExpressionType?.isRegex.orFalse() || rightExpressionsType?.isRegex.orFalse()
    val isDictionary: Boolean = leftExpressionType?.isDictionary ?: rightExpressionsType?.isDictionary ?: false
    val isString: Boolean = isString(expr) || (leftExpressionType?.isString ?: rightExpressionsType?.isString ?: false)
    val isSelector: Boolean = isSelector(expr) || (leftExpressionType?.isSelector ?: rightExpressionsType?.isSelector
    ?: false)
    val types = leftExpressionType?.types.orEmpty() + rightExpressionsType?.types.orEmpty()
    return InferenceResult(
            types = types,
            isNumeric = isNumeric,
            isBoolean = isBoolean,
            isRegex = isRegex,
            isDictionary = isDictionary,
            isString = isString,
            isSelector = isSelector
    )
}

fun leftExpressionType(leftExpression: ObjJLeftExpr?, tag: Long): InferenceResult? {
    if (leftExpression == null) {// || level < 0) {
        return null
    }
    return leftExpression.getCachedInferredTypes(tag) {
        ProgressIndicatorProvider.checkCanceled()
        if (leftExpression.tagged(tag, false))
            return@getCachedInferredTypes null

        if (leftExpression.functionCall != null) {
            return@getCachedInferredTypes inferFunctionCallReturnType(leftExpression.functionCall!!, tag)
        }
        if (leftExpression.parenEnclosedExpr != null) {
            if (leftExpression.parenEnclosedExpr?.expr == null)
                return@getCachedInferredTypes null
            return@getCachedInferredTypes inferExpressionType(leftExpression.parenEnclosedExpr!!.expr!!, tag)
        }

        if (leftExpression.newExpression != null) {
            return@getCachedInferredTypes setOf(leftExpression.newExpression?.functionCall?.functionNameString
                    ?: "Object").toInferenceResult()
        }

        if (leftExpression.qualifiedReference != null)
            return@getCachedInferredTypes inferQualifiedReferenceType(leftExpression.qualifiedReference!!.qualifiedNameParts, tag)

        if (leftExpression.primary != null) {
            val primary = leftExpression.primary ?: return@getCachedInferredTypes null
            when {
                primary.integer != null ->
                    return@getCachedInferredTypes InferenceResult(
                            isNumeric = true,
                            types = setOf("int").toJsTypeList()
                    )
                primary.decimalLiteral != null -> return@getCachedInferredTypes InferenceResult(
                        isNumeric = true,
                        types = setOf("number").toJsTypeList()
                )
                primary.booleanLiteral != null -> return@getCachedInferredTypes setOf("boolean").toInferenceResult()
                primary.stringLiteral != null -> return@getCachedInferredTypes setOf("string").toInferenceResult()
                primary.nullLiterals != null -> return@getCachedInferredTypes null
            }
        }
        if (leftExpression.regularExpressionLiteral != null)
            return@getCachedInferredTypes InferenceResult(isRegex = true)
        if (leftExpression.methodCall != null)
            return@getCachedInferredTypes inferMethodCallType(leftExpression.methodCall!!, tag)
        if (leftExpression.refExpression?.variableName != null)
            return@getCachedInferredTypes inferVariableNameTypeAtIndexZero(leftExpression.refExpression!!.variableName!!, tag)
        if (leftExpression.variableAssignmentLogical != null)
            return@getCachedInferredTypes InferenceResult(isNumeric = true)
        if (leftExpression.typeOfExprPrime != null)
            return@getCachedInferredTypes InferenceResult(isString = true)
        if (leftExpression.minusMinus != null || leftExpression.plusPlus != null)
            return@getCachedInferredTypes InferenceResult(isNumeric = true)
        if (leftExpression.arrayLiteral != null) {
            var types: Set<JsTypeListType> = getInferredTypeFromExpressionArray(leftExpression.arrayLiteral!!.exprList, tag).classes.toJsTypeList()
            if (types.isNotNullOrEmpty())
                types = types + JsTypeListArrayType(types, 1)
            return@getCachedInferredTypes InferenceResult(
                    types = types
            )
        }
        val objectLiteral = leftExpression.objectLiteral
        if (objectLiteral != null) {
            val interaceBody = objectLiteral.toJsObjectTypeSimple()
            val objectClass = if (objectLiteral.atOpenBrace != null) {
                "CPDictionary"
            } else "Object"
            return@getCachedInferredTypes InferenceResult(
                    types = setOf(JsTypeListType.JsTypeListBasicType(objectClass), interaceBody)
            )
        }
        if (leftExpression.variableDeclaration?.expr != null) {
            val variableDeclaration = leftExpression.variableDeclaration!!
            return@getCachedInferredTypes inferExpressionType(variableDeclaration.expr!!, tag)
        }

        if (leftExpression.functionDeclaration != null) {
            return@getCachedInferredTypes leftExpression.functionDeclaration!!.toJsFunctionTypeResult(tag)
        }
        if (leftExpression.functionLiteral != null) {
            return@getCachedInferredTypes leftExpression.functionLiteral!!.toJsFunctionTypeResult(tag)
        }
        if (leftExpression.derefExpression?.variableName != null) {
            return@getCachedInferredTypes inferVariableNameTypeAtIndexZero(leftExpression.derefExpression!!.variableName!!, tag)
        }
        return@getCachedInferredTypes INFERRED_ANY_TYPE
    }
}

fun rightExpressionTypes(leftExpression: ObjJLeftExpr?, rightExpressions: List<ObjJRightExpr>, tag: Long): InferenceResult? {
    if (leftExpression == null)// || level < 0)
        return null
    var orExpressionType: InferenceResult? = null
    var current = INFERRED_EMPTY_TYPE
    for (rightExpr in rightExpressions) {
        ProgressIndicatorProvider.checkCanceled()
        if (rightExpr.comparisonExprPrime != null)
            return InferenceResult(types = setOf(JS_BOOL).toJsTypeList(), isBoolean = true)
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
        if (rightExpr.logicExprPrime?.or != null) {
            if (rightExpr.logicExprPrime?.expr != null) {
                if (orExpressionType == null) {
                    orExpressionType = leftExpressionType(leftExpression, tag)
                }
                orExpressionType = listOfNotNull(
                        orExpressionType,
                        inferExpressionType(rightExpr.logicExprPrime?.expr!!, tag)
                ).combine()
                current = orExpressionType
            }
            continue
        }

        if (rightExpr.comparisonExprPrime != null || rightExpr.instanceOfExprPrime != null || rightExpr.logicExprPrime != null) {
            current = current.copy(isBoolean = true, types = current.classes.plus(JS_BOOL).toJsTypeList())
        }
        if (rightExpr.mathExprPrime != null) {
            val newTypes = inferExpressionType(rightExpr.mathExprPrime!!.expr, tag)?.classes.orEmpty() + current.classes
            if (isNotNumber(newTypes))
                return InferenceResult(
                        types = setOf(JS_STRING).toJsTypeList(),
                        isString = true
                )
            current = resolveToNumberType(newTypes)
        }
        if (rightExpr.arrayIndexSelector != null) {
            val types = current.types.flatMap {
                (it as? JsTypeListArrayType)?.types ?: (it as? JsTypeListType.JsTypeListMapType)?.valueTypes.orEmpty()
            }
            current = InferenceResult(types = types.toSet())
            //current = current.copy(classes = current.classes.plus(JS_ARRAY.className))
        }
    }
    return current
}

internal fun getInferredTypeFromExpressionArray(assignments: List<ObjJExpr>, tag: Long): InferenceResult {
    return assignments.mapNotNull {
        inferExpressionType(it, tag)
    }.combine()
}

private object IsNumericUtil {

    fun isNumeric(expr: ObjJExpr): Boolean {
        return if (expr.parent is ObjJAssignmentExprPrime)
            true
        else if (isPrefixExpressionNumeric(expr.prefixedExpr))
            true
        else if (expr.rightExprList.isEmpty() && expr.leftExpr != null)
            isLeftExprNumber(expr.leftExpr)
        else
            isNumber(expr.rightExprList)
    }

    private fun isLeftExprNumber(leftExpression: ObjJLeftExpr?): Boolean {
        if (leftExpression == null)
            return false
        if (leftExpression.plusPlus != null || leftExpression.minusMinus != null)
            return true
        if (leftExpression.variableAssignmentLogical != null)
            return true
        return false
    }

    private fun isNumber(rightExpressions: List<ObjJRightExpr>): Boolean {
        if (rightExpressions.isEmpty())
            return false
        for (rightExpr in rightExpressions) {
            //ProgressManager.checkCanceled()
            if (isNumeric(rightExpr))
                return true
        }
        return false
    }

    private fun isNumeric(rightExpr: ObjJRightExpr): Boolean {
        return rightExpr.mathExprPrime != null
    }

    private fun isPrefixExpressionNumeric(prefixedExpr: ObjJPrefixedExpr?): Boolean {
        return prefixedExpr?.minus != null
                || prefixedExpr?.plus != null
                || prefixedExpr?.bitNot != null
    }
}

private object IsBooleanUtil {
    internal fun isBoolean(expr: ObjJExpr?): Boolean {
        if (expr == null) return false
        return when {
            isPrefixExpressionBoolean(expr.prefixedExpr) -> true
            expr.rightExprList.isNotEmpty() -> isBoolean(expr.rightExprList)
            expr.leftExpr != null -> isBoolean(expr.leftExpr!!)
            else -> false
        }
    }

    private fun isBoolean(expr: ObjJLeftExpr?): Boolean {
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

    private fun isBoolean(rightExpressions: List<ObjJRightExpr>): Boolean {
        for (expr in rightExpressions) {
            //ProgressManager.checkCanceled()
            if (isBoolean(expr))
                return true
        }
        return false
    }

    private fun isBoolean(expr: ObjJRightExpr): Boolean {
        if (expr.comparisonExprPrime != null)
            return true
        if (expr.parenEnclosedExpr?.expr != null) {
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

    private fun isPrefixExpressionBoolean(prefixedExpr: ObjJPrefixedExpr?): Boolean {
        return prefixedExpr?.not != null
    }
}

private fun isString(expr: ObjJExpr): Boolean {
    return expr.leftExpr?.primary?.stringLiteral != null
}

private fun isSelector(expr: ObjJExpr): Boolean {
    return expr.leftExpr?.selectorLiteral != null
}

private fun isRegex(expr: ObjJExpr): Boolean {
    return expr.leftExpr?.regularExpressionLiteral != null
}

val addsToNumberTypes = numberTypes.map { it.toLowerCase() } + "jsobject" + "Object"

private fun isNotNumber(classes: Set<String>): Boolean {
    return classes.any {
        it !in anyTypes && it.toLowerCase() !in addsToNumberTypes
    }
}

private fun resolveToNumberType(newTypes: Set<String>): InferenceResult {
    return if (JS_DOUBLE in newTypes && JS_NUMBER !in newTypes) {
        InferenceResult(
                types = setOf(JS_DOUBLE).toJsTypeList(),
                isNumeric = true
        )
    } else if (JS_FLOAT in newTypes && JS_NUMBER !in newTypes) {
        InferenceResult(
                types = setOf(JS_FLOAT).toJsTypeList(),
                isNumeric = true
        )
    } else if ((JS_LONG in newTypes || JS_LONG_LONG in newTypes) && JS_LONG !in newTypes) {
        InferenceResult(
                types = setOf(JS_LONG).toJsTypeList(),
                isNumeric = true
        )
    } else if (JS_INT in newTypes && JS_NUMBER !in newTypes) {
        InferenceResult(
                types = setOf(JS_INT).toJsTypeList(),
                isNumeric = true
        )
    } else {
        InferenceResult(
                types = setOf(JS_NUMBER).toJsTypeList(),
                isNumeric = true
        )
    }
}


const val JS_DOUBLE = "double"
const val JS_NUMBER = "number"
const val JS_FLOAT = "float"
const val JS_LONG = "long"
const val JS_LONG_LONG = "long long"
const val JS_INT = "int"
const val JS_BOOL = "boolean"
const val JS_STRING = "string"
