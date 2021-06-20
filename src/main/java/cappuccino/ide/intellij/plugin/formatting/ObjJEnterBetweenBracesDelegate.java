package cappuccino.ide.intellij.plugin.formatting;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesDelegate;

public class ObjJEnterBetweenBracesDelegate extends EnterBetweenBracesDelegate {

    @Override
    protected boolean isBracePair(char lBrace, char rBrace) {
        return (lBrace == '(' && rBrace == ')') || (lBrace == '{' && rBrace == '}') || (lBrace == '[' && rBrace == ']');
    }
}
