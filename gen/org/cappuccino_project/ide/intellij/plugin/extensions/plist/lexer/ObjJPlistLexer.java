package org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer;

import com.intellij.lexer.FlexAdapter;

public class ObjJPlistLexer extends FlexAdapter {
    public ObjJPlistLexer() {
        super(new _ObjJPlistLexer());
    }
}
