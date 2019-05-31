package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import kotlin.math.min

internal fun inferMethodCallType(methodCall:ObjJMethodCall, level:Int) : InferenceResult? {
    val cachedTypes = methodCall.getUserData(INFERENCE_TYPES_USER_DATA_KEY)
    if (cachedTypes != null && cachedTypes.toClassList().isNotEmpty())
        return cachedTypes
    val out = internalInferMethodCallType(methodCall, level)
    if (out != null && out.toClassList().isNotEmpty())
        methodCall.putUserData(INFERENCE_TYPES_USER_DATA_KEY, out)
    return out
}

private fun internalInferMethodCallType(methodCall:ObjJMethodCall, level:Int) : InferenceResult? {


    ProgressManager.checkCanceled()
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        //LOGGER.info("Method Call is alloc statement for: <${methodCall.callTargetText}>")
        val classes = setOf(methodCall.callTargetText)
        return if (classes.isNotEmpty()) {
            //LOGGER.info("Method Call is alloc statement is for types: <${classes}>")
            InferenceResult(
                    classes = classes
            )
        } else {
            //LOGGER.info("Inheritance util failed to find class for name>")
            InferenceResult(
                    classes = setOf(methodCall.callTargetText)
            )
        }
    }
    val callTargetTypes = getCallTargetTypes(methodCall.callTarget, level)

    val methods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
        if (callTargetTypes.isNotEmpty()) {
            val classes = callTargetTypes.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) }
            ObjJUnifiedMethodIndex.instance[selector, project].filter {
                it.containingClassName in classes
            }
        } else {
            ObjJUnifiedMethodIndex.instance[selector, project]
        }
    } else
        emptyList()
    val methodDeclarations = methods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val returnTypes = methodDeclarations.flatMap { methodDeclaration ->

        ProgressManager.checkCanceled()
        val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
        if (simpleReturnType != "id") {
            val type = simpleReturnType.stripRefSuffixes()
            setOf(type)
        } else {
            var out = InferenceResult()
            val expressions = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
            val selfExpressionTypes = expressions.filter { it.text == "self"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName)}
            val superExpressionTypes = expressions.filter { it.text == "super"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text)}
            val simpleOut = selfExpressionTypes + superExpressionTypes
            if (simpleOut.isNotEmpty()) {
                return InferenceResult(classes = simpleOut.toSet())
            }
            expressions.forEach {
                //LOGGER.info("Checking return statement <${it.text ?: "_"}> for method call : <${methodCall.text}>")
                val type = inferExpressionType(it, min(level - 1, 3))
                if (type != null)
                    out += type
            }
            out.classes
        }
    }
    return InferenceResult(classes = returnTypes.toSet())
}

private fun getCallTargetTypes(callTarget: ObjJCallTarget, level:Int) : Set<String> {
    val cachedCallTargetTypes = callTarget.getUserData(INFERENCE_TYPES_USER_DATA_KEY)
    return if (cachedCallTargetTypes?.classes.orEmpty().isNotEmpty()) {
        cachedCallTargetTypes!!.toClassList()
    } else {
        val inferredCallTargetTypes =  inferCallTargetType(callTarget, level)
        if (inferredCallTargetTypes?.classes.orEmpty().isNotEmpty()) {
            callTarget.putUserData(INFERENCE_TYPES_USER_DATA_KEY, inferredCallTargetTypes)
        }
        inferredCallTargetTypes?.toClassList() ?: emptySet()
    }
}

private fun inferCallTargetType(callTarget:ObjJCallTarget, level:Int) : InferenceResult? {

    ProgressManager.checkCanceled()
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, level - 1)

    if (callTarget.functionCall != null) {
        return inferFunctionCallReturnType(callTarget.functionCall!!, level - 1)
    }
    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, false,level - 1)
    }
    return null
}