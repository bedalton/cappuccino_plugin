package cappuccino.ide.intellij.plugin.lang

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes

import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import java.awt.Color
import java.awt.Font

class ObjJSyntaxHighlighter : SyntaxHighlighterBase() {

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
                tokenType == ObjJTypes.ObjJ_AT_TYPE_DEF) {
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
                tokenType == ObjJTypes.ObjJ_VOID ||
                tokenType == ObjJTypes.ObjJ_UNDEFINED) {
            attrKey = KEYWORD
        } /*else if (tokenType == ObjJTypes.ObjJ_VAR_TYPE_BOOL ||
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
                tokenType == ObjJTypes.ObjJ_VAR_TYPE_INT) {
            attrKey = KEYWORD
        } */ else if (tokenType == ObjJTypes.ObjJ_PP_DEFINE ||
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
                tokenType == ObjJTypes.ObjJ_PP_INCLUDE) {
            attrKey = PRE_PROCESSOR
        } else if (tokenType == ObjJTypes.ObjJ_IMPORT_FRAMEWORK_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL ||
                tokenType == ObjJTypes.ObjJ_SINGLE_QUO ||
                tokenType == ObjJTypes.ObjJ_DOUBLE_QUO ||
                tokenType == ObjJTypes.ObjJ_QUO_TEXT) {
            attrKey = STRING
        } else if (tokenType == ObjJTypes.ObjJ_SINGLE_LINE_COMMENT) {
            attrKey = LINE_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_START ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_END ||
                tokenType == ObjJTypes.ObjJ_BLOCK_COMMENT_TEXT) {
            attrKey = BLOCK_COMMENT
        } else if (tokenType == ObjJTypes.ObjJ_PRAGMA_MARKER || tokenType == ObjJTypes.ObjJ_REGULAR_EXPRESSION_LITERAL) {
            attrKey = SECONDARY_LITERAL
        }
        return if (attrKey != null) arrayOf(attrKey) else EMPTY_KEYS
    }

    companion object {
        private val EMPTY_KEYS:Array<TextAttributesKey> = arrayOf()
        val ID:TextAttributesKey = createTextAttributesKey("ObjectiveJ_ID", DefaultLanguageHighlighterColors.IDENTIFIER)
        val AT_STATEMENT:TextAttributesKey = createTextAttributesKey("ObjectiveJ_AT_STATEMENT", DefaultLanguageHighlighterColors.KEYWORD)
        val PRE_PROCESSOR:TextAttributesKey = createTextAttributesKey("ObjectiveJ_PRE_PROC", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE)
        val KEYWORD:TextAttributesKey = createTextAttributesKey("ObjectiveJ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING:TextAttributesKey = createTextAttributesKey("ObjectiveJ_STRING", DefaultLanguageHighlighterColors.STRING)
        val LINE_COMMENT:TextAttributesKey = createTextAttributesKey("ObjectiveJ_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT:TextAttributesKey = createTextAttributesKey("ObjectiveJ_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val SECONDARY_LITERAL:TextAttributesKey = createTextAttributesKey("ObjectiveJ_PREPROCESSOR_VAR", DefaultLanguageHighlighterColors.CONSTANT)
        val VARIABLE_TYPE:TextAttributesKey = TextAttributesKeyBuilder("ObjectiveJ_VARIABLE_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME)
                .setForegroundColor(Color(44, 124, 224))
                .setFontType(Font.BOLD)
                .build()
        val INSTANCE_VAR:TextAttributesKey = TextAttributesKeyBuilder("ObjectiveJ_INSTANCE_VARS")
                .setForegroundColor(Color(113, 41, 221))
                .build()

    }
}

class TextAttributesKeyBuilder (private val key:String, private val base:TextAttributesKey? = null){

    private var _foregroundColor:Color? = base?.defaultAttributes?.foregroundColor
    private var _backgroundColor:Color? = base?.defaultAttributes?.backgroundColor
    private var _errorStripeColor:Color? = base?.defaultAttributes?.errorStripeColor
    private var _effectColor:Color? = base?.defaultAttributes?.effectColor
    private var _effectType : EffectType? = base?.defaultAttributes?.effectType
    private var _fontType : Int? = base?.defaultAttributes?.fontType


    fun setForegroundColor(color:Color?) : TextAttributesKeyBuilder {
        _foregroundColor = color
        return this
    }

    fun setBackgroundColor(color:Color?) : TextAttributesKeyBuilder {
        _backgroundColor = color
        return this
    }
    fun setEffectColor(color:Color?) : TextAttributesKeyBuilder {
        _effectColor = color
        return this
    }

    fun setErrorStripeColor(color:Color?) : TextAttributesKeyBuilder {
        _errorStripeColor = color
        return this;
    }

    fun setEffectType(type:EffectType?) : TextAttributesKeyBuilder {
        _effectType = type
        return this
    }

    fun setFontType(type:Int?) : TextAttributesKeyBuilder {
        _fontType = type
        return this
    }

    fun build() : TextAttributesKey {
        val out = if (base != null) {
            createTextAttributesKey(key, base)
        } else {
            createTextAttributesKey(key)
        }
        val defaultAttributes = out.defaultAttributes
        if (_foregroundColor != null) {
            defaultAttributes.foregroundColor = _foregroundColor
        }
        if (_backgroundColor != null) {
            defaultAttributes.backgroundColor = _backgroundColor
        }
        if (_errorStripeColor != null) {
            defaultAttributes.errorStripeColor = _errorStripeColor
        }
        if (_effectColor != null) {
            defaultAttributes.effectColor = _effectColor
        }
        if (_effectType != null) {
            defaultAttributes.effectType = _effectType
        }
        val fontType:Int? = _fontType
        if (fontType != null) {
            defaultAttributes.fontType = fontType.toInt()
        }
        return out
    }

    companion object {
        fun toBuilder(key:String, baseBuilder:TextAttributesKeyBuilder) : TextAttributesKeyBuilder {
            val newBuilder = TextAttributesKeyBuilder(key, baseBuilder.base)
            newBuilder.setForegroundColor(baseBuilder._foregroundColor)
            newBuilder.setBackgroundColor(baseBuilder._backgroundColor)
            newBuilder.setErrorStripeColor(baseBuilder._errorStripeColor)
            newBuilder.setEffectColor(baseBuilder._effectColor)
            newBuilder.setEffectType(baseBuilder._effectType)
            newBuilder.setFontType(baseBuilder._fontType)
            return newBuilder
        }
    }



}
