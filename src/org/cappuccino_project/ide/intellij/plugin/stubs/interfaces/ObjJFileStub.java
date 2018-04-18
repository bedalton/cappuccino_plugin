package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.PsiFileStub;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ObjJFileStub extends PsiFileStub<ObjJFile> {
    @NotNull
    List<String> getImports();
    @NotNull
    String getFileName();
    @NotNull
    List<String> getImportsForFramework(@NotNull String framework);
}