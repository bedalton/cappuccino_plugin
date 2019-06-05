package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.containingSuperClassName
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService

internal fun inferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    return methodCall.getCachedInferredTypes {
        if (methodCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferMethodCallType(methodCall, tag)
    }
}

private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        return getAllocStatementType(methodCall)
    }
    val callTargetTypes = getCallTargetTypes(methodCall.callTarget, tag)
    val methods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
        if (callTargetTypes.isNotEmpty()) {
            ObjJUnifiedMethodIndex.instance[selector, project].filter {
                it.containingClassName in callTargetTypes
            }
        } else {
            ObjJUnifiedMethodIndex.instance[selector, project]
        }
    } else
        emptyList()
    val methodDeclarations = methods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val returnTypes = methodDeclarations.flatMap { methodDeclaration ->
        methodDeclaration.getCachedInferredTypes {
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
    val out = if (returnTypes.isNotEmpty() && instanceVariableTypes.isNotEmpty())
        returnTypes + instanceVariableTypes
    else if (returnTypes.isNotEmpty())
        returnTypes
    else
        instanceVariableTypes
    return InferenceResult(classes = out.toSet())
}

private fun getAllocStatementType(methodCall: ObjJMethodCall) : InferenceResult? {
    val callTargetText = methodCall.callTargetText
    val className = if (callTargetText == "super") {
        methodCall.callTarget
                .getParentOfType(ObjJHasContainingClass::class.java)
                ?.containingSuperClassName
                ?: callTargetText
    } else {
        callTargetText
    }
    val isValidClass = className in ObjJImplementationDeclarationsIndex.instance.getAllKeys(methodCall.project)
    if (!isValidClass)
        return null
    return InferenceResult(
            classes = setOf(className)
    )
}

private fun getCallTargetTypes(callTarget: ObjJCallTarget, tag:Long) : Set<String> {
    /*if (level < 0)
        return emptySet()*/
    return callTarget.getCachedInferredTypes {
        inferCallTargetType(callTarget, tag)
    }?.classes.orEmpty()
}

private fun inferCallTargetType(callTarget:ObjJCallTarget, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
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
    ProgressManager.checkCanceled()
    val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
    if (simpleReturnType != "id") {
        val type = simpleReturnType.stripRefSuffixes()
        return setOf(type)
    } else {
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