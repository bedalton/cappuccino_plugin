package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsFunction
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.psi.PsiElement

internal fun inferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    return functionCall.getCachedInferredTypes(tag) {
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall:ObjJFunctionCall, tag:Long) : InferenceResult? {
    val resolved = functionCall.functionName?.reference?.resolve() as? ObjJCompositeElement
    if (resolved == null) {
        val functionName = functionCall.functionName?.text
        if (functionName != null) {
            val functionSet = JsTypeDefFunctionsByNameIndex.instance[functionName, functionCall.project]
            val out = functionSet.flatMap {
                        it.functionReturnType?.typeList?.toJsTypeDefTypeListTypes() ?: emptyList()
                    }.toSet()
            if (out.isNotEmpty()) {
                return InferenceResult(types = out )
            }
        }
        return null
    }
    val cached = (resolved as? ObjJFunctionName)?.getCachedReturnType(tag)
            ?: (resolved as? ObjJVariableName)?.getClassTypes(tag)
            ?: (resolved as? ObjJFunctionDeclarationElement<*>)?.getCachedReturnType(tag)
    if (cached != null)
        return cached
    return resolved.getCachedInferredTypes(tag) {
        val functionAsVariableName = resolved as? ObjJVariableName
        val function:ObjJFunctionDeclarationElement<*>? = (when {
            functionAsVariableName != null -> functionAsVariableName.parentFunctionDeclaration
            resolved is ObjJFunctionName -> resolved.parentFunctionDeclaration
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
        return InferenceResult(types = commentReturnTypes!!.toJsTypeList())
    val returnStatementExpressions = function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    if (returnStatementExpressions.isEmpty())
        return INFERRED_VOID_TYPE
    val types = getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
    if (types.toClassList().isEmpty())
        return INFERRED_ANY_TYPE
    return types
}

fun ObjJFunctionDeclarationElement<*>.toJsFunctionType(tag:Long) : JsFunction {
    val returnTypes = inferFunctionDeclarationReturnType(this, tag) ?: INFERRED_ANY_TYPE
    return JsFunction(name = this.functionNameAsString, parameters = this.parameterTypes(), returnType = returnTypes)
}

fun ObjJFunctionDeclarationElement<*>.toJsFunctionTypeResult(tag:Long) : InferenceResult? {
    val functionType = toJsFunctionType(tag).toJsTypeListType()
    return InferenceResult(
            types = setOf(functionType)
    )
}

private fun ObjJFunctionDeclarationElement<*>.parameterTypes() : List<JsTypeDefNamedProperty> {
    //ProgressManager.checkCanceled()
    val parameters = formalParameterArgList
    val out = mutableListOf<JsTypeDefNamedProperty>()
    val commentWrapper = this.docComment
    for ((i, parameter) in parameters.withIndex()) {
        //ProgressManager.checkCanceled()
        val parameterName = parameter.variableName?.text ?: "$i"
        if (i < commentWrapper?.parameterComments?.size.orElse(0)) {
            val comment = commentWrapper?.parameterComments
                    ?.get(i)
            val types = comment?.getTypes(project)
            val property = JsTypeDefNamedProperty(
                    name = parameterName,
                    types = InferenceResult(types = types?.toJsTypeList().orEmpty())
            )
            out.add(property)
        } else {
            out.add(JsTypeDefNamedProperty(
                    name = parameterName,
                    types = INFERRED_ANY_TYPE
            ))
        }
    }
    return out
}


internal val PsiElement.parentFunctionDeclaration:ObjJFunctionDeclarationElement<*>?
    get() {
        return (this as? ObjJFunctionName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJVariableName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJFunctionCall)?.functionName?.resolve()?.parentFunctionDeclaration
                ?: ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this.reference?.resolve())
    }
