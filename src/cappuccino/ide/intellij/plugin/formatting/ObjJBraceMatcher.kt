package cappuccino.ide.intellij.plugin.formatting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import com.intellij.psi.TokenType

/**
 * Attempts to create a matching brace for a given starting brace
 */
class ObjJBraceMatcher : PairedBraceMatcher {


    override fun getPairs(): Array<BracePair> {
        return PAIRS
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, nextTokenType: IElementType?): Boolean {
        return when {
            lbraceType == ObjJ_LESS_THAN -> nextTokenType == ObjJ_INHERITED_PROTOCOL_LIST || nextTokenType == ObjJ_OPEN_BRACE
            else -> nextTokenType in allowBraceBefore
        }
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf(
                BracePair(ObjJ_OPEN_PAREN, ObjJ_CLOSE_PAREN, false),
                BracePair(ObjJ_OPEN_BRACKET, ObjJ_CLOSE_BRACKET, false),
                BracePair(ObjJ_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
                BracePair(ObjJ_AT_OPENBRACKET, ObjJ_CLOSE_BRACKET, false),
                BracePair(ObjJ_AT_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
                BracePair(ObjJ_LESS_THAN, ObjJ_GREATER_THAN, true),
                BracePair(ObjJ_DOUBLE_QUO, ObjJ_DOUBLE_QUO, true),
                BracePair(ObjJ_SINGLE_QUO, ObjJ_SINGLE_QUO, true),
                BracePair(ObjJ_BLOCK_COMMENT_START, ObjJ_BLOCK_COMMENT_END, true)
        )

        private val allowBraceBefore = listOf(
                ObjJ_SEMI_COLON,
                ObjJ_COMMA,
                ObjJ_CLOSE_PAREN,
                ObjJ_CLOSE_BRACE,
                ObjJ_CLOSE_BRACKET,
                ObjJ_DOT,
                ObjJ_METHOD_HEADER,
                ObjJ_PLUS,
                ObjJ_MINUS,
                ObjJ_AT_END,
                TokenType.WHITE_SPACE
        )
    }
}
