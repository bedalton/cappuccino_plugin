package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjJFileType extends LanguageFileType {
    public static final String FILE_EXTENSION = "j";
    public static final ObjJFileType INSTANCE = new ObjJFileType();

    private ObjJFileType() {
        super(ObjJLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Objective-J Script";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "An Objective-J script file for the Cappuccino Web Framework";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ObjJIcons.DOCUMENT_ICON;
    }
}
