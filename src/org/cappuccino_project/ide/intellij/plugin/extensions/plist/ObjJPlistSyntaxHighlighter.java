package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer.ObjJPlistLexer;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;
import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;

public class ObjJPlistSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ObjJPlist_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey KEY_TAG =
            createTextAttributesKey("ObjJPlist_KEY_TAG", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey VALUE_TAG =
            createTextAttributesKey("ObjJPlist_VALUE_TAG", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("ObjJPlist_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("ObjJPlist_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LITERAL_VALUE =
            createTextAttributesKey("ObjJPlist_LITERAL_VALUE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey XML_TAG_ATTRIBUTE =
            createTextAttributesKey("ObjJPlist_LITERAL_VALUE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);


    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new ObjJPlistLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType == null) {
            return EMPTY_KEYS;
        }
        TextAttributesKey attrKey = null;
        if (
            tokenType.equals(ObjJPlist_ARRAY_OPEN) ||
            tokenType.equals(ObjJPlist_ARRAY_CLOSE) ||
            tokenType.equals(ObjJPlist_DICT_OPEN) ||
            tokenType.equals(ObjJPlist_DICT_CLOSE) ||
            tokenType.equals(ObjJPlist_REAL_OPEN) ||
            tokenType.equals(ObjJPlist_REAL_CLOSE) ||
            tokenType.equals(ObjJPlist_INTEGER_OPEN) ||
            tokenType.equals(ObjJPlist_INTEGER_CLOSE) ||
            tokenType.equals(ObjJPlist_DATA_OPEN) ||
            tokenType.equals(ObjJPlist_DATA_CLOSE) ||
            tokenType.equals(ObjJPlist_DATE_OPEN) ||
            tokenType.equals(ObjJPlist_DATE_CLOSE) ||
            tokenType.equals(ObjJPlist_STRING_OPEN) ||
            tokenType.equals(ObjJPlist_STRING_CLOSE) ||
            tokenType.equals(ObjJPlist_TRUE) ||
            tokenType.equals(ObjJPlist_FALSE)
        ) {
            attrKey = VALUE_TAG;
        } else if (
            tokenType.equals(ObjJPlist_KEY_OPEN) ||
            tokenType.equals(ObjJPlist_KEY_CLOSE)
        ) {
            attrKey = KEY_TAG;
        } else if (
            tokenType.equals(ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL) ||
            tokenType.equals(ObjJPlist_SINGLE_QUOTE_STRING_LITERAL)
        ) {
            attrKey = STRING;
        } else if (
            tokenType.equals(ObjJPlist_VERSION) ||
            tokenType.equals(ObjJPlist_XML) ||
            tokenType.equals(ObjJPlist_CLOSING_HEADER_BRACKET) ||
            tokenType.equals(ObjJPlist_OPENING_HEADER_BRACKET) ||
            tokenType.equals(ObjJPlist_DOCTYPE_OPEN) ||
            tokenType.equals(ObjJPlist_LT) ||
            tokenType.equals(ObjJPlist_LT_SLASH) ||
            tokenType.equals(ObjJPlist_GT) ||
            tokenType.equals(ObjJPlist_SLASH_GT) ||
            tokenType.equals(ObjJPlist_PLIST_OPEN) ||
            tokenType.equals(ObjJPlist_PLIST_CLOSE)
        ) {
            attrKey = KEYWORD;
        } else if (
            tokenType.equals(ObjJPlist_DATA_LITERAL) ||
            tokenType.equals(ObjJPlist_STRING_TAG_LITERAL) ||
            tokenType.equals(ObjJPlist_INTEGER) ||
            tokenType.equals(ObjJPlist_DECIMAL_LITERAL)
        ) {
            attrKey = LITERAL_VALUE;
        } else if (
            tokenType.equals(ObjJPlist_COMMENT)
        ) {
            attrKey = LINE_COMMENT;
        } else if (
                tokenType.equals(ObjJPlist_XML_TAG_PROPERTY_KEY)
        ) {
            attrKey = XML_TAG_ATTRIBUTE;
        }
        return attrKey != null ? new TextAttributesKey[]{attrKey} : EMPTY_KEYS;
    }
}
