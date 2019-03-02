package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*

import java.util.ArrayList
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.ID
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDEF_CLASS_NAME
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.isPrimitive
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import com.intellij.psi.PsiElement

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
    val selectors: List<String>? = methodCall.stub?.selectorStrings
    if (selectors != null && selectors.isNotEmpty())
        return selectors
    return ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(getSelectorList(methodCall));
}

fun getSelectorList(methodCall:ObjJMethodCall): List<ObjJSelector?> {
    val singleSelector = methodCall.selector
    if (singleSelector != null) {
        return mutableListOf(singleSelector)
    }
    val out = ArrayList<ObjJSelector?>()
    for (qualifiedSelector in methodCall.qualifiedMethodCallSelectorList) {
        if (qualifiedSelector.selector != null) {
            out.add(qualifiedSelector.selector!!)
        } else {
            out.add(null)
        }
    }
    return out
}

fun getCallTargetText(methodCall:ObjJMethodCall): String =
        methodCall.stub?.callTarget ?: methodCall.callTarget.text


/**
 * Gets selectors from incomplete method call
 * Used while building completion results
 * @param psiElement currently editing psi element
 * @return array of selectors in this editing psi element
 */
fun getSelectorsFromIncompleteMethodCall(psiElement: PsiElement, selectorParentMethodCall: ObjJMethodCall?): List<ObjJSelector> {
    val project = psiElement.project

    //Check to ensure this element is part of a method
    if (selectorParentMethodCall == null) {
        //LOGGER.log(Level.INFO, "PsiElement is not a selector in a method call element");
        return listOf()
    }

    // If element is selector or direct parent is a selector,
    // then element is well formed, and can return the basic selector list
    // which will hold selectors up to self, which is what is needed for completion
    var selectorIndex: Int
    val selectors = selectorParentMethodCall.selectorList as MutableList
    if (psiElement is ObjJSelector || psiElement.parent is ObjJSelector) {
        return selectors
    }

    // If psi parent is a qualified method call,
    // find it's index in selector array for autocompletion
    if (psiElement.parent is ObjJQualifiedMethodCallSelector) {
        val qualifiedMethodCallSelector = psiElement.parent as ObjJQualifiedMethodCallSelector
        if (qualifiedMethodCallSelector.selector != null) {
            selectorIndex = selectors.indexOf(qualifiedMethodCallSelector.selector!!)
        } else {
            selectorIndex = selectors.size - 1
        }
    } else {
        selectorIndex = selectors.size - 1
    }

    // Find orphaned elements in method call
    // and create selector elements for later use.
    val orphanedSelectors = psiElement.parent.getChildrenOfType(ObjJTypes.ObjJ_ID)
    for (subSelector in orphanedSelectors) {
        val selector = ObjJElementFactory.createSelector(project, subSelector.text) ?: return selectors
        selectors.add(++selectorIndex, selector)
    }
    //Finally add the current psi element as a selector
    val selector = ObjJElementFactory.createSelector(project, psiElement.text) ?: return selectors
    selectors.add(selector)
    return selectors
}