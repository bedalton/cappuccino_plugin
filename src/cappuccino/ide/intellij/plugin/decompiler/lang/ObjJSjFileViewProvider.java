package cappuccino.ide.intellij.plugin.decompiler.lang;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJSjFileViewProvider extends SingleRootFileViewProvider {
    public ObjJSjFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile file) {
        this(manager, file, true);
    }

    public ObjJSjFileViewProvider(@NotNull PsiManager manager,
                                  @NotNull VirtualFile virtualFile,
                                  boolean eventSystemEnabled) {
        super(manager, virtualFile, eventSystemEnabled, ObjJLanguage.Companion.getINSTANCE());
    }

    @Nullable
    @Override
    protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile file, @NotNull FileType fileType) {
        return new ObjJSjFileImpl(this);
    }

    @NotNull
    @Override
    public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
        return new ObjJSjFileViewProvider(getManager(), copy, false);
    }
}