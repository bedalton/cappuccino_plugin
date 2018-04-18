package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjJPlistFileType extends LanguageFileType {
    public static final String FILE_EXTENSION = "plist";
    public static final ObjJPlistFileType INSTANCE = new ObjJPlistFileType();

    private ObjJPlistFileType() {
        super(ObjJPlistLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Objective-J Property Plist";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "An Objective-J plist file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ObjJPlistIcons.DOCUMENT_ICON;
    }
}
