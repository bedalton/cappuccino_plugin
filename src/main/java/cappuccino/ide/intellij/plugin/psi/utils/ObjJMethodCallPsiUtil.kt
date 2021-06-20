package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedMethodCallSelector
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.ID
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDEF_CLASS_NAME
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.isPrimitive
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReference
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import java.util.*

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

/**
 * Gets selectors from incomplete method call
 * Used while building completion results
 * @param psiElement currently editing psi element
 * @return array of selectors in this editing psi element
 */
fun getSelectorsFromIncompleteMethodCall(psiElement: PsiElement, selectorParentMethodCall: ObjJMethodCall?): List<ObjJSelector>? {
    val project = psiElement.project

    //Check to ensure this element is part of a method
    if (selectorParentMethodCall == null) {
        ////LOGGER.info("PsiElement is not a selector in a method call element");
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
    selectorIndex = if (psiElement.parent is ObjJQualifiedMethodCallSelector) {
        val qualifiedMethodCallSelector = psiElement.parent as ObjJQualifiedMethodCallSelector
        if (qualifiedMethodCallSelector.selector != null) {
            selectors.indexOf(qualifiedMethodCallSelector.selector!!)
        } else {
            selectors.size - 1
        }
    } else {
        selectors.size - 1
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
    return selectors.ifEmpty { null }
}

fun getCallTargetText(methodCall:ObjJMethodCall): String =
        methodCall.stub?.callTarget ?: methodCall.callTarget.text

fun isUniversalMethodCaller(className: String): Boolean {
    return !isPrimitive(className) && (UNDETERMINED == className || UNDEF_CLASS_NAME == className) || ID == className
}

val ObjJMethodCall.referencedHeaders:List<ObjJMethodHeaderDeclaration<*>> get() {
    return this.multiResolve.mapNotNull {
        it.element.thisOrParentAs(ObjJMethodHeaderDeclaration::class.java)
    }
}

val ObjJMethodCall.multiResolve:Array<ResolveResult> get() {
    val selector = this.selectorList.firstOrNull() ?: return emptyArray()
    val reference = ObjJSelectorReference(selector)
    return reference.multiResolve(false)
}

val ObjJQualifiedMethodCallSelector.index:Int get() {
    val methodCall = this.parent as? ObjJMethodCall ?: this.parent.parent as? ObjJMethodCall ?: return -1
    return methodCall.qualifiedMethodCallSelectorList.indexOf(this)
}