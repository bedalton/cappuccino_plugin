package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJResolveableElement;

public interface ObjJResolveableStub<PsiT extends ObjJResolveableElement<? extends ObjJResolveableStub>> extends StubElement<PsiT> {
    boolean shouldResolve();
}
