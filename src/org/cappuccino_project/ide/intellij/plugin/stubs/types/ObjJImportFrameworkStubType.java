package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFileImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;

public class ObjJImportFrameworkStubType extends ObjJImportStatementStubType<ObjJImportFrameworkImpl> {

    public ObjJImportFrameworkStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJImportFrameworkImpl.class);
    }

    @Override
    public ObjJImportFrameworkImpl createPsi(
            @NotNull
                    ObjJImportStub<ObjJImportFrameworkImpl> stub) {
        return new ObjJImportFrameworkImpl(stub, this);
    }
}
