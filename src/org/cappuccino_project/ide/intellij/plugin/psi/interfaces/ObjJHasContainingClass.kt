package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.util.PsiTreeUtil
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType

interface ObjJHasContainingClass : ObjJCompositeElement {

    val containingClassName: String
        get() {
            val containingClass = containingClass
            return if (containingClass != null) containingClass.classType.className else ObjJClassType.UNDEF.className
        }

    val containingClass: ObjJClassDeclarationElement<*>?
        get() {
            val implementationDeclaration = PsiTreeUtil.getParentOfType(this, ObjJImplementationDeclaration::class.java)
            return implementationDeclaration ?: PsiTreeUtil.getParentOfType(this, ObjJProtocolDeclaration::class.java)
        }

}
