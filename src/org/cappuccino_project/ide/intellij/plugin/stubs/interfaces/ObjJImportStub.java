package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ObjJImportStub<PsiT extends ObjJImportStatement<? extends ObjJImportStub<PsiT>>> extends StubElement<PsiT> {
    @NotNull
    String getFileName();
    @Nullable
    String getFramework();
}
