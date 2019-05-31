package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.globalJsFunctions
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
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
    val resolve = functionCall.reference?.resolve()
    if (resolve == null) {
        val functionName = functionCall.functionName?.text
        if (functionName != null) {
            val out = globalJsFunctions
                    .filter {
                        it.name == functionName
                    }.flatMap {
                        it.returns?.type?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) ?: emptyList()
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
        functionAsVariableName != null -> getFunctionForVariableName(functionAsVariableName)
        resolve is ObjJFunctionName -> resolve.getParentOfType(ObjJFunctionDeclarationElement::class.java)
        else -> null
    })
    if (function == null) {
        LOGGER.info("Failed to find function for function name element")
        return null
    }
    return inferFunctionDeclarationReturnType(function, tag)
}

internal fun inferFunctionDeclarationReturnType(function:ObjJFunctionDeclarationElement<*>, tag:Long) : InferenceResult? {
    val commentReturnValue = function.docComment?.returnParameterComment
    if (commentReturnValue != null)
        return InferenceResult(classes = commentReturnValue.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toSet())
    val returnStatementExpressions = function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    return getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
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