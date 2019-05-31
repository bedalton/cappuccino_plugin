package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.inferFunctionCallReturnType
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil


/**
 * Attempts to get possible variable types from a selector
 */
fun getClassConstraints(element: ObjJSelector, tag:Long): List<String> {
    return getClassConstraints(element.getParentOfType( ObjJHasMethodSelector::class.java), tag)
}


private fun getClassConstraints(element: ObjJHasMethodSelector?, tag:Long): List<String> {
    if (element !is ObjJMethodCall) {
        return emptyList()
    }
    val methodCall = element as ObjJMethodCall?
    val callTarget: ObjJCallTarget? = methodCall?.callTarget
    if (callTarget != null) {

        val out = getPossibleClassTypesForCallTarget(callTarget, tag).flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, element.project)
        }
        if (out.isNotEmpty()) {
            return out.toList()
        }
    }
    return methodCall?.callTarget?.getPossibleCallTargetTypes(tag) ?: mutableListOf()
}

/**
 * Attempts to get possible class types for a call target
 */
fun getPossibleClassTypesForCallTarget(callTarget: ObjJCallTarget, tag:Long) : Set<String> {
    val inference = when {
        callTarget.expr != null -> inferExpressionType(callTarget.expr!!, INFERENCE_LEVELS_DEFAULT, tag)
        callTarget.functionCall != null -> inferFunctionCallReturnType(callTarget.functionCall!!, INFERENCE_LEVELS_DEFAULT, tag)
        callTarget.qualifiedReference != null -> inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, INFERENCE_LEVELS_DEFAULT, tag)
        else -> null
    }
    val classList = inference?.toClassList().orEmpty()
    if (classList.isNotEmpty()) {
        return classList
    }
    val qualifiedReference = callTarget.qualifiedReference ?: return setOf()
    return inferQualifiedReferenceType(qualifiedReference.qualifiedNameParts, INFERENCE_LEVELS_DEFAULT, tag)?.toClassList() ?: emptySet()
}