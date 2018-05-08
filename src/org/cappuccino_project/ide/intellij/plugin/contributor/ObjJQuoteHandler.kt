package org.cappuccino_project.ide.intellij.plugin.contributor

import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes

class ObjJQuoteHandler @JvmOverloads constructor(protected val myLiteralTokenSet: TokenSet = TokenSet.create(ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL, ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL)) : QuoteHandler {

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType

        if (myLiteralTokenSet.contains(tokenType)) {
            val start = iterator.start
            val end = iterator.end
            return end - start >= 1 && offset == end - 1
        }

        return false
    }

    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        if (myLiteralTokenSet.contains(iterator.tokenType)) {
            val start = iterator.start
            return offset == start
        }

        return false
    }

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean {
        val start = iterator.start
        try {
            val doc = editor.document
            val chars = doc.charsSequence
            val lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset))

            while (!iterator.atEnd() && iterator.start < lineEnd) {
                val tokenType = iterator.tokenType

                if (myLiteralTokenSet.contains(tokenType)) {
                    if (isNonClosedLiteral(iterator, chars)) return true
                }
                iterator.advance()
            }
        } finally {
            while (iterator.atEnd() || iterator.start != start) iterator.retreat()
        }

        return false
    }

    protected fun isNonClosedLiteral(iterator: HighlighterIterator, chars: CharSequence): Boolean {
        return if (iterator.start >= iterator.end - 1 || chars[iterator.end - 1] != '\"' && chars[iterator.end - 1] != '\'') {
            true
        } else false
    }

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean {
        return myLiteralTokenSet.contains(iterator.tokenType)
    }

}
