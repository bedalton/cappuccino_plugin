package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentParsableBlockToken
import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentParsableBlockToken.OBJJ_DOC_COMMENT_PARSABLE_BLOCK
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTempTextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import java.awt.Color

class ObjJSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer {
        return ObjJLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        if (tokenType == null) {
            return EMPTY_KEYS
        }
        val attrKey: TextAttributesKey? = if (tokenType == ObjJTypes.ObjJ_ID)
            return EMPTY_KEYS
        else if (tokenType == ObjJTypes.ObjJ_DOT)
            DOT
        else if (tokenType == ObjJTypes.ObjJ_COMMA)
            COMMA
        else if (tokenType == ObjJTypes.ObjJ_SEMI_COLON)
            SEMICOLON
        else if (tokenType == ObjJTypes.ObjJ_OPEN_BRACE || tokenType == ObjJTypes.ObjJ_CLOSE_BRACE)
            BRACES
        else if (tokenType == ObjJTypes.ObjJ_OPEN_PAREN || tokenType == ObjJTypes.ObjJ_CLOSE_PAREN)
            PARENTHESIS
        else if (tokenType == ObjJTypes.ObjJ_OPEN_BRACKET || tokenType == ObjJTypes.ObjJ_CLOSE_BRACKET)
            BRACKETS
        else if (
            tokenType == ObjJTypes.ObjJ_MULTIPLY ||
            tokenType == ObjJTypes.ObjJ_MULTIPLY_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_DIVIDE ||
            tokenType == ObjJTypes.ObjJ_DIVIDE_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_PLUS ||
            tokenType == ObjJTypes.ObjJ_PLUS_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_PLUS_PLUS ||
            tokenType == ObjJTypes.ObjJ_MINUS ||
            tokenType == ObjJTypes.ObjJ_MINUS_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_MINUS_MINUS ||
            tokenType == ObjJTypes.ObjJ_LEFT_SHIFT_ARITHMATIC ||
            tokenType == ObjJTypes.ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_RIGHT_SHIFT_ARITHMATIC ||
            tokenType == ObjJTypes.ObjJ_RIGHT_SHIFT_LOGICAL ||
            tokenType == ObjJTypes.ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_LESS_THAN ||
            tokenType == ObjJTypes.ObjJ_LESS_THAN_EQUALS ||
            tokenType == ObjJTypes.ObjJ_GREATER_THAN ||
            tokenType == ObjJTypes.ObjJ_GREATER_THAN_EQUALS ||
            tokenType == ObjJTypes.ObjJ_EQUALS ||
            tokenType == ObjJTypes.ObjJ_NOT_EQUALS ||
            tokenType == ObjJTypes.ObjJ_IDENTITY_NOT_EQUALS ||
            tokenType == ObjJTypes.ObjJ_IDENTITY_EQUALS ||
            tokenType == ObjJTypes.ObjJ_BIT_AND ||
            tokenType == ObjJTypes.ObjJ_BIT_AND_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_BIT_OR ||
            tokenType == ObjJTypes.ObjJ_BIT_OR_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_BIT_XOR ||
            tokenType == ObjJTypes.ObjJ_BIT_XOR_ASSIGN ||
            tokenType == ObjJTypes.ObjJ_BIT_NOT
        )
            OPERATORS
            else if (tokenType == ObjJTypes.ObjJ_AT_IMPLEMENTATION ||
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
            AT_STATEMENT
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
                tokenType == ObjJTypes.ObjJ_IF ||
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
            KEYWORD
        } else if (tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_BOOL ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_DOUBLE ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_FLOAT ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_IBACTION ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_IBOUTLET ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_CHAR ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_SHORT ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_BYTE ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_UNSIGNED ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_SIGNED ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_SEL ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_LONG ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_LONG_LONG ||
                tokenType == ObjJTypes.ObjJ_VARIABLE_TYPE_INT ||
                tokenType == ObjJTypes.ObjJ_VOID) {
            VARIABLE_TYPE
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
            PRE_PROCESSOR
        } else if (tokenType == ObjJTypes.ObjJ_IMPORT_FRAMEWORK_LITERAL ||
                tokenType == ObjJTypes.ObjJ_FILE_NAME_AS_IMPORT_STRING ||
                tokenType == ObjJTypes.ObjJ_FRAMEWORK_NAME ||
                tokenType == ObjJTypes.ObjJ_FILE_NAME_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUO ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUO ||
                tokenType == ObjJTypes.ObjJ_SELECTOR_LITERAL ||
                tokenType == ObjJTypes.ObjJ_AT ||
                tokenType == ObjJTypes.ObjJ_QUO_TEXT) {
            STRING
        } else if (tokenType == ObjJTypes.ObjJ_SINGLE_LINE_COMMENT) {
            LINE_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_START ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_END ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_BODY ||
                tokenType == OBJJ_DOC_COMMENT_PARSABLE_BLOCK
                ) {
            BLOCK_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_PRAGMA_MARKER || tokenType == ObjJTypes.ObjJ_REGULAR_EXPRESSION_LITERAL_TOKEN) {
            SECONDARY_LITERAL
        } else
            null

        return if (attrKey != null) arrayOf(attrKey) else EMPTY_KEYS
    }

    companion object {
        val LOCAL_VARIABLE: TextAttributesKey = createTextAttributesKey("ObjJ_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val CLASS: TextAttributesKey = createTextAttributesKey("ObjJ_CLASS", DefaultLanguageHighlighterColors.CLASS_NAME)
        val CLASS_REFERENCE: TextAttributesKey = createTextAttributesKey("ObjJ_CLASS_REFERENCE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
        private val EMPTY_KEYS:Array<TextAttributesKey> = arrayOf()
        val ID:TextAttributesKey = createTextAttributesKey("ObjJ_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val AT_STATEMENT:TextAttributesKey = createTextAttributesKey("ObjJ_AT_STATEMENT", DefaultLanguageHighlighterColors.MARKUP_ENTITY)
        val PRE_PROCESSOR:TextAttributesKey = createTextAttributesKey("ObjJ_PRE_PROC", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE)
        val KEYWORD:TextAttributesKey = createTextAttributesKey("ObjJ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING:TextAttributesKey = createTextAttributesKey("ObjJ_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT:TextAttributesKey = createTextAttributesKey("ObjJ_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT:TextAttributesKey = createTextAttributesKey("ObjJ_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val SECONDARY_LITERAL:TextAttributesKey = createTextAttributesKey("ObjJ_SECONDARY_LITERAL", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        val VARIABLE_TYPE:TextAttributesKey = createTextAttributesKey("ObjJ_VARIABLE_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME)
        val VARIABLE_TYPE_WITH_ERROR:TextAttributesKey = VARIABLE_TYPE.asErrorAttribute
        val INSTANCE_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_INSTANCE_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val PARAMETER_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_PARAMETER_VARIABLE", DefaultLanguageHighlighterColors.PARAMETER)
        val GLOBAL_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
        val FUNCTION_NAME:TextAttributesKey = createTextAttributesKey("ObjJ_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val JS_TYPEDEF_FUNCTION_NAME:TextAttributesKey = createTextAttributesKey("ObjJ_JS_TYPEDEF_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val GLOBAL_FUNCTION_NAME:TextAttributesKey = createTextAttributesKey("ObjJ_GLOBAL_FUNCTION_NAME", DefaultLanguageHighlighterColors.CONSTANT)
        val FILE_LEVEL_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_FILE_LEVEL_VARIABLE", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val JS_TYPEDEF_VARIABLE:TextAttributesKey = createTextAttributesKey("ObjJ_JS_TYPEDEF_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
        val SELECTOR: TextAttributesKey = createTextAttributesKey("ObjJ_SELECTOR", DefaultLanguageHighlighterColors.LABEL)
        val SELECTOR_DECLARATION: TextAttributesKey = createTextAttributesKey("ObjJ_SELECTOR_DECLARATION", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
        val OPERATORS: TextAttributesKey = createTextAttributesKey("ObjJ_OPERATORS", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PROTOCOL_REFERENCE: TextAttributesKey = createTextAttributesKey("ObjJ_PROTOCOL_REFERENCE", DefaultLanguageHighlighterColors.INTERFACE_NAME)
        val PARENTHESIS: TextAttributesKey = createTextAttributesKey("ObjJ_PARENTHESIS", DefaultLanguageHighlighterColors.PARENTHESES)
        val COMMA: TextAttributesKey = createTextAttributesKey("ObjJ_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val SEMICOLON: TextAttributesKey = createTextAttributesKey("ObjJ_SEMI_COLON", DefaultLanguageHighlighterColors.SEMICOLON)
        val BRACES: TextAttributesKey = createTextAttributesKey("ObjJ_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS: TextAttributesKey = createTextAttributesKey("ObjJ_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val DOT: TextAttributesKey = createTextAttributesKey("ObjJ_DOT", DefaultLanguageHighlighterColors.DOT)

    }
}

private val TextAttributesKey.asErrorAttribute:TextAttributesKey get() {
    val attributes = this.defaultAttributes.clone()
    attributes.effectType = EffectType.WAVE_UNDERSCORE
    attributes.effectColor = Color.RED.brighter()
    attributes.errorStripeColor = Color.RED.brighter()
    val name = this.externalName + "_ERROR"
    val temp = createTempTextAttributesKey(name, attributes)
    return createTextAttributesKey(name, temp)
}