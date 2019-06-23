package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.containingSuperClassName
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getCallTargetText
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService

internal fun inferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    return methodCall.getCachedInferredTypes(tag) {
        internalInferMethodCallType(methodCall, tag)
    }
}

// MUST BE FALSE
// Causes infinite recursion
private val CLASS_METHOD_BASED_RESOLVE = false
/*
private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    //ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        return getAllocStatementType(methodCall)
    }
    if (selector == "respondsToSelector:" || selector == "respondsToSelector")
        return listOf("BOOL").toInferenceResult()

    val callTargetType = inferCallTargetType(methodCall.callTarget, tag)
    val callTargetTypes = callTargetType?.toClassList(null)?.withoutAnyType().orEmpty()

    if (selector == "copy" || selector == "copy:")
        return callTargetType

    if (callTargetTypes.isEmpty())
        return INFERRED_ANY_TYPE

    if (CLASS_METHOD_BASED_RESOLVE) {
        var out = callTargetTypes.mapNotNull {className ->
            ObjJImplementationDeclarationsIndex.instance[className, project].firstOrNull { !it.isCategory} ?.getMethodReturnType(selector, tag)
                    ?:ObjJImplementationDeclarationsIndex.instance[className, project].firstOrNull()?.getMethodReturnType(selector, tag)
        }
        if (out.isEmpty()) {
            out = callTargetTypes.mapNotNull {
                ObjJProtocolDeclarationsIndex.instance[it, project].firstOrNull()?.getMethodReturnType(selector, tag)
            }
        }
        return out.collapse()
    } else {

        val methods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
            if (callTargetTypes.isNotEmpty()) {
                return callTargetTypes.flatMap { className ->
                    ObjJClassAndSelectorMethodIndex.instance.getByClassAndSelector(className, selector, project).mapNotNull {
                        it.getCachedReturnType(tag)
                    }
                }.collapse()
            }
            ObjJUnifiedMethodIndex.instance[selector, project]
        } else
            emptyList()
        return methods.toSet().mapNotNull {
            it.getCachedReturnType(tag)
        }.collapse()
    }
}
*/
private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    //ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        return getAllocStatementType(methodCall)
    }
    val callTargetType = inferCallTargetType(methodCall.callTarget, tag)
    val callTargetTypes = callTargetType?.classes.orEmpty()
    if (selector == "copy" || selector == "copy:" || selector == "new:" || selector == "new:")
        return callTargetType
    val getMethods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
        if (callTargetTypes.isNotEmpty()) {
            ObjJUnifiedMethodIndex.instance[selector, project].filter {
                it.containingClassName in callTargetTypes
            }
        } else {
            ObjJUnifiedMethodIndex.instance[selector, project]
        }
    } else
        emptyList()
    val methodDeclarations = getMethods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val getReturnType = methodDeclarations.flatMap { methodDeclaration ->
        methodDeclaration.getCachedInferredTypes(tag) {
            if (methodDeclaration.tagged(tag))
                return@getCachedInferredTypes null
            val commentReturnTypes = methodDeclaration.docComment?.getReturnTypes(methodDeclaration.project).orEmpty().withoutAnyType()
            if (commentReturnTypes.isNotEmpty()) {
                return@getCachedInferredTypes commentReturnTypes.toInferenceResult()
            }
            val thisClasses = getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration, tag)
            if (thisClasses.isNotEmpty()) {
                InferenceResult(
                        classes = thisClasses
                )
            } else
                null
        }?.classes.orEmpty()
    }
    val instanceVariableTypes = callTargetTypes.flatMap {className ->
        ObjJInstanceVariablesByClassIndex.instance[className, project].filter{ it.variableName?.text == selector}.mapNotNull {
            val type = it.variableType
            if (type.isNotBlank())
                type
            else
                null
        }
    }
    val out = if (getReturnType.isNotEmpty() && instanceVariableTypes.isNotEmpty())
        getReturnType + instanceVariableTypes
    else if (getReturnType.isNotEmpty())
        getReturnType
    else
        instanceVariableTypes
    return InferenceResult(classes = out.toSet())
}

private fun getAllocStatementType(methodCall: ObjJMethodCall) : InferenceResult? {
    val className = when (val callTargetText = methodCall.callTargetText) {
        "super" -> methodCall
                .getParentOfType(ObjJHasContainingClass::class.java)
                ?.containingSuperClassName
                ?: callTargetText
        "self" -> methodCall
                .getParentOfType(ObjJHasContainingClass::class.java)
                ?.containingClassName
                ?: callTargetText
        else -> callTargetText
    }
    val isValidClass = ObjJImplementationDeclarationsIndex.instance.containsKey(className, methodCall.project)
    if (!isValidClass)
        return null
    return InferenceResult(
            classes = setOf(className)
    )
}

fun inferCallTargetType(callTarget: ObjJCallTarget, tag:Long) : InferenceResult? {
    /*if (level < 0)
        return emptySet()*/
    return callTarget.getCachedInferredTypes(tag) {
        internalInferCallTargetType(callTarget, tag)
    }
}

private fun internalInferCallTargetType(callTarget:ObjJCallTarget, tag:Long) : InferenceResult? {
    val callTargetText = callTarget.text
    if (ObjJClassDeclarationsIndex.instance.containsKey(callTargetText, callTarget.project))
        return setOf(callTargetText).toInferenceResult()
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, tag)

    if (callTarget.functionCall != null) {
        return inferFunctionCallReturnType(callTarget.functionCall!!, tag)
    }
    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, tag)
    }
    return null
}

private fun getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration:ObjJMethodDeclaration, tag:Long) : Set<String> {
    //ProgressManager.checkCanceled()
    val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
    if (simpleReturnType != "id") {
        val type = simpleReturnType.stripRefSuffixes()
        return setOf(type)
    } else {
        //var out = methodDeclaration.methodHeader.getCachedReturnType(tag)
        //if (out != null)
         //   return out.classes
        var out = INFERRED_EMPTY_TYPE
        val expressions = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
        val selfExpressionTypes = expressions.filter { it.text == "self"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName)}
        val superExpressionTypes = expressions.filter { it.text == "super"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text)}
        val simpleOut = selfExpressionTypes + superExpressionTypes
        if (simpleOut.isNotEmpty()) {
            return simpleOut.toSet()
        }
        expressions.forEach {
            //LOGGER.info("Checking return statement <${it.text ?: "_"}> for method call : <${methodCall.text}>")
            val type = inferExpressionType(it, tag)
            if (type != null)
                out += type
        }
        return out.classes
    }
}