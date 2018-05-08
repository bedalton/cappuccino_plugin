package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub

interface ObjJResolveableElement<StubT : ObjJResolveableStub<*>> : ObjJStubBasedElement<StubT> {
    fun shouldResolve(): Boolean
}
