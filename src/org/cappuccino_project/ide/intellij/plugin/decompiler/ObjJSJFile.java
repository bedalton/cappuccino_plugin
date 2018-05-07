package org.cappuccino_project.ide.intellij.plugin.decompiler;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFileType;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ObjJSJFile extends PsiFileBase {


    public ObjJSJFile(@NotNull
                                         FileViewProvider viewProvider) {
        super(viewProvider, ObjJLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Objective-J compiled source";
    }

    @Override
    public Icon getIcon(int flags) {
        return ObjJIcons.SJ_DOCUMENT_ICON;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ObjJSJFileType.INSTANCE;
    }


}
