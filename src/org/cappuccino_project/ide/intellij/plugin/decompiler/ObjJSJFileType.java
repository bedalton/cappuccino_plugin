package org.cappuccino_project.ide.intellij.plugin.decompiler;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjJSJFileType extends LanguageFileType {
    public static final String FILE_EXTENSION = "sj";
    public static final ObjJSJFileType INSTANCE = new ObjJSJFileType();

    private ObjJSJFileType() {
        super(ObjJLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Objective-J Compiled Source";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Compiled Objective-J source code";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ObjJIcons.SJ_DOCUMENT_ICON;
    }
}
