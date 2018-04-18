package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJIncludeFileImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;

public class ObjJIncludeFileStubType extends ObjJImportStatementStubType<ObjJIncludeFileImpl> {

    public ObjJIncludeFileStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJIncludeFileImpl.class);
    }

    @Override
    public ObjJIncludeFileImpl createPsi(
            @NotNull
                    ObjJImportStub<ObjJIncludeFileImpl> stub) {
        return new ObjJIncludeFileImpl(stub, this);
    }
}
