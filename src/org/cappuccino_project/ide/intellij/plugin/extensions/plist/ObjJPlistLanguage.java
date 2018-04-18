package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.lang.Language;

public class ObjJPlistLanguage extends Language {
    public static final ObjJPlistLanguage INSTANCE = new ObjJPlistLanguage();

    private ObjJPlistLanguage() {
        super("Plist");
    }
}