package cappuccino.ide.intellij.plugin.decompiler.lang;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiFileImpl;
import org.jetbrains.annotations.NotNull;

public class ObjJSjFileImpl extends PsiFileImpl implements PsiFile {

    protected ObjJSjFileImpl(
            @NotNull
                    FileViewProvider provider) {
        super(provider);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ObjJSjFileType.Companion.getINSTANCE();
    }

    @Override
    public void accept(
            @NotNull
                    PsiElementVisitor psiElementVisitor) {

    }
}
