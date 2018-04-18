package org.cappuccino_project.ide.intellij.plugin.lexer;

import com.intellij.lexer.FlexAdapter;

public class ObjJLexer extends FlexAdapter {
    public ObjJLexer() {
        super(new _ObjectiveJLexer());
    }
}
