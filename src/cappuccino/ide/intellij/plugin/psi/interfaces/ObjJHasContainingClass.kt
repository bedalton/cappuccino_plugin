package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import com.intellij.psi.util.PsiTreeUtil

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

val ObjJHasContainingClass.containingClassNameOrNull:String? get() {
    return containingClass?.classType?.className
}

val ObjJHasContainingClass.containingSuperClassName:String?
    get() = cappuccino.ide.intellij.plugin.psi.utils.getContainingSuperClass(this, false)?.text

