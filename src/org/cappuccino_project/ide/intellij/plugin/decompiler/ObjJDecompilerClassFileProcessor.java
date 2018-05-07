package org.cappuccino_project.ide.intellij.plugin.decompiler;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import static com.intellij.psi.compiled.ClassFileDecompilers.*;
/**
 * Objective-J SJ file decompiler
 */
public class ObjJDecompilerClassFileProcessor extends Light {

    private ObjJSJDecompilerService decompilerService;

    public ObjJDecompilerClassFileProcessor() {
        decompilerService = ServiceManager.getService(ObjJSJDecompilerService.class);
    }

    @NotNull
    @Override
    public CharSequence getText(
            @NotNull
                    VirtualFile virtualFile) throws CannotDecompileException {
        return "/* Should decompile here */";
    }

    @Override
    public boolean accepts(
            @NotNull
                    VirtualFile virtualFile) {
        return virtualFile.getFileType() == ObjJSJFileType.INSTANCE;
    }
}
