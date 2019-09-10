package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.TokenType

class ObjJSimpleQuoteHandler : SimpleTokenSetQuoteHandler(ObjJTokenSets.QUOTE_CHARS) {

    override fun isOpeningQuote(iterator: HighlighterIterator?, offset: Int): Boolean {
        val isOpeningQuote = super.isOpeningQuote(iterator, offset)
        if (isOpeningQuote) {
            iterator?.retreat()
            val prevToken = iterator?.tokenType
            return prevToken != ObjJ_QUO_TEXT
        }
        return isOpeningQuote
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        return if (tokenType != ObjJ_SINGLE_QUO && tokenType != ObjJ_DOUBLE_QUO) {
            false
        } else {
            val start = iterator.start
            val end = iterator.end
            end - start >= 1
        }
    }

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean {
        return when (iterator.tokenType) {
            ObjJ_QUO_TEXT, TokenType.BAD_CHARACTER -> true
            else -> false
        }
    }
}