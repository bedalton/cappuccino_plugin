package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*

import java.util.ArrayList
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.ID
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDEF_CLASS_NAME
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.isPrimitive

private val LOGGER = Logger.getLogger("cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil")
private val GET_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("class")
private val GET_SUPERCLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("superclass")
val IS_KIND_OF_CLASS_METHOD_SELECTOR = ObjJMethodPsiUtils.getSelectorString("isKindOfClass")

fun getSelectorString(methodCall : ObjJMethodCall): String {
    return methodCall.stub?.selectorString
            ?: ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(methodCall.selectorStrings)
}


fun isUniversalMethodCaller(className: String): Boolean {
    return !isPrimitive(className) && (UNDETERMINED == className || UNDEF_CLASS_NAME == className) || ID == className
}

fun getSelectorStrings(methodCall:ObjJMethodCall): List<String> {
    val selectors = methodCall.stub?.selectorStrings
    return if (selectors != null && selectors.isNotEmpty())
        selectors
    else
        ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(methodCall.selectorList)
}

fun getSelectorList(methodCall:ObjJMethodCall): List<ObjJSelector> {
    val singleSelector = methodCall.selector
    if (singleSelector != null) {
        return mutableListOf(singleSelector)
    }
    val out = ArrayList<ObjJSelector>()
    for (qualifiedSelector in methodCall.qualifiedMethodCallSelectorList) {
        if (qualifiedSelector.selector != null) {
            out.add(qualifiedSelector.selector!!)
        }
    }
    return out
}

fun getCallTargetText(methodCall:ObjJMethodCall): String =
        methodCall.stub?.callTarget ?: methodCall.callTarget.text