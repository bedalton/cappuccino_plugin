package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector


fun getElementDescription(
        psiElement: PsiElement,
        elementDescriptionLocation: ElementDescriptionLocation): String {
    return if (psiElement is ObjJSelector) {
        getElementDescription(psiElement, elementDescriptionLocation)
    } else ElementDescriptionUtil.getElementDescription(psiElement, elementDescriptionLocation)
}

private fun getElementDescription(selector: ObjJSelector,
                                  elementDescriptionLocation: ElementDescriptionLocation): String {
    return if (elementDescriptionLocation is UsageViewLongNameLocation) {
        selector.getSelectorString(true)
    } else ElementDescriptionUtil.getElementDescription(selector, elementDescriptionLocation)
}