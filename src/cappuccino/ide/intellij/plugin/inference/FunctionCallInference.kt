package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement

internal fun inferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Tag): InferenceResult? {
    return functionCall.getCachedInferredTypes(tag) {
        if (functionCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferFunctionCallReturnType(functionCall, tag)
    }
}

internal fun internalInferFunctionCallReturnType(functionCall: ObjJFunctionCall, tag: Tag): InferenceResult? {
    ProgressIndicatorProvider.checkCanceled()
    val functionName = functionCall.functionName ?: return null
    val functionNameString = functionName.text ?: return null
    if (functionNameString == "apply") {
        val thisIndex = functionName.indexInQualifiedReference
        val parentQualifiedReference = functionName.getParentOfType(ObjJQualifiedReference::class.java)
        if (parentQualifiedReference != null && thisIndex > 0) {
            val siblings = parentQualifiedReference.qualifiedNameParts.subList(0, thisIndex - 1)
            val types = inferQualifiedReferenceType(siblings, tag)
                    ?.functionTypes
                    ?.mapNotNull {
                        it.returnType
                    }
                    ?.combine()
            if (types?.withoutAnyType()?.isNotEmpty().orFalse())
                return types
        }
    }
    // Get Type from JsTypeDef if possible
    val functionSet = JsTypeDefFunctionsByNameIndex.instance[functionNameString, functionCall.project]
    var lastOut = functionSet.flatMap {
        it.getReturnTypes(tag)?.types.orEmpty()
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
                it.getReturnTypes(tag)?.types.orEmpty()
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
                ?: if (!resolved.isEquivalentTo(functionName)) (resolved as? ObjJFunctionName)?.parentFunctionDeclaration?.getReturnTypes(tag) else null
                        ?: (resolved as? ObjJFunctionDeclarationElement<*>)?.getCachedReturnType(tag)
        if (cached != null) {
            return cached
        }
        resolved.getCachedInferredTypes(tag) {
            inferTypeForResolved(resolved, tag)
        }
    }.combine()
    if (out.types.isNotEmpty()) {
        return out
    }
    return null

}

private fun inferTypeForResolved(resolved: PsiElement, tag: Tag): InferenceResult? {
    if (resolved.tagged(tag))
        return null
    ProgressIndicatorProvider.checkCanceled()
    val function: ObjJUniversalFunctionElement? = when (resolved) {
        is ObjJVariableName -> resolved.parentFunctionDeclaration
        is ObjJFunctionName -> resolved.parentFunctionDeclaration
        else ->  null
    }
    return when {
        function != null -> {
            inferFunctionDeclarationReturnType(function, tag)
        }
        resolved is JsTypeDefElement -> {
            inferWhenResolvedIsJsTypeDefElement(resolved)
        }
        else -> {
            val expression = (resolved as? ObjJVariableName)
                    ?.getAssignmentExprOrNull()
                    ?: return null
            inferExpressionType(expression, tag)?.functionTypes?.firstOrNull()?.returnType
        }
    }
}

private fun inferWhenResolvedIsJsTypeDefElement(resolved: PsiElement): InferenceResult? {
    val parent = resolved.parent
    return when (resolved) {
        is JsTypeDefFunction -> resolved.getReturnTypes(createTag())
        is JsTypeDefFunctionName -> (parent as? JsTypeDefFunction)?.getReturnTypes(createTag())
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
}

private val hasReturnCallRegex = "(^return|\\s+return)\\s+[a-zA-Z0-9\$_]".toRegex();
fun inferFunctionDeclarationReturnType(function: ObjJUniversalFunctionElement, tag: Tag): InferenceResult? {
    val commentReturnTypes = function.docComment?.getReturnTypes()
    if (commentReturnTypes != null)
        return commentReturnTypes
    if (function is JsTypeDefFunction) {
        val types = function.getReturnTypes(tag)?.types.orEmpty()
        if (types.isEmpty())
            return null
        return InferenceResult(types = types)
    }
    if (!ObjJPluginSettings.inferFunctionReturnTypeFromReturnStatements())
        return if (function.text.contains(hasReturnCallRegex)) INFERRED_ANY_TYPE else INFERRED_VOID_TYPE

    val returnStatementExpressions = if (function is ObjJFunctionDeclarationElement<*>)
        function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    else
        emptyList()
    if (returnStatementExpressions.isEmpty())
        return INFERRED_VOID_TYPE

    val types =
            getInferredTypeFromExpressionArray(returnStatementExpressions, tag)
    if (types.toClassList().isEmpty())
        return INFERRED_ANY_TYPE
    return types
}

internal fun ObjJFunctionDeclarationElement<*>.toJsFunctionTypeResult(tag: Tag): InferenceResult? {
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
        val comment = commentWrapper?.getParameterComment(parameterName)
                ?: commentWrapper?.getParameterComment(i)
        if (comment != null) {
            val property = JsTypeDefFunctionArgument(
                    name = parameterName,
                    types = comment.types ?: INFERRED_ANY_TYPE
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
                ?: this.reference?.resolve()?.let {
                    if (this != it)
                        it.parentFunctionDeclaration
                    else
                        null
                }
    }


internal val PsiElement.parentFunctionDeclarationNoCache: ObjJUniversalFunctionElement?
    get() {
        return (this as? ObjJFunctionCall)?.functionName?.resolve()?.parentFunctionDeclaration
                ?: (this as? JsTypeDefFunctionName)?.getParentOfType(JsTypeDefFunction::class.java)
                ?: (this as? JsTypeDefPropertyName)?.getParentOfType(JsTypeDefProperty::class.java)?.typeList?.firstOrNull { it.anonymousFunction != null } as? JsTypeDefAnonymousFunction
                ?: this.reference?.resolve()?.let {
                    if (this != it)
                        it.parentFunctionDeclaration
                    else
                        null
                }
    }

val PsiElement.directParentFunctionElement: ObjJFunctionDeclarationElement<*>?
    get() {
        val parent: ObjJFunctionDeclarationElement<*>? = this.getSelfOrParentOfType(ObjJFunctionDeclarationElement::class.java)
        if (parent != null) {
            return parent;
        }
        val expr = this.getParentOfType(ObjJExpr::class.java)?.leftExpr
                ?: return null
        return expr.functionDeclaration ?: expr.functionLiteral
    }