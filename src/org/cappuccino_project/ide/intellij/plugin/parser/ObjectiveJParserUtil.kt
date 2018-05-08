package org.cappuccino_project.ide.intellij.plugin.parser

import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import gnu.trove.TObjectLongHashMap
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

import java.util.Objects

class ObjectiveJParserUtil : GeneratedParserUtilBase() {
    companion object {

        private var doAllowSemiColonAfterMethodHeader = false
        private val MODES_KEY = Key.create<TObjectLongHashMap<String>>("MODES_KEY")

        private fun getParsingModes(builder_: PsiBuilder): TObjectLongHashMap<String> {
            var flags = builder_.getUserDataUnprotected(MODES_KEY)
            if (flags == null) builder_.putUserDataUnprotected(MODES_KEY, flags = TObjectLongHashMap<String>())
            return flags
        }

        fun inMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            //LOGGER.log(Level.INFO, String.format("In Mode: <%s>: <%b>", mode, getParsingModes(builder_).get(mode) > 0));
            return getParsingModes(builder_).get(mode) > 0
        }


        fun notInMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            return getParsingModes(builder_).get(mode) < 1
        }

        fun enterMode(builder_: PsiBuilder, level: Int, mode: String): Boolean {
            val flags = getParsingModes(builder_)
            if (!flags.increment(mode)) flags.put(mode, 1)
            return true
        }

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

        override fun adapt_builder_(root: IElementType, builder: PsiBuilder, parser: PsiParser, tokenSets: Array<TokenSet>): PsiBuilder {
            val result = GeneratedParserUtilBase.adapt_builder_(root, builder, parser, tokenSets)
            GeneratedParserUtilBase.ErrorState.get(result).altMode = true
            return result
        }

        fun doAllowSemiColonAfterMethodHeader(builder_: PsiBuilder, level: Int): Boolean {
            return doAllowSemiColonAfterMethodHeader
        }

        fun doAllowSemiColonAfterMethodHeader(builder_: PsiBuilder, level: Int, doAllow: String): Boolean {
            doAllowSemiColonAfterMethodHeader = doAllow.equals("true", ignoreCase = true)
            return true
        }

        fun regexPossible(builder_: PsiBuilder, level_: Int): Boolean {

            if (builder_.eof()) {
                return false
            }
            val lastNode = builder_.latestDoneMarker
            val lastTokenType = lastNode?.tokenType
            return lastTokenType == null || lastTokenType != ObjJTypes.ObjJ_ID && lastTokenType != ObjJTypes.ObjJ_NULL_LITERAL && lastTokenType != ObjJTypes.ObjJ_BOOLEAN_LITERAL && lastTokenType != ObjJTypes.ObjJ_THIS && lastTokenType != ObjJTypes.ObjJ_CLOSE_BRACKET && lastTokenType != ObjJTypes.ObjJ_CLOSE_PAREN && lastTokenType != ObjJTypes.ObjJ_OCTAL_INTEGER_LITERAL && lastTokenType != ObjJTypes.ObjJ_DECIMAL_LITERAL && lastTokenType != ObjJTypes.ObjJ_HEX_INTEGER_LITERAL && lastTokenType != ObjJTypes.ObjJ_PLUS_PLUS && lastTokenType != ObjJTypes.ObjJ_MINUS_MINUS

        }

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
        fun lineTerminatorAhead(builder_: PsiBuilder, level_: Int): Boolean {
            // Get the token ahead of the current index.

            var i = 0
            var ahead = builder_.lookAhead(i)
            var hadLineTerminator = false
            while (ahead === com.intellij.psi.TokenType.WHITE_SPACE || ahead === ObjJTypes.ObjJ_LINE_TERMINATOR) {
                if (ahead === ObjJTypes.ObjJ_LINE_TERMINATOR) {
                    hadLineTerminator = true
                }
                ahead = builder_.lookAhead(++i)
            }
            return GeneratedParserUtilBase.eof(builder_, level_) || ObjJPsiImplUtil.eosToken(ahead, hadLineTerminator)

        }

        fun closeBrace(builder_: PsiBuilder, level: Int): Boolean {
            val next = builder_.lookAhead(1)
            return next == null || next == ObjJTypes.ObjJ_CLOSE_BRACE
        }

        fun notOpenBraceAndNotFunction(builder_: PsiBuilder, level: Int): Boolean {
            val nextTokenType = builder_.lookAhead(1)
            return nextTokenType != ObjJTypes.ObjJ_OPEN_BRACE && nextTokenType != ObjJTypes.ObjJ_FUNCTION
        }
    }
}