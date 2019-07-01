package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefPropertyName
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.psi.PsiElement

internal fun inferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Long): InferenceResult? {
    return functionCall.getCachedInferredTypes(tag) {
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Long): InferenceResult? {
    val out = functionCall.functionName?.reference?.multiResolve(false)?.mapNotNull {
        val resolved = it.element ?: return@mapNotNull null
        val cached = (resolved as? ObjJFunctionName)?.getCachedReturnType(tag)
                ?: (resolved as? ObjJVariableName)?.getClassTypes(tag)
                ?: (resolved as? ObjJFunctionDeclarationElement<*>)?.getCachedReturnType(tag)
        if (cached != null)
            return cached
        resolved.getCachedInferredTypes(tag) {
            val function: ObjJFunctionDeclarationElement<*>? = (when (resolved) {
                is ObjJVariableName -> resolved.parentFunctionDeclaration
                is ObjJFunctionName -> resolved.parentFunctionDeclaration
                else -> null
            })
            when {
                function != null -> {
                    inferFunctionDeclarationReturnType(function, tag)
                }
                resolved is JsTypeDefElement -> {
                    val parent = resolved.parent

                    val out = when (resolved) {
                        is JsTypeDefFunction -> resolved.functionReturnType?.toTypeListType()
                        is JsTypeDefFunctionName -> (parent as? JsTypeDefFunction)?.functionReturnType?.toTypeListType()
                        is JsTypeDefProperty -> resolved.toJsNamedProperty().types
                        is JsTypeDefPropertyName -> (parent as? JsTypeDefProperty)?.toJsNamedProperty()?.types
                        else -> {
                            null
                        }
                    }
                    out
                }
                else -> {
                    val expression = (resolved as? ObjJVariableName)?.getAssignmentExprOrNull()
                            ?: return@getCachedInferredTypes null
                    inferExpressionType(expression, tag)?.functionTypes?.firstOrNull()?.returnType
                }
            }
        }
    }.orEmpty().combine()
    if (out.types.isNotEmpty()) {
        return out
    }
    val functionName = functionCall.functionName?.text
    if (functionName != null) {
        val functionSet = JsTypeDefFunctionsByNameIndex.instance[functionName, functionCall.project]
        val lastOut = functionSet.flatMap {
            it.functionReturnType?.typeList?.toJsTypeDefTypeListTypes() ?: emptySet()
        }.toSet()
        if (lastOut.isNotEmpty()) {
            return InferenceResult(types = lastOut)
        }
    }
    return null

}

fun inferFunctionDeclarationReturnType(function: ObjJFunctionDeclarationElement<*>, tag: Long): InferenceResult? {
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

fun ObjJFunctionDeclarationElement<*>.toJsFunctionType(tag: Long): JsTypeListFunctionType {
    val returnTypes = inferFunctionDeclarationReturnType(this, tag) ?: INFERRED_ANY_TYPE
    return JsTypeListFunctionType(
            name = this.functionNameString,
            parameters = this.parameterTypes(),
            returnType = returnTypes,
            comment = docComment?.commentText,
            static = true
    )
}

fun ObjJFunctionDeclarationElement<*>.toJsFunctionTypeResult(tag: Long): InferenceResult? {
    val functionType = toJsFunctionType(tag)
    return InferenceResult(
            types = setOf(functionType)
    )
}

private fun ObjJFunctionDeclarationElement<*>.parameterTypes(): List<JsTypeDefFunctionArgument> {
    //ProgressManager.checkCanceled()
    val parameters = formalParameterArgList
    val out = mutableListOf<JsTypeDefFunctionArgument>()
    val commentWrapper = this.docComment
    for ((i, parameter) in parameters.withIndex()) {
        //ProgressManager.checkCanceled()
        val parameterName = parameter.variableName?.text ?: "$i"
        if (i < commentWrapper?.parameterComments?.size.orElse(0)) {
            val comment = commentWrapper?.parameterComments
                    ?.get(i)
            val types = comment?.getTypes(project)
            val property = JsTypeDefFunctionArgument(
                    name = parameterName,
                    types = InferenceResult(types = types?.toJsTypeList().orEmpty())
            )
            out.add(property)
        } else {
            out.add(JsTypeDefFunctionArgument(
                    name = parameterName,
                    types = INFERRED_ANY_TYPE
            ))
        }
    }
    return out
}


internal val PsiElement.parentFunctionDeclaration: ObjJFunctionDeclarationElement<*>?
    get() {
        return (this as? ObjJFunctionName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJVariableName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJFunctionCall)?.functionName?.resolve()?.parentFunctionDeclaration
                ?: ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this.reference?.resolve())
    }
