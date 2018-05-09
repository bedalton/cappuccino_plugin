package org.cappuccino_project.ide.intellij.plugin.psi.utils

import org.cappuccino_project.ide.intellij.plugin.psi.*

import java.util.ArrayList
import java.util.logging.Logger

import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.Companion.ID
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDEF_CLASS_NAME
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDETERMINED
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.Companion.isPrimitive

private val LOGGER = Logger.getLogger("org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil")
private val GET_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("class")
private val GET_SUPERCLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("superclass")
val IS_KIND_OF_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("isKindOfClass")

fun ObjJMethodCall.getSelectorString(): String {
    val stub = stub
    return stub?.selectorString
            ?: ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(selectorStrings)
}


fun isUniversalMethodCaller(className: String): Boolean {
    return !isPrimitive(className) && (UNDETERMINED == className || UNDEF_CLASS_NAME == className) || ID == className
}

fun ObjJMethodCall.getSelectorStrings(): List<String> {
    return if (stub != null && !stub.selectorStrings.isEmpty()) {
        stub.selectorStrings
    } else ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(getSelectorList())
}

fun ObjJMethodCall.getSelectorList(): List<ObjJSelector> {
    if (selector != null) {
        return listOf<ObjJSelector>(selector!!)
    }
    val out = ArrayList<ObjJSelector>()
    for (qualifiedSelector in qualifiedMethodCallSelectorList) {
        if (qualifiedSelector.selector != null) {
            out.add(qualifiedSelector.selector!!)
        }
    }
    return out
}

fun ObjJMethodCall.getCallTargetText(): String {
    return if (stub != null) {
        stub.callTarget
    } else callTarget.text
}
