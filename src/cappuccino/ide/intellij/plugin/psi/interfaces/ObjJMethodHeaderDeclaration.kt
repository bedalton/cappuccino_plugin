package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope

interface ObjJMethodHeaderDeclaration<StubT : ObjJMethodHeaderDeclarationStub<*>> : ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJHasMethodSelector, ObjJHasContainingClass {

    val returnType: String

    val methodScope: MethodScope

    val isStatic: Boolean
        get() = methodScope == MethodScope.STATIC

    override fun getStub(): StubT
}
