package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType

interface ObjJHasContainingClass : ObjJCompositeElement {

    val containingClassName: String
        get() {
            val containingClass = containingClass
            return containingClass?.classType?.className ?: ObjJClassType.UNDEF.className
        }

    val containingClass: ObjJClassDeclarationElement<*>?
        get() {
            val implementationDeclaration = PsiTreeUtil.getParentOfType(this, ObjJImplementationDeclaration::class.java)
            return implementationDeclaration ?: PsiTreeUtil.getParentOfType(this, ObjJProtocolDeclaration::class.java)
        }

    fun getContainingSuperClass(returnDefault: Boolean = false): ObjJClassName? = cappuccino.ide.intellij.plugin.psi.utils.getContainingSuperClass(this, returnDefault)

}
