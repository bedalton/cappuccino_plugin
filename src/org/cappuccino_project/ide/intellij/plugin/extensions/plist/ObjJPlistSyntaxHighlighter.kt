package org.cappuccino_project.ide.intellij.plugin.extensions.plist

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer.ObjJPlistLexer

import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*

class ObjJPlistSyntaxHighlighter : SyntaxHighlighterBase() {


    override fun getHighlightingLexer(): Lexer {
        return ObjJPlistLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        if (tokenType == null) {
            return EMPTY_KEYS
        }
        var attrKey: TextAttributesKey? = null
        if (tokenType == ObjJPlist_ARRAY_OPEN ||
                tokenType == ObjJPlist_ARRAY_CLOSE ||
                tokenType == ObjJPlist_DICT_OPEN ||
                tokenType == ObjJPlist_DICT_CLOSE ||
                tokenType == ObjJPlist_REAL_OPEN ||
                tokenType == ObjJPlist_REAL_CLOSE ||
                tokenType == ObjJPlist_INTEGER_OPEN ||
                tokenType == ObjJPlist_INTEGER_CLOSE ||
                tokenType == ObjJPlist_DATA_OPEN ||
                tokenType == ObjJPlist_DATA_CLOSE ||
                tokenType == ObjJPlist_DATE_OPEN ||
                tokenType == ObjJPlist_DATE_CLOSE ||
                tokenType == ObjJPlist_STRING_OPEN ||
                tokenType == ObjJPlist_STRING_CLOSE ||
                tokenType == ObjJPlist_TRUE ||
                tokenType == ObjJPlist_FALSE) {
            attrKey = VALUE_TAG
        } else if (tokenType == ObjJPlist_KEY_OPEN || tokenType == ObjJPlist_KEY_CLOSE) {
            attrKey = KEY_TAG
        } else if (tokenType == ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL || tokenType == ObjJPlist_SINGLE_QUOTE_STRING_LITERAL) {
            attrKey = STRING
        } else if (tokenType == ObjJPlist_VERSION ||
                tokenType == ObjJPlist_XML ||
                tokenType == ObjJPlist_CLOSING_HEADER_BRACKET ||
                tokenType == ObjJPlist_OPENING_HEADER_BRACKET ||
                tokenType == ObjJPlist_DOCTYPE_OPEN ||
                tokenType == ObjJPlist_LT ||
                tokenType == ObjJPlist_LT_SLASH ||
                tokenType == ObjJPlist_GT ||
                tokenType == ObjJPlist_SLASH_GT ||
                tokenType == ObjJPlist_PLIST_OPEN ||
                tokenType == ObjJPlist_PLIST_CLOSE) {
            attrKey = KEYWORD
        } else if (tokenType == ObjJPlist_DATA_LITERAL ||
                tokenType == ObjJPlist_STRING_TAG_LITERAL ||
                tokenType == ObjJPlist_INTEGER ||
                tokenType == ObjJPlist_DECIMAL_LITERAL) {
            attrKey = LITERAL_VALUE
        } else if (tokenType == ObjJPlist_COMMENT) {
            attrKey = LINE_COMMENT
        } else if (tokenType == ObjJPlist_XML_TAG_PROPERTY_KEY) {
            attrKey = XML_TAG_ATTRIBUTE
        }
        return if (attrKey != null) arrayOf<TextAttributesKey>(attrKey) else EMPTY_KEYS
    }

    companion object {
        private val EMPTY_KEYS = arrayOfNulls<TextAttributesKey>(0)
        val KEYWORD = createTextAttributesKey("ObjJPlist_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val KEY_TAG = createTextAttributesKey("ObjJPlist_KEY_TAG", DefaultLanguageHighlighterColors.KEYWORD)
        val VALUE_TAG = createTextAttributesKey("ObjJPlist_VALUE_TAG", DefaultLanguageHighlighterColors.KEYWORD)
        val LINE_COMMENT = createTextAttributesKey("ObjJPlist_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING = createTextAttributesKey("ObjJPlist_STRING", DefaultLanguageHighlighterColors.STRING)
        val LITERAL_VALUE = createTextAttributesKey("ObjJPlist_LITERAL_VALUE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val XML_TAG_ATTRIBUTE = createTextAttributesKey("ObjJPlist_LITERAL_VALUE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    }
}
