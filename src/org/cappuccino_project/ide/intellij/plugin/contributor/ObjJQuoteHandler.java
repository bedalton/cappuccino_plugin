package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;

public class ObjJQuoteHandler implements QuoteHandler {
    protected final TokenSet myLiteralTokenSet;

    public ObjJQuoteHandler () {
        this(TokenSet.create(ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL, ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL));
    }

    public ObjJQuoteHandler(TokenSet tokenSet) {
        myLiteralTokenSet = tokenSet;
    }

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        final IElementType tokenType = iterator.getTokenType();

        if (myLiteralTokenSet.contains(tokenType)) {
            int start = iterator.getStart();
            int end = iterator.getEnd();
            return end - start >= 1 && offset == end - 1;
        }

        return false;
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        if (myLiteralTokenSet.contains(iterator.getTokenType())) {
            int start = iterator.getStart();
            return offset == start;
        }

        return false;
    }

    @Override
    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
        int start = iterator.getStart();
        try {
            Document doc = editor.getDocument();
            CharSequence chars = doc.getCharsSequence();
            int lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset));

            while (!iterator.atEnd() && iterator.getStart() < lineEnd) {
                IElementType tokenType = iterator.getTokenType();

                if (myLiteralTokenSet.contains(tokenType)) {
                    if (isNonClosedLiteral(iterator, chars)) return true;
                }
                iterator.advance();
            }
        } finally {
            while (iterator.atEnd() || iterator.getStart() != start) iterator.retreat();
        }

        return false;
    }

    protected boolean isNonClosedLiteral(HighlighterIterator iterator, CharSequence chars) {
        if (iterator.getStart() >= iterator.getEnd() - 1 ||
                chars.charAt(iterator.getEnd() - 1) != '\"' && chars.charAt(iterator.getEnd() - 1) != '\'') {
            return true;
        }
        return false;
    }

    @Override
    public boolean isInsideLiteral(HighlighterIterator iterator) {
        return myLiteralTokenSet.contains(iterator.getTokenType());
    }

}
