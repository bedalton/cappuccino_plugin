package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class ObjJPlistFile extends PsiFileBase {
    public ObjJPlistFile(
            @NotNull
                    FileViewProvider viewProvider) {
        super(viewProvider, ObjJPlistLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ObjJPlistFileType.INSTANCE;
    }

    @Override
    @NotNull
    public String toString() {
        return "plist file";
    }
}
