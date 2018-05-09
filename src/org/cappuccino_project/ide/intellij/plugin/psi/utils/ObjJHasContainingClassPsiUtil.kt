package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType

import java.util.ArrayList


object ObjJHasContainingClassPsiUtil {

    fun isSimilarClass(methodContainingClass: String, className: String): Boolean {
        return isUniversalMethodCaller(className) || isUniversalMethodCaller(methodContainingClass) || className == methodContainingClass
    }

    fun getContainingClass(element: PsiElement?): ObjJClassDeclarationElement<*>? {
        return element.getParentOfType( ObjJClassDeclarationElement::class.java)
    }

    fun getContainingClassName(methodHeader: ObjJMethodHeader): String {
        val stub = methodHeader.stub
        if (stub != null) {
            return stub.containingClassName
        }
        val containingClass = methodHeader.containingClass
        return if (containingClass != null) containingClass.classType.className else ObjJClassType.UNDEF_CLASS_NAME
    }

    fun getContainingClassName(compositeElement: ObjJCompositeElement): String {
        val classDeclarationElement = getContainingClass(compositeElement) ?: return ObjJClassType.UNDEF.className
        return classDeclarationElement.classNameString
    }

    fun getContainingClassName(classDeclarationElement: ObjJClassDeclarationElement<*>?): String {
        return classDeclarationElement?.classNameString ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    fun getContainingClassName(selectorLiteral: ObjJSelectorLiteral?): String {
        if (selectorLiteral == null) {
            return ObjJClassType.UNDEF_CLASS_NAME
        }
        val stub = selectorLiteral.stub
        return stub?.containingClassName ?: getContainingClassName(selectorLiteral.containingClass)
    }

    fun getContainingClassNamesFromSelector(elements: List<ObjJSelector>): List<String> {
        val out = ArrayList<String>()
        for (element in elements) {

        }
        return out
    }

    fun getContainingClassNames(elements: List<PsiElement>): List<String> {
        val out = ArrayList<String>()
        for (element in elements) {
            var className: String? = null
            if (elements is ObjJHasContainingClass) {
                className = (element as ObjJHasContainingClass).containingClassName
            } else if (element is ObjJCompositeElement) {
                className = getContainingClassName(element)
            }
            if (className != null && !out.contains(className)) {
                out.add(className)
            }
        }
        return out
    }

    fun sharesContainingClass(className: String, vararg elements: PsiElement): Boolean {
        for (element in elements) {
            if (!ObjJHasContainingClassPsiUtil.hasContainingClass(element, className)) {
                return false
            }
        }
        return true
    }

    fun hasContainingClass(element: PsiElement?, className: String): Boolean {
        return className == ObjJClassType.UNDETERMINED || element != null && element is ObjJHasContainingClass && element.containingClassName == className
    }


    fun getContainingClassOrFileName(psiElement: PsiElement): String {
        var containingClassName: String? = null
        if (psiElement is ObjJHasContainingClass) {
            containingClassName = psiElement.containingClassName
        }
        if (containingClassName == null) {
            val classDeclarationElement = psiElement.getParentOfType( ObjJClassDeclarationElement::class.java)
            if (classDeclarationElement != null) {
                containingClassName = classDeclarationElement.classNameString
            }
        }
        if (containingClassName == null) {
            containingClassName = psiElement.containingFile.virtualFile.name
        }
        return containingClassName
    }

    fun getContainingSuperClassName(element: ObjJCompositeElement): String? {
        val classDeclarationElement = getContainingClass(element)
        return if (classDeclarationElement == null || classDeclarationElement !is ObjJImplementationDeclaration) {
            null
        } else classDeclarationElement.getSuperClassName()
    }


}
