package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJIncludeFrameworkImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;

public class ObjJIncludeFrameworkStubType extends ObjJImportStatementStubType<ObjJIncludeFrameworkImpl> {

    public ObjJIncludeFrameworkStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJIncludeFrameworkImpl.class);
    }

    @Override
    public ObjJIncludeFrameworkImpl createPsi(
            @NotNull
                    ObjJImportStub<ObjJIncludeFrameworkImpl> stub) {
        return new ObjJIncludeFrameworkImpl(stub, this);
    }
}
