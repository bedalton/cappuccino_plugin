package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil


/**
 * Attempts to get possible variable types from a selector
 */
fun getClassConstraints(element: ObjJSelector, tag: Tag, strict:MutableList<String>? = null): List<String> {
    return getClassConstraints(element.getParentOfType( ObjJHasMethodSelector::class.java), tag, strict)
}


private fun getClassConstraints(element: ObjJHasMethodSelector?, tag: Tag, strict:MutableList<String>?): List<String> {
    if (element !is ObjJMethodCall) {
        return emptyList()
    }
    val methodCall = element as ObjJMethodCall?
    val callTarget: ObjJCallTarget? = methodCall?.callTarget
    if (callTarget != null) {

        val out = getPossibleClassTypesForCallTarget(callTarget, tag).flatMap {
            strict?.add(it)
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
fun getPossibleClassTypesForCallTarget(callTarget: ObjJCallTarget, tag: Tag) : Set<String> {
    val inference = when {
        callTarget.expr != null -> inferExpressionType(callTarget.expr!!, tag)
        callTarget.qualifiedReference != null -> inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, tag)
        else -> null
    }
    val classList = inference?.toClassList(null)?.withoutAnyType().orEmpty()
    if (classList.isNotEmpty()) {
        return classList
    }
    val qualifiedReference = callTarget.qualifiedReference ?: return setOf()
    return inferQualifiedReferenceType(qualifiedReference.qualifiedNameParts, tag)?.toClassList() ?: emptySet()
}