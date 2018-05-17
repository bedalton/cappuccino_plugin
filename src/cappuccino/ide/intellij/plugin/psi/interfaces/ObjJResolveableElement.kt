package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub

interface ObjJResolveableElement<StubT : ObjJResolveableStub<*>> : ObjJStubBasedElement<StubT> {
    fun shouldResolve(): Boolean
}
