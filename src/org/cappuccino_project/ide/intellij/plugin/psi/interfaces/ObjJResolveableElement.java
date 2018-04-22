package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub;

public interface ObjJResolveableElement<StubT extends ObjJResolveableStub> extends ObjJStubBasedElement<StubT> {
    boolean shouldResolve();
}
