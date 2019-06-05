package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.VOID
import cappuccino.ide.intellij.plugin.contributor.allGlobalJsClassFunctions
import cappuccino.ide.intellij.plugin.contributor.globalJsFunctions
import cappuccino.ide.intellij.plugin.contributor.returnTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement

internal fun inferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    return functionCall.getCachedInferredTypes {
        if (functionCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    val resolve = functionCall.functionName?.reference?.resolve() as? ObjJCompositeElement
    if (resolve == null) {
        val functionName = functionCall.functionName?.text
        if (functionName != null) {
            val functionSet = if (functionCall.indexInQualifiedReference == 0) {
                globalJsFunctions
            } else
                allGlobalJsClassFunctions
            val out = functionSet
                    .filter {
                        it.name == functionName
                    }.flatMap {
                        it.returnTypes
                    }.toSet()
            if (out.isNotEmpty()) {
                return InferenceResult(classes = out)
            }
        }
        return null
    }
    return resolve.getCachedInferredTypes {
        val functionAsVariableName = resolve as? ObjJVariableName
        val function = (when {
            functionAsVariableName != null -> functionAsVariableName.parentFunctionDeclaration
            resolve is ObjJFunctionName -> resolve.parentFunctionDeclaration
            else -> null
        })
        if (function == null) {
            val expression = functionAsVariableName?.getAssignmentExprOrNull() ?: return@getCachedInferredTypes null
            return@getCachedInferredTypes inferExpressionType(expression, tag)?.functionTypes?.firstOrNull()?.returnType
        }
        inferFunctionDeclarationReturnType(function, tag)
    }
}

fun inferFunctionDeclarationReturnType(function:ObjJFunctionDeclarationElement<*>, tag:Long) : InferenceResult? {
    val commentReturnTypes = function.docComment?.getReturnTypes(function.project)
    if (commentReturnTypes.isNotNullOrEmpty())
        return InferenceResult(classes = commentReturnTypes!!)
    val returnStatementExpressions = function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    if (returnStatementExpressions.isEmpty())
        return InferenceResult(classes = setOf(VOID.type))
    val types = getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
    if (types.toClassList().isEmpty())
        return INFERRED_ANY_TYPE
    return types
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
        if (i < commentWrapper?.parameterComments?.size.orElse(0)) {
            val parameterType = commentWrapper?.parameterComments
                    ?.get(i)
                    ?.getTypes(project)
            out[parameterName] = if (parameterType != null) InferenceResult(classes = parameterType.toSet())  else INFERRED_ANY_TYPE
        } else {
            out[parameterName] = INFERRED_ANY_TYPE
        }
    }
    return out
}


internal val PsiElement.parentFunctionDeclaration
    get() = ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this)
