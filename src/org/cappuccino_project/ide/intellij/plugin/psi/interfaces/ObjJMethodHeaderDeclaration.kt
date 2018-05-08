package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope

interface ObjJMethodHeaderDeclaration<StubT : ObjJMethodHeaderDeclarationStub<out ObjJMethodHeaderDeclaration<*>>> : ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJHasMethodSelector, ObjJHasContainingClass {

    val returnType: String

    val methodScope: MethodScope

    val isStatic: Boolean
        get() = methodScope == MethodScope.STATIC

    override fun getStub(): StubT
}
