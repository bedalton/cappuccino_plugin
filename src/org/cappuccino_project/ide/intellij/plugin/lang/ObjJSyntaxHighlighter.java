package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.lexer.ObjJLexer;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class ObjJSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public static final TextAttributesKey ID =
            createTextAttributesKey("ObjectiveJ_ID", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey AT_STATEMENT =
            createTextAttributesKey("ObjectiveJ_AT_STATEMENT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey PRE_PROCESSOR =
            createTextAttributesKey("ObjectiveJ_PRE_PROC", DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE);
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ObjectiveJ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("ObjectiveJ_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("ObjectiveJ_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("ObjectiveJ_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey SECONDARY_LITERAL =
            createTextAttributesKey("ObjectiveJ_PREPROCESSOR_VAR", DefaultLanguageHighlighterColors.CONSTANT);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new ObjJLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType == null) {
            return EMPTY_KEYS;
        }
        TextAttributesKey attrKey = null;
        if (tokenType.equals(ObjJTypes.ObjJ_ID)) {
            return EMPTY_KEYS;
        } else if (
                tokenType.equals(ObjJTypes.ObjJ_AT_IMPLEMENTATION) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_OUTLET) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_ACCESSORS) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_END) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_IMPORT) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_ACTION) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_SELECTOR) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_CLASS) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_GLOBAL) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_REF) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_DEREF) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_PROTOCOL) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_OPTIONAL) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_REQUIRED) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_INTERFACE) ||
                tokenType.equals(ObjJTypes.ObjJ_AT_TYPE_DEF)
        ) {
            attrKey = AT_STATEMENT;
        } else if (tokenType.equals(ObjJTypes.ObjJ_BREAK) ||
                tokenType.equals(ObjJTypes.ObjJ_CASE) ||
                tokenType.equals(ObjJTypes.ObjJ_CATCH) ||
                tokenType.equals(ObjJTypes.ObjJ_CONTINUE) ||
                tokenType.equals(ObjJTypes.ObjJ_DEBUGGER) ||
                tokenType.equals(ObjJTypes.ObjJ_DEFAULT) ||
                tokenType.equals(ObjJTypes.ObjJ_DO) ||
                tokenType.equals(ObjJTypes.ObjJ_ELSE) ||
                tokenType.equals(ObjJTypes.ObjJ_FINALLY) ||
                tokenType.equals(ObjJTypes.ObjJ_FOR) ||
                tokenType.equals(ObjJTypes.ObjJ_FUNCTION) ||
                tokenType.equals(ObjJTypes.ObjJ_IF) ||
                tokenType.equals(ObjJTypes.ObjJ_RETURN) ||
                tokenType.equals(ObjJTypes.ObjJ_SWITCH) ||
                tokenType.equals(ObjJTypes.ObjJ_THROW) ||
                tokenType.equals(ObjJTypes.ObjJ_TRY) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR) ||
                tokenType.equals(ObjJTypes.ObjJ_WHILE) ||
                //tokenType.equals(ObjJTypes.ObjJ_WITH) ||
                tokenType.equals(ObjJTypes.ObjJ_NULL_LITERAL) ||
                tokenType.equals(ObjJTypes.ObjJ_NEW) ||
                tokenType.equals(ObjJTypes.ObjJ_IN) ||
                tokenType.equals(ObjJTypes.ObjJ_INSTANCE_OF) ||
                tokenType.equals(ObjJTypes.ObjJ_THIS) ||
                tokenType.equals(ObjJTypes.ObjJ_TYPE_OF) ||
                tokenType.equals(ObjJTypes.ObjJ_DELETE) ||
                tokenType.equals(ObjJTypes.ObjJ_UNDEFINED) ||
                tokenType.equals(ObjJTypes.ObjJ_NIL) ||
                tokenType.equals(ObjJTypes.ObjJ_BOOLEAN_LITERAL) ||
                tokenType.equals(ObjJTypes.ObjJ_MARK) ||
                tokenType.equals(ObjJTypes.ObjJ_NIL) ||
                tokenType.equals(ObjJTypes.ObjJ_NULL_LITERAL) ||
                tokenType.equals(ObjJTypes.ObjJ_VOID) ||
                tokenType.equals(ObjJTypes.ObjJ_UNDEFINED)
                ) {
            attrKey = KEYWORD;
        } else if (
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_BOOL) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_DOUBLE) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_FLOAT) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_IBACTION) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_IBOUTLET) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_CHAR) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_SHORT) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_BYTE) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_UNSIGNED) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_SIGNED) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_SEL) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_LONG) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_LONG_LONG) ||
                tokenType.equals(ObjJTypes.ObjJ_VAR_TYPE_INT)
                ) {
            attrKey = KEYWORD;
        } else if (
                tokenType.equals(ObjJTypes.ObjJ_PP_DEFINE) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_UNDEF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_IF_DEF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_IF_NDEF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_IF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_ELSE) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_END_IF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_ELSE_IF) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_PRAGMA) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_DEFINED) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_ERROR) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_WARNING) ||
                tokenType.equals(ObjJTypes.ObjJ_PP_INCLUDE)
                ) {
            attrKey = PRE_PROCESSOR;
        } else if (
                tokenType.equals(ObjJTypes.ObjJ_IMPORT_FRAMEWORK_LITERAL) ||
                        tokenType.equals(ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL) ||
                        tokenType.equals(ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL)
        ){
            attrKey = STRING;
        } else if (tokenType.equals(ObjJTypes.ObjJ_SINGLE_LINE_COMMENT)) {
            attrKey = LINE_COMMENT;
        } else if (tokenType.equals(ObjJTypes.ObjJ_BLOCK_COMMENT)) {
            attrKey = BLOCK_COMMENT;
        } else if (
                tokenType.equals(ObjJTypes.ObjJ_PRAGMA_MARKER) ||
                tokenType.equals(ObjJTypes.ObjJ_REGULAR_EXPRESSION_LITERAL)
        ) {
            attrKey = SECONDARY_LITERAL;
        }
        return attrKey != null ? new TextAttributesKey[]{attrKey} : EMPTY_KEYS;
    }
}
