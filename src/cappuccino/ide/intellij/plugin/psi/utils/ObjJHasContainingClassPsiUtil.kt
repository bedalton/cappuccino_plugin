package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJSelectorLiteral
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType

object ObjJHasContainingClassPsiUtil {

    fun getContainingClass(element: PsiElement?): ObjJClassDeclarationElement<*>? {
        return element.getParentOfType( ObjJClassDeclarationElement::class.java)
    }

    fun getContainingClassName(methodHeader: ObjJMethodHeader): String {
        return methodHeader.stub?.containingClassName
                ?: getContainingClass(methodHeader)?.classNameString
                ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    fun getContainingClassName(variableDeclaration:ObjJInstanceVariableDeclaration) : String {
        return variableDeclaration.stub?.containingClass
                ?: getContainingClass(variableDeclaration)?.classNameString
                ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    fun getContainingClassName(compositeElement: ObjJCompositeElement): String {
        val classDeclarationElement = getContainingClass(compositeElement) ?: return ObjJClassType.UNDEF_CLASS_NAME
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
        } else classDeclarationElement.superClassName
    }


}
