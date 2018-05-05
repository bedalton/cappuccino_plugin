package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;

public class ObjJVarTypeIdStubImpl extends ObjJStubBaseImpl<ObjJVarTypeIdImpl> implements ObjJVarTypeIdStub {
    final String idType;
    final boolean shouldResolve;
    public ObjJVarTypeIdStubImpl(StubElement parent, @NotNull String idType, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.VAR_TYPE_ID);
        this.idType = idType;
        this.shouldResolve = shouldResolve;
    }

    @Override
    public String getIdType() {
        return idType;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }
}
