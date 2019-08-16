package cappuccino.ide.intellij.plugin.jstypedef.lang

import cappuccino.ide.intellij.plugin.jstypedef.lexer.JsTypeDefLexer
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class JsTypeDefSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer {
        return JsTypeDefLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        if (tokenType == null) {
            return EMPTY_KEYS
        }
        var attrKey: TextAttributesKey? = null
        if (tokenType == JS_ID) {
            return EMPTY_KEYS
        } else if (tokenType == JS_NULL_TYPE ||
                tokenType == JS_INTERFACE ||
                tokenType == JS_CLASS_KEYWORD ||
                tokenType == JS_KEYS_KEYWORD ||
                tokenType == JS_TYPE_MAP_KEYWORD ||
                tokenType == JS_FUNCTION_KEYWORD ||
                tokenType == JS_MODULE_KEYWORD ||
                tokenType == JS_KEYOF ||
                tokenType == JS_READONLY ||
                tokenType == JS_STATIC_KEYWORD ||
                tokenType == JS_EXTENDS ||
                tokenType == JS_CONST ||
                tokenType == JS_DECLARE ||
                tokenType == JS_AT_QUIET ||
                tokenType == JS_AT_SILENT ||
                tokenType == JS_ALIAS ||
                tokenType == JS_VAR
                ) {
            attrKey = KEYWORD
        } else if (tokenType == JS_STRING_LITERAL ||
                tokenType == JS_DOUBLE_QUOTE_STRING ||
                tokenType == JS_SINGLE_QUOTE_STRING) {
            attrKey = STRING
        } else if (tokenType == JS_SINGLE_LINE_COMMENT) {
            attrKey = LINE_COMMENT
        } else if (tokenType == JS_BLOCK_COMMENT ||
                tokenType == JS_BLOCK_COMMENT_START ||
                tokenType == JS_BLOCK_COMMENT_END ||
                tokenType == JS_BLOCK_COMMENT_BODY) {
            attrKey = BLOCK_COMMENT
        }
        return if (attrKey != null) arrayOf(attrKey) else EMPTY_KEYS
    }

    companion object {
        private val EMPTY_KEYS: Array<TextAttributesKey> = arrayOf()
        val ID: TextAttributesKey = createTextAttributesKey("JsTypeDef_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val KEYWORD: TextAttributesKey = createTextAttributesKey("JsTypeDef_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING: TextAttributesKey = createTextAttributesKey("JsTypeDef_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT: TextAttributesKey = createTextAttributesKey("JsTypeDef_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT: TextAttributesKey = createTextAttributesKey("JsTypeDef_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val SECONDARY_LITERAL: TextAttributesKey = createTextAttributesKey("JsTypeDef_SECONDARY_LITERAL", DefaultLanguageHighlighterColors.CONSTANT)
        val VARIABLE_TYPE: TextAttributesKey = createTextAttributesKey("JsTypeDef_VARIABLE_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
        val INSTANCE_VARIABLE: TextAttributesKey = createTextAttributesKey("JsTypeDef_INSTANCE_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val PARAMETER_VARIABLE: TextAttributesKey = createTextAttributesKey("JsTypeDef_PARAMETER_VARIABLE", DefaultLanguageHighlighterColors.PARAMETER)
        val GLOBAL_VARIABLE: TextAttributesKey = createTextAttributesKey("JsTypeDef_GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
        val FUNCTION_NAME: TextAttributesKey = createTextAttributesKey("JsTypeDef_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val GLOBAL_FUNCTION_NAME: TextAttributesKey = createTextAttributesKey("JsTypeDef_GLOBAL_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val FILE_LEVEL_VARIABLE: TextAttributesKey = createTextAttributesKey("JsTypeDef_FILE_LEVEL_VARIABLE", DefaultLanguageHighlighterColors.FUNCTION_CALL)

    }
}