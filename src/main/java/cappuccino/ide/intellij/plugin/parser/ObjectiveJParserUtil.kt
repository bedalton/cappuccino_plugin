@file:Suppress("UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.parser

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.util.Key
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import gnu.trove.TObjectLongHashMap

@Suppress("DEPRECATION")
class ObjectiveJParserUtil : GeneratedParserUtilBase() {
    companion object {

        private val ALLOW_SEMICOLONS_KEY = Key.create<Boolean>("ALLOW_SEMICOLONS")
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
            ////LOGGER.info(String.format("In Mode: <%s>: <%b>", mode, getParsingModes(builder_).get(mode) > 0));
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
        fun doAllowSemiColonAfterMethodHeader(builder_: PsiBuilder, level: Int): Boolean {
            return builder_.getUserData(ALLOW_SEMICOLONS_KEY) ?: false
        }

        @JvmStatic
        fun doAllowSemiColonAfterMethodHeader(builder_: PsiBuilder, level: Int, doAllow: String): Boolean {
            builder_.putUserData(ALLOW_SEMICOLONS_KEY, doAllow.equals("true", ignoreCase = true))
            return true
        }

        @JvmStatic
        fun regexPossible(builder_: PsiBuilder, level_: Int): Boolean {

            if (builder_.eof()) {
                return false
            }
            val lastNode = builder_.latestDoneMarker
            val lastTokenType = lastNode?.tokenType
            return lastTokenType == null || lastTokenType != ObjJTypes.ObjJ_ID && lastTokenType != ObjJTypes.ObjJ_NULL_LITERAL && lastTokenType != ObjJTypes.ObjJ_BOOLEAN_LITERAL && lastTokenType != ObjJTypes.ObjJ_THIS && lastTokenType != ObjJTypes.ObjJ_CLOSE_BRACKET && lastTokenType != ObjJTypes.ObjJ_CLOSE_PAREN && lastTokenType != ObjJTypes.ObjJ_OCTAL_INTEGER_LITERAL && lastTokenType != ObjJTypes.ObjJ_DECIMAL_LITERAL && lastTokenType != ObjJTypes.ObjJ_HEX_INTEGER_LITERAL && lastTokenType != ObjJTypes.ObjJ_PLUS_PLUS && lastTokenType != ObjJTypes.ObjJ_MINUS_MINUS

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
            while (ahead === TokenType.WHITE_SPACE || ahead === ObjJ_LINE_TERMINATOR) {
                if (ahead === ObjJ_LINE_TERMINATOR) {
                    hadLineTerminator = true
                }
                ahead = builder_.lookAhead(++i)
            }
            if (eof(builder_, level_))
                return true

            val text = builder_.originalText
            var offset = builder_.currentOffset
            while (text[offset] == ' ' || text[offset] == '\t')
                offset++
            if (text[offset] == '\n')
                return true
            return  ObjJPsiImplUtil.eosToken(ahead, hadLineTerminator)

        }

        @JvmStatic
        fun closeBrace(builder_: PsiBuilder, level: Int): Boolean {
            val next = builder_.lookAhead(1)
            return next == null || next == ObjJTypes.ObjJ_CLOSE_BRACE
        }

        @JvmStatic
        fun notOpenBraceAndNotFunction(builder_: PsiBuilder, level: Int): Boolean {
            val nextTokenType = builder_.lookAhead(1)
            return nextTokenType != ObjJTypes.ObjJ_OPEN_BRACE && nextTokenType != ObjJTypes.ObjJ_FUNCTION
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
                    token == ObjJ_SINGLE_LINE_COMMENT ||
                    token == ObjJ_BLOCK_COMMENT_START ||
                    token == ObjJ_LINE_TERMINATOR
        }

    }
}