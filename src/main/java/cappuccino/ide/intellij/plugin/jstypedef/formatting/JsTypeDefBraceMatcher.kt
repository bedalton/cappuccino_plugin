package cappuccino.ide.intellij.plugin.jstypedef.formatting

import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/**
 * Attempts to create a matching brace for a given starting brace
 */
class JsTypeDefBraceMatcher : PairedBraceMatcher {


    override fun getPairs(): Array<BracePair> {
        return PAIRS
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, nextTokenType: IElementType?): Boolean {
        return nextTokenType in allowBraceBefore
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf(
                BracePair(JS_OPEN_PAREN, JS_CLOSE_PAREN, false),
                BracePair(JS_OPEN_BRACKET, JS_CLOSE_BRACKET, false),
                BracePair(JS_OPEN_BRACE, JS_CLOSE_BRACE, false),
                BracePair(JS_OPEN_ARROW, JS_CLOSE_ARROW, true),
                BracePair(JS_DOUBLE_QUOTE, JS_DOUBLE_QUOTE, true),
                BracePair(JS_SINGLE_QUOTE, JS_SINGLE_QUOTE, true),
                BracePair(JS_BLOCK_COMMENT_START, JS_BLOCK_COMMENT_END, true)
        )

        private val allowBraceBefore = listOf(
                JS_SEMI_COLON,
                JS_COMMA,
                JS_CLOSE_PAREN,
                JS_CLOSE_BRACE,
                JS_CLOSE_BRACKET,
                JS_DOT,
                JS_LINE_TERMINATOR,
                TokenType.WHITE_SPACE
        )
    }
}
