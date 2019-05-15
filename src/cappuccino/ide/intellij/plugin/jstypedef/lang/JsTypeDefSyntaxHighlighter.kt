package cappuccino.ide.intellij.plugin.jstypedef.lang

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes

import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

class JsTypeDefSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer {
        return ObjJLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        if (tokenType == null) {
            return EMPTY_KEYS
        }
        var attrKey: TextAttributesKey? = null
        if (tokenType == ObjJTypes.ObjJ_ID) {
            return EMPTY_KEYS
        } else if (tokenType == ObjJTypes.ObjJ_AT_IMPLEMENTATION ||
                tokenType == ObjJTypes.ObjJ_AT_OUTLET ||
                tokenType == ObjJTypes.ObjJ_AT_ACCESSORS ||
                tokenType == ObjJTypes.ObjJ_AT_END ||
                tokenType == ObjJTypes.ObjJ_AT_IMPORT ||
                tokenType == ObjJTypes.ObjJ_AT_ACTION ||
                tokenType == ObjJTypes.ObjJ_AT_SELECTOR ||
                tokenType == ObjJTypes.ObjJ_AT_CLASS ||
                tokenType == ObjJTypes.ObjJ_AT_GLOBAL ||
                tokenType == ObjJTypes.ObjJ_AT_REF ||
                tokenType == ObjJTypes.ObjJ_AT_DEREF ||
                tokenType == ObjJTypes.ObjJ_AT_PROTOCOL ||
                tokenType == ObjJTypes.ObjJ_AT_OPTIONAL ||
                tokenType == ObjJTypes.ObjJ_AT_REQUIRED ||
                tokenType == ObjJTypes.ObjJ_AT_INTERFACE ||
                tokenType == ObjJTypes.ObjJ_AT_TYPE_DEF ||
                tokenType == ObjJTypes.ObjJ_AT_FRAGMENT
        ) {
            attrKey = AT_STATEMENT
        } else if (tokenType == ObjJTypes.ObjJ_BREAK ||
                tokenType == ObjJTypes.ObjJ_CASE ||
                tokenType == ObjJTypes.ObjJ_CATCH ||
                tokenType == ObjJTypes.ObjJ_CONTINUE ||
                tokenType == ObjJTypes.ObjJ_DEBUGGER ||
                tokenType == ObjJTypes.ObjJ_DEFAULT ||
                tokenType == ObjJTypes.ObjJ_DO ||
                tokenType == ObjJTypes.ObjJ_ELSE ||
                tokenType == ObjJTypes.ObjJ_FINALLY ||
                tokenType == ObjJTypes.ObjJ_FOR ||
                tokenType == ObjJTypes.ObjJ_FUNCTION ||
                tokenType == ObjJTypes.ObjJ_IF ||
                tokenType == ObjJTypes.ObjJ_RETURN ||
                tokenType == ObjJTypes.ObjJ_SWITCH ||
                tokenType == ObjJTypes.ObjJ_THROW ||
                tokenType == ObjJTypes.ObjJ_TRY ||
                tokenType == ObjJTypes.ObjJ_VAR ||
                tokenType == ObjJTypes.ObjJ_WHILE ||
                //tokenType.equals(ObjJTypes.ObjJ_WITH) ||
                tokenType == ObjJTypes.ObjJ_NULL_LITERAL ||
                tokenType == ObjJTypes.ObjJ_NEW ||
                tokenType == ObjJTypes.ObjJ_IN ||
                tokenType == ObjJTypes.ObjJ_INSTANCE_OF ||
                tokenType == ObjJTypes.ObjJ_THIS ||
                tokenType == ObjJTypes.ObjJ_TYPE_OF ||
                tokenType == ObjJTypes.ObjJ_DELETE ||
                tokenType == ObjJTypes.ObjJ_UNDEFINED ||
                tokenType == ObjJTypes.ObjJ_NIL ||
                tokenType == ObjJTypes.ObjJ_BOOLEAN_LITERAL ||
                tokenType == ObjJTypes.ObjJ_MARK ||
                tokenType == ObjJTypes.ObjJ_NIL ||
                tokenType == ObjJTypes.ObjJ_NULL_LITERAL ||
                tokenType == ObjJTypes.ObjJ_UNDEFINED) {
            attrKey = KEYWORD
        } else if (tokenType == ObjJTypes.ObjJ_VAR_TYPE_BOOL ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_DOUBLE ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_FLOAT ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_IBACTION ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_IBOUTLET ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_CHAR ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_SHORT ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_BYTE ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_UNSIGNED ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_SIGNED ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_SEL ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_LONG ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_LONG_LONG ||
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_INT ||
                tokenType == ObjJTypes.ObjJ_VOID) {
            attrKey = VARIABLE_TYPE
        } else if (tokenType == ObjJTypes.ObjJ_PP_DEFINE ||
                tokenType == ObjJTypes.ObjJ_PP_UNDEF ||
                tokenType == ObjJTypes.ObjJ_PP_IF_DEF ||
                tokenType == ObjJTypes.ObjJ_PP_IF_NDEF ||
                tokenType == ObjJTypes.ObjJ_PP_IF ||
                tokenType == ObjJTypes.ObjJ_PP_ELSE ||
                tokenType == ObjJTypes.ObjJ_PP_END_IF ||
                tokenType == ObjJTypes.ObjJ_PP_ELSE_IF ||
                tokenType == ObjJTypes.ObjJ_PP_PRAGMA ||
                tokenType == ObjJTypes.ObjJ_PP_DEFINED ||
                tokenType == ObjJTypes.ObjJ_PP_ERROR ||
                tokenType == ObjJTypes.ObjJ_PP_WARNING ||
                tokenType == ObjJTypes.ObjJ_PP_INCLUDE ||
                tokenType == ObjJTypes.ObjJ_PP_FRAGMENT) {
            attrKey = PRE_PROCESSOR
        } else if (tokenType == ObjJTypes.ObjJ_IMPORT_FRAMEWORK_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUO ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUO ||
                tokenType == ObjJTypes.ObjJ_SELECTOR_LITERAL ||
                tokenType == ObjJTypes.ObjJ_QUO_TEXT) {
            attrKey = STRING
        } else if (tokenType == ObjJTypes.ObjJ_SINGLE_LINE_COMMENT) {
            attrKey = LINE_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_START ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_END ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_BODY) {
            attrKey = BLOCK_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_PRAGMA_MARKER || tokenType == ObjJTypes.ObjJ_REGULAR_EXPRESSION_LITERAL_TOKEN) {
            attrKey = SECONDARY_LITERAL
        }
        return if (attrKey != null) arrayOf(attrKey) else EMPTY_KEYS
    }

    companion object {
        private val EMPTY_KEYS:Array<TextAttributesKey> = arrayOf()
        val ID:TextAttributesKey = createTextAttributesKey("ObjJ_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val AT_STATEMENT:TextAttributesKey = createTextAttributesKey("ObjJ_AT_STATEMENT", DefaultLanguageHighlighterColors.KEYWORD)
        val PRE_PROCESSOR:TextAttributesKey = createTextAttributesKey("ObjJ_PRE_PROC", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE)
        val KEYWORD:TextAttributesKey = createTextAttributesKey("ObjJ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING:TextAttributesKey = createTextAttributesKey("ObjJ_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT:TextAttributesKey = createTextAttributesKey("ObjJ_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT:TextAttributesKey = createTextAttributesKey("ObjJ_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val SECONDARY_LITERAL:TextAttributesKey = createTextAttributesKey("ObjJ_SECONDARY_LITERAL", DefaultLanguageHighlighterColors.CONSTANT)
        val VARIABLE_TYPE:TextAttributesKey = createTextAttributesKey("ObjJ_VARIABLE_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
        val INSTANCE_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_INSTANCE_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val PARAMETER_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_PARAMETER_VARIABLE", DefaultLanguageHighlighterColors.PARAMETER)
        val GLOBAL_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
        val FUNCTION_NAME:TextAttributesKey = createTextAttributesKey("ObjJ_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val GLOBAL_FUNCTION_NAME:TextAttributesKey = createTextAttributesKey("ObjJ_GLOBAL_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val FILE_LEVEL_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_FILE_LEVEL_VARIABLE", DefaultLanguageHighlighterColors.FUNCTION_CALL)

    }
}