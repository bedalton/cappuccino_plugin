@file:Suppress("UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.jstypedef.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import gnu.trove.TObjectLongHashMap
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.psi.TokenType
import java.util.logging.Logger

@Suppress("DEPRECATION")
class JsTypeDefParserUtil : GeneratedParserUtilBase() {
    companion object {

        private var doAllowSemiColonAfterMethodHeader = false
        private val MODES_KEY = Key.create<TObjectLongHashMap<String>>("MODES_KEY")

        private fun getParsingModes(builder_: PsiBuilder): TObjectLongHashMap<String> {
            var flags = builder_.getUserDataUnprotected(MODES_KEY)
            if (flags == null) {
                flags = TObjectLongHashMap();
                builder_.putUserDataUnprotected(MODES_KEY, flags)
            }
            return flags
        }

        @JvmStatic
        fun inMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            //LOGGER.log(Level.INFO, String.format("In Mode: <%s>: <%b>", mode, getParsingModes(builder_).get(mode) > 0));
            return getParsingModes(builder_).get(mode) > 0
        }


        @JvmStatic
        fun notInMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            return getParsingModes(builder_).get(mode) < 1
        }

        @JvmStatic
        fun enterMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            val flags = getParsingModes(builder_)
            if (!flags.increment(mode)) flags.put(mode, 1)
            return true
        }

        @JvmStatic
        fun eol(builder_: PsiBuilder, level: Int): Boolean {
            return builder_.tokenText?.contains("\n".toRegex()).orTrue()
        }

        @JvmStatic
        fun exitMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            val flags = getParsingModes(builder_)
            val count = flags.get(mode)
            if (count == 1L)
                flags.remove(mode)
            else if (count > 1)
                flags.put(mode, count - 1)
            else
                builder_.error("Could not exit inactive '" + mode + "' mode at offset " + builder_.currentOffset)
            return true
        }

        fun adapt_builder_(root: IElementType, builder: PsiBuilder, parser: PsiParser, tokenSets: Array<TokenSet>): PsiBuilder {
            val result = GeneratedParserUtilBase.adapt_builder_(root, builder, parser, tokenSets)
            GeneratedParserUtilBase.ErrorState.get(result).altMode = true
            return result
        }

        @JvmStatic
        fun notLineTerminator(builder_: PsiBuilder, level: Int): Boolean {
            return !lineTerminatorAhead(builder_, level)
        }

        /**
         * Returns `true` iff on the current index of the parser's
         * token stream a token exists on the `HIDDEN` channel which
         * either is a line terminator, or is a multi line comment that
         * contains a line terminator.
         *
         * @return `true` iff on the current index of the parser's
         * token stream a token exists on the `HIDDEN` channel which
         * either is a line terminator, or is a multi line comment that
         * contains a line terminator.
         */
        @JvmStatic
        fun lineTerminatorAhead(builder_: PsiBuilder, level_: Int): Boolean {
            // Get the token ahead of the current index.

            var i = 0
            var ahead = builder_.lookAhead(i)
            var hadLineTerminator = false
            while (ahead === com.intellij.psi.TokenType.WHITE_SPACE || ahead === JS_LINE_TERMINATOR) {
                if (ahead === JS_LINE_TERMINATOR) {
                    hadLineTerminator = true
                }
                ahead = builder_.lookAhead(++i)
            }
            return eof(builder_, level_) || JsTypeDefPsiImplUtil.eosToken(ahead, hadLineTerminator)

        }

        @JvmStatic
        fun closeBrace(builder_: PsiBuilder, level: Int): Boolean {
            val next = builder_.lookAhead(1)
            return next == null || next == JS_CLOSE_BRACE
        }

        @JvmStatic
        fun notOpenBrace(builder_: PsiBuilder, level: Int): Boolean {
            val nextTokenType = builder_.lookAhead(1)
            return nextTokenType != JS_OPEN_BRACE
        }

        @JvmStatic
        fun isNext(builder: PsiBuilder, level: Int, elementType:IElementType) : Boolean {
            val tokenType = nextNonEmptyNonCommentToken(builder)
            return tokenType != null && tokenType == elementType
        }

        @JvmStatic
        fun nextNonEmptyNonCommentToken(builder: PsiBuilder): IElementType? {
            var lookaheadIndex = 1
            var elementType:IElementType?
            do {
                elementType = builder.lookAhead(lookaheadIndex++)
            } while (elementType != null && isTokenWhitespaceOrComment(elementType))
            return elementType
        }

        private fun isTokenWhitespaceOrComment(token:IElementType): Boolean {
            return token == TokenType.WHITE_SPACE ||
                    token == JS_SINGLE_LINE_COMMENT ||
                    token == JS_BLOCK_COMMENT_START ||
                    token == JS_BLOCK_COMMENT ||
                    token == JS_LINE_TERMINATOR
        }

    }
}