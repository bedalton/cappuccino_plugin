package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesDelegate;

public class ObjJBraceEnterHandler  extends EnterBetweenBracesDelegate {

    protected boolean isBracePair(char c1, char c2) {
        return (c1 == '(' && c2 == ')') || (c1 == '{' && c2 == '}') || (c1 == '[' && c2 == ']') || (c1 == '<' && c2 == '>');
    }
}