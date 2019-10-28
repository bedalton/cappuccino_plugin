package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.psi.PsiElement

internal fun inferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Long): InferenceResult? {
    return functionCall.getCachedInferredTypes(tag) {
        if (functionCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Long): InferenceResult? {
    val functionName = functionCall.functionName ?: return null
    val functionNameString = functionName.text ?: return null

    // Get Type from JsTypeDef if possible
    val functionSet = JsTypeDefFunctionsByNameIndex.instance[functionNameString, functionCall.project]
    var lastOut = functionSet.flatMap {
        it.functionReturnType?.typeList?.toJsTypeDefTypeListTypes() ?: emptySet()
    }.toSet()
    if (JsTypeDefClassesByNamespaceIndex.instance.containsKey(functionNameString, functionCall.project))
        lastOut = lastOut + JsTypeListType.JsTypeListBasicType(functionNameString)
    if (lastOut.isNotEmpty()) {
        return InferenceResult(types = lastOut)
    }

    // Resolve according to reference
    // If resolved to objj function, parse objects as necessary
    val resolvesRaw = functionName.reference.multiResolve(false).orEmpty()
    val resolves = resolvesRaw.map { it.element }
    val jsTypeDefResolves = resolves.mapNotNull { it as? JsTypeDefElement }
    val typeDefOut = jsTypeDefResolves
            .mapNotNull {
                (it as? JsTypeDefFunction) ?: ((it as? JsTypeDefFunctionName)?.parent as? JsTypeDefFunction)
            }.flatMap {
                it.functionReturnType?.toTypeListType()?.toJsTypeList().orEmpty()
            } +
            jsTypeDefResolves.mapNotNull {
                it as? JsTypeDefProperty ?: it.parent as? JsTypeDefProperty
                ?: (it as? JsTypeDefPropertyName)?.getParentOfType(JsTypeDefProperty::class.java)
            }.flatMap {
                it.typeList.filterIsInstance(JsTypeListFunctionType::class.java).flatMap { it.returnType?.types.orEmpty() }.toSet()
            }
    if (typeDefOut.isNotEmpty()) {
        return InferenceResult(types = typeDefOut.toSet())
    }

    val out = resolvesRaw.mapNotNull { referenceResult ->
        val resolved = referenceResult.element ?: return@mapNotNull null
        val cached = (resolved as? ObjJFunctionName)?.getCachedReturnType(tag)
                ?: (resolved as? ObjJVariableName)?.getClassTypes(tag)?.functionTypes?.flatMap { it.returnType?.types.orEmpty() }?.toSet()?.let {
                    InferenceResult(types = it)
                }
                ?: (resolved as? ObjJFunctionDeclarationElement<*>)?.getCachedReturnType(tag)
        if (cached != null) {
            return cached
        }
        resolved.getCachedInferredTypes(tag) {
            if (resolved.tagged(tag))
                return@getCachedInferredTypes null
            val function: ObjJUniversalFunctionElement? = (when (resolved) {
                is ObjJVariableName -> resolved.parentFunctionDeclaration
                is ObjJFunctionName -> resolved.parentFunctionDeclaration
                else -> {
                    null
                }
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
                        is JsTypeDefProperty -> resolved.toJsNamedProperty().types.functionTypes.flatMap { it.returnType?.types.orEmpty() }.ifEmpty { null }?.let {
                            InferenceResult(types = it.toSet())
                        }
                        is JsTypeDefPropertyName -> (parent as? JsTypeDefProperty)?.toJsNamedProperty()?.types?.functionTypes?.flatMap { it.returnType?.types.orEmpty() }?.ifEmpty { null }?.let {
                            InferenceResult(types = it.toSet())
                        }
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
    return null

}

fun inferFunctionDeclarationReturnType(function: ObjJUniversalFunctionElement, tag: Long): InferenceResult? {
    val commentReturnTypes = function.docComment?.getReturnTypes(function.project)
    if (commentReturnTypes.isNotNullOrEmpty())
        return InferenceResult(types = commentReturnTypes!!.toJsTypeList())
    if (function is JsTypeDefFunction) {
        val types = function.functionReturnType?.typeList.toJsTypeDefTypeListTypes()
        if (types.isEmpty())
            return null
        return InferenceResult(types = types)
    }
    val returnStatementExpressions = if (function is ObjJFunctionDeclarationElement<*>)
        function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    else
        emptyList()
    if (returnStatementExpressions.isEmpty())
        return INFERRED_VOID_TYPE
    val types = getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
    if (types.toClassList().isEmpty())
        return INFERRED_ANY_TYPE
    return types
}

fun ObjJFunctionDeclarationElement<*>.toJsFunctionTypeResult(tag: Long): InferenceResult? {
    val functionType = toJsFunctionType(tag)
    return InferenceResult(
            types = setOf(functionType)
    )
}

fun ObjJFunctionDeclarationElement<*>.parameterTypes(): List<JsTypeDefFunctionArgument> {
    //ProgressManager.checkCanceled()
    val parameters = formalParameterArgList
    val out = mutableListOf<JsTypeDefFunctionArgument>()
    val commentWrapper = this.docComment
    for ((i, parameter) in parameters.withIndex()) {
        //ProgressManager.checkCanceled()
        val parameterName = parameter.variableName?.text ?: "$i"
        val comment = commentWrapper?.parameterComments?.firstOrNull{ it.paramName == parameterName } ?: commentWrapper?.parameterComments
                ?.getOrNull(i)
        if (comment != null) {
            val types = comment.getTypes(project)
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


internal val PsiElement.parentFunctionDeclaration: ObjJUniversalFunctionElement?
    get() {
        return (this as? ObjJFunctionName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJVariableName)?.cachedParentFunctionDeclaration
                ?: (this as? ObjJFunctionCall)?.functionName?.resolve()?.parentFunctionDeclaration
                ?: (this as? JsTypeDefFunctionName)?.getParentOfType(JsTypeDefFunction::class.java)
                ?: (this as? JsTypeDefPropertyName)?.getParentOfType(JsTypeDefProperty::class.java)?.typeList?.firstOrNull { it.anonymousFunction != null } as? JsTypeDefAnonymousFunction
                ?: ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this.reference?.resolve())
    }
