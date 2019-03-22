package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import org.jetbrains.annotations.Contract

fun getSelectorString(methodCall : ObjJMethodCall): String {
    return methodCall.stub?.selectorString
            ?: getSelectorStringFromSelectorStrings(methodCall.selectorStrings)
}

fun getSelectorStrings(methodCall: ObjJMethodCall): List<String> {
    val selectors: List<String>? = methodCall.stub?.selectorStrings
    if (selectors != null && selectors.isNotEmpty())
        return selectors
    return getSelectorStringsFromSelectorList(getSelectorList(methodCall))
}

fun getSelectorString(methodHeader: ObjJMethodHeader): String {
    return methodHeader.stub?.selectorString ?: getSelectorStringFromSelectorStrings(getSelectorStrings(methodHeader))
}

private fun getSelectorStringsFromMethodDeclarationSelectorList(
        selectorElements: List<ObjJMethodDeclarationSelector>): List<String> {
    if (selectorElements.isEmpty()) {
        return ArrayUtils.EMPTY_STRING_ARRAY
    }
    val selectorStrings = ArrayList<String>()
    for (selectorElement in selectorElements) {
        selectorStrings.add(getSelectorString(selectorElement, false))
    }
    return selectorStrings
}

fun getSelectorStrings(methodHeader: ObjJMethodHeader): List<String> {
    return methodHeader.stub?.selectorStrings ?: getSelectorStringsFromMethodDeclarationSelectorList(methodHeader.methodDeclarationSelectorList)
}


fun getSelectorString(selectorElement: ObjJMethodDeclarationSelector?, addSuffix: Boolean): String {
    val selector = selectorElement?.selector
    return getSelectorString(selector, addSuffix)
}

@Contract(pure = true)
fun getSelectorString(selectorElement: ObjJSelector?, addSuffix: Boolean): String {
    val selector = if (selectorElement != null) selectorElement.text else ""
    return if (addSuffix) {
        selector + ObjJMethodPsiUtils.SELECTOR_SYMBOL
    } else {
        selector
    }
}

fun getSelectorString(selectorLiteral: ObjJSelectorLiteral): String {
    return selectorLiteral.stub?.selectorString ?: getSelectorStringFromSelectorStrings(selectorLiteral.selectorStrings)
}

@Contract(pure = true)
fun getSelectorString(selector: String?): String {
    return (selector ?: "") + ObjJMethodPsiUtils.SELECTOR_SYMBOL
}

fun getSelectorStringFromSelectorStrings(selectors: List<String>): String {
    return ArrayUtils.join(selectors, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
}

fun getSelectorStringFromSelectorList(selectors: List<ObjJSelector?>): String {
    return getSelectorStringFromSelectorStrings(getSelectorStringsFromSelectorList(selectors))
}

fun getSelectorStringsFromSelectorList(selectors:List<ObjJSelector?>) : List<String> {
    val out:MutableList<String> = ArrayList()
    selectors.forEach {
        out.add(it?.getSelectorString(false) ?: "")
    }
    return out
}

fun getSelectorUntil(targetSelectorElement: ObjJSelector, include: Boolean): String? {
    val parent = targetSelectorElement.getParentOfType( ObjJHasMethodSelector::class.java)
            ?: return null

    val builder = StringBuilder()
    val selectorIndex = if (include)  targetSelectorElement.selectorIndex else targetSelectorElement.selectorIndex - 1
    if (selectorIndex <=0 && !include) {
        return null
    }
    val selectors = parent.selectorList.subList(0, selectorIndex)
    for (subSelector in selectors) {
        builder.append(getSelectorString(subSelector, true))
    }
    return builder.toString()
}
/**
 * Gets all selector sibling selector strings after the given index
 * @param selector base selector
 * @param selectorIndex selector index
 * @return list of trailing sibling selectors as strings
 */
fun getTrailingSelectorStrings(selector: ObjJSelector, selectorIndex: Int): List<String> {
    val methodHeaderDeclaration = selector.getParentOfType( ObjJMethodHeaderDeclaration::class.java)
    val temporarySelectorsList = methodHeaderDeclaration?.selectorStrings ?: ArrayUtils.EMPTY_STRING_ARRAY
    val numSelectors = temporarySelectorsList.size
    return if (numSelectors > selectorIndex) temporarySelectorsList.subList(selectorIndex + 1, numSelectors) else ArrayUtils.EMPTY_STRING_ARRAY
}