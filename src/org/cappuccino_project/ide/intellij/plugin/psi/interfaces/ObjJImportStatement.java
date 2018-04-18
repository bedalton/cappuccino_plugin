package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ObjJImportStatement<StubT extends ObjJImportStub<? extends ObjJImportStatement<?>>> extends ObjJStubBasedElement<StubT>, ObjJCompositeElement {

    public static final String DELIMITER = "::";

    @Nullable
    String getFrameworkName();
    @NotNull
    String getFileName();

    default String getImportAsUnifiedString() {
        return (getFrameworkName() != null ? getFrameworkName() : "") + DELIMITER + getFileName();
    }

}
