package cappuccino.ide.intellij.plugin.jsdef;

import cappuccino.ide.intellij.plugin.inspections.JsTypeDefTokenSets
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class JsTypeDefSyntaxHighlighter : SyntaxHighlighterBase(){

        override fun getHighlightingLexer(): Lexer{
            return JsTypeDefLexer()
        }

        override fun getTokenHighlights(tokenType:IElementType?):Array<TextAttributesKey> {

            val attributesKey:TextAttributesKey? = when (tokenType) {
                in JsTypeDefTokenSets.KEYWORDS -> KEYWORD
                else -> return EMPTY_KEYS
            }
            return if (attributesKey != null) arrayOf(attributesKey) else EMPTY_KEYS


        }

    companion object {
        private val EMPTY_KEYS: Array<TextAttributesKey> = arrayOf()
        // Colors
        val ID:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val AT_STATEMENT:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_AT_STATEMENT", DefaultLanguageHighlighterColors.KEYWORD)
        val PRE_PROCESSOR:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_PRE_PROC", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE)
        val KEYWORD:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT:TextAttributesKey = TextAttributesKey.createTextAttributesKey("JsTypeDef_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)

    }
}