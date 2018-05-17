package cappuccino.ide.intellij.plugin.decompiler.lang;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJSjFileViewProviderFactory  implements FileViewProviderFactory {
    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull
                                                           VirtualFile file,
                                                   @Nullable
                                                           Language language,
                                                   @NotNull
                                                           PsiManager manager,
                                                   boolean eventSystemEnabled) {
        return new ObjJSjFileViewProvider(manager, file, eventSystemEnabled);
    }
}