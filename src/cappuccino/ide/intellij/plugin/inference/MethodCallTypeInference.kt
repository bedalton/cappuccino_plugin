package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.containingSuperClassName
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import kotlin.math.min

internal fun inferMethodCallType(methodCall:ObjJMethodCall, level:Int, tag:Long) : InferenceResult? {
    return methodCall.getCachedInferredTypes {
        if (methodCall.tagged(tag))
            return@getCachedInferredTypes null
        LOGGER.info("Method call <${methodCall.text}> inference type not cached")
        internalInferMethodCallType(methodCall, level - 1, tag)
    }
}

private fun internalInferMethodCallType(methodCall:ObjJMethodCall, level:Int, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        LOGGER.info("Checking alloc statement for calltarget <${methodCall.callTargetText}>")
        return getAllocStatementType(methodCall)
    }
    val callTargetTypes = getCallTargetTypes(methodCall.callTarget, level, tag)
    val methods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
        if (callTargetTypes.isNotEmpty()) {
            LOGGER.info("Gathering all matching methods by class name in [$callTargetTypes]")
            ObjJUnifiedMethodIndex.instance[selector, project].filter {
                it.containingClassName in callTargetTypes
            }
        } else {
            LOGGER.info("Gathering all matching methods without class constraints")
            ObjJUnifiedMethodIndex.instance[selector, project]
        }
    } else
        emptyList()
    val methodDeclarations = methods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val returnTypes = methodDeclarations.flatMap { methodDeclaration ->
        getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration, level, tag)
    }.toSet()
    return InferenceResult(classes = returnTypes)
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
    LOGGER.info("Method Call is alloc statement for: <$className>")
    return InferenceResult(
            classes = setOf(className)
    )
}

private fun getCallTargetTypes(callTarget: ObjJCallTarget, level:Int, tag:Long) : Set<String> {
    /*if (level < 0)
        return emptySet()*/
    return callTarget.getCachedInferredTypes {
        LOGGER.info("Call target <${callTarget.text}>'s types were not cached")
        inferCallTargetType(callTarget, level - 1, tag)
    }?.classes.orEmpty()
}

private fun inferCallTargetType(callTarget:ObjJCallTarget, level:Int, tag: Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, level, tag)

    if (callTarget.functionCall != null) {
        return inferFunctionCallReturnType(callTarget.functionCall!!, level, tag)
    }
    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, level, tag)
    }
    return null
}

private fun getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration:ObjJMethodDeclaration, level:Int, tag: Long) : Iterable<String> {
    ProgressManager.checkCanceled()
    val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
    if (simpleReturnType != "id") {
        val type = simpleReturnType.stripRefSuffixes()
        return listOf(type)
    } else {
        var out = InferenceResult()
        val expressions = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
        val selfExpressionTypes = expressions.filter { it.text == "self"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName)}
        val superExpressionTypes = expressions.filter { it.text == "super"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text)}
        val simpleOut = selfExpressionTypes + superExpressionTypes
        if (simpleOut.isNotEmpty()) {
            return simpleOut
        }
        expressions.forEach {
            //LOGGER.info("Checking return statement <${it.text ?: "_"}> for method call : <${methodCall.text}>")
            val type = inferExpressionType(it, min(level - 1, 3), tag)
            if (type != null)
                out += type
        }
        return out.classes
    }
}