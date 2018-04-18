package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.jetbrains.annotations.NotNull;

public class ObjJFileStubBuilder extends DefaultStubBuilder {
    @NotNull
    @Override
    protected StubElement createStubForFile(@NotNull
                                                    PsiFile file) {
        if (!(file instanceof ObjJFile)) {
            return super.createStubForFile(file);
        }
        return StubIndexService.getInstance().createFileStub((ObjJFile)file);
    }
}
