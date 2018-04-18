package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl;

public interface ObjJVarTypeIdStub extends StubElement<ObjJVarTypeIdImpl> {
    String getIdType();
}
