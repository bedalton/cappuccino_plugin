package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.VOID
import cappuccino.ide.intellij.plugin.contributor.globalJsFunctions
import cappuccino.ide.intellij.plugin.contributor.returnTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    return functionCall.getCachedInferredTypes {
        if (functionCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    LOGGER.info("Inferring Type for function call ${functionCall.functionName?.text}")
    val resolve = functionCall.functionName?.reference?.resolve()
    if (resolve == null) {
        val functionName = functionCall.functionName?.text
        if (functionName != null) {
            val out = globalJsFunctions
                    .filter {
                        it.name == functionName
                    }.flatMap {
                        it.returnTypes
                    }.toSet()
            if (out.isNotEmpty()) {
                return InferenceResult(classes = out)
            }
        }
        LOGGER.info("Failed to resolve function call reference")
        return null
    }
    val functionAsVariableName = resolve as? ObjJVariableName
    val function = (when {
        functionAsVariableName != null -> functionAsVariableName.parentFunctionDeclaration
        resolve is ObjJFunctionName -> resolve.parentFunctionDeclaration
        else -> null
    })
    if (function == null) {
        LOGGER.info("Failed to find function for function name element")
        val expression = functionAsVariableName?.getAssignmentExprOrNull() ?: return null
        return inferExpressionType(expression, tag)?.functionTypes?.firstOrNull()?.returnType
    }
    return inferFunctionDeclarationReturnType(function, tag)
}

fun inferFunctionDeclarationReturnType(function:ObjJFunctionDeclarationElement<*>, tag:Long) : InferenceResult? {
    val commentReturnTypes = function.docComment?.returnTypes
    if (commentReturnTypes.isNotNullOrEmpty())
        return InferenceResult(classes = commentReturnTypes!!)
    val returnStatementExpressions = function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    if (returnStatementExpressions.isEmpty())
        return InferenceResult(classes = setOf(VOID.type))
    val types = getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
    if (types.toClassList().isEmpty())
        return INFERRED_ANY_TYPE
    LOGGER.info("Got function dec types for function ${function.functionNameAsString} : ${types.toClassListString()}")
    return types
}

internal fun getFunctionForVariableName(variableName:ObjJVariableName) : ObjJFunctionDeclarationElement<*>? {
    ProgressManager.checkCanceled()
    if (variableName.parent is ObjJGlobalVariableDeclaration)
        return (variableName.parent as ObjJGlobalVariableDeclaration).expr?.leftExpr?.getChildOfType(ObjJFunctionDeclarationElement::class.java)
    val usages = ReferencesSearch.search(variableName)
            .findAll()
            .map { it.element } + variableName

    val assignments = usages.mapNotNull{ getAssignedExpressions(it)?.leftExpr?.getChildOfType(ObjJFunctionDeclarationElement::class.java)}
    return assignments.getOrNull(0)

}

private fun getAssignedExpressions(element: PsiElement?) : ObjJExpr? {
    ProgressManager.checkCanceled()
    return if (element == null || element !is ObjJVariableName)
        null
    else if (element.parent is ObjJGlobalVariableDeclaration)
        (element.parent as ObjJGlobalVariableDeclaration).expr
    else if (element.parent !is ObjJQualifiedReference)
        null
    else if (element.parent.parent is ObjJVariableDeclaration)
        (element.parent.parent as ObjJVariableDeclaration).expr
    else
        null
}


fun ObjJFunctionDeclarationElement<*>.toJsFunctionType(tag:Long) : JsFunctionType {
    val returnTypes = inferFunctionDeclarationReturnType(this, tag) ?: INFERRED_ANY_TYPE
    return JsFunctionType(this.parameterTypes(), returnTypes)
}

fun ObjJFunctionDeclarationElement<*>.toJsFunctionTypeResult(tag:Long) : InferenceResult? {
    val functionType = toJsFunctionType(tag)
    return InferenceResult(
            functionTypes = listOf(functionType)
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
        LOGGER.info("Parameter Name is $parameterName")
        if (i < commentWrapper?.parameterComments?.size.orElse(0)) {
            val parameterType = commentWrapper?.parameterComments
                    ?.get(i)
                    ?.types
            out[parameterName] = if (parameterType != null) InferenceResult(classes = parameterType.toSet())  else INFERRED_ANY_TYPE
        } else {
            out[parameterName] = INFERRED_ANY_TYPE
        }
    }
    return out
}


internal val PsiElement.parentFunctionDeclaration
    get() = ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this)
