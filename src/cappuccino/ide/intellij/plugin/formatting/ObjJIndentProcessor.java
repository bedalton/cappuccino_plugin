package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition;
import cappuccino.ide.intellij.plugin.psi.utils.ObjJTreeUtilKt;
import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*;
import static com.intellij.psi.TokenType.WHITE_SPACE;

public class ObjJIndentProcessor {

    public static final TokenSet EXPRESSIONS = TokenSet
            .create(ObjJ_ARRAY_INDEX_SELECTOR, ObjJ_VARIABLE_DECLARATION, ObjJ_FUNCTION_CALL, ObjJ_LEFT_EXPR, ObjJ_RIGHT_EXPR, ObjJ_EXPR);

    public static final TokenSet BLOCKS = TokenSet.create(ObjJ_BLOCK_ELEMENT, ObjJ_BRACKET_LESS_BLOCK, ObjJ_METHOD_BLOCK, ObjJ_PROTOCOL_SCOPED_BLOCK,
            ObjJ_STATEMENT_OR_BLOCK);

    public static final TokenSet COMMENTS = ObjJParserDefinition.Companion.getCOMMENTS();

    public static final TokenSet CLASS_DECLARATIONS = TokenSet.create(ObjJ_IMPLEMENTATION_DECLARATION, ObjJ_PROTOCOL_DECLARATION);

    private final CommonCodeStyleSettings settings;

    public ObjJIndentProcessor(CommonCodeStyleSettings settings) {
        this.settings = settings;
    }

    public Indent getChildIndent(final ASTNode node, final FormattingMode mode) {
        final IElementType elementType = node.getElementType();
        final ASTNode prevSibling = ObjJTreeUtilKt.getPreviousNonEmptySiblingIgnoringComments(node);
        final IElementType prevSiblingType = prevSibling == null ? null : prevSibling.getElementType();
        final ASTNode parent = node.getTreeParent();
        final IElementType parentType = parent != null ? parent.getElementType() : null;
        final ASTNode superParent = parent == null ? null : parent.getTreeParent();
        final IElementType superParentType = superParent == null ? null : superParent.getElementType();

        final int braceStyle = settings.BRACE_STYLE;

        if (parent == null || parent.getTreeParent() == null/* || parentType == EMBEDDED_CONTENT*/) {
            return Indent.getNoneIndent();
        }
        if (elementType == ObjJ_BLOCK_COMMENT_BODY) {
            return Indent.getContinuationIndent();
        }
        if (elementType == ObjJ_BLOCK_COMMENT_LEADING_ASTERISK || elementType == ObjJ_BLOCK_COMMENT_END) {
            return Indent.getSpaceIndent(1, true);
        }
        if (settings.KEEP_FIRST_COLUMN_COMMENT && (elementType == ObjJ_SINGLE_LINE_COMMENT || elementType == ObjJ_BLOCK_COMMENT)) {
            final ASTNode previousNode = node.getTreePrev();
            if (previousNode != null && previousNode.getElementType() == WHITE_SPACE && previousNode.getText().endsWith("\n")) {
                return Indent.getAbsoluteNoneIndent();
            }
        }

        if (COMMENTS.contains(elementType) && prevSiblingType == ObjJ_OPEN_BRACE && CLASS_DECLARATIONS.contains(parentType)) {
            return Indent.getNormalIndent();
        }

        /*if (parentType == ENUM_DEFINITION && isBetweenBraces(node)) {
            // instead of isBetweenBraces(node) we can parse enum block as a separate ASTNode, or build formatter blocks not tied to AST.
            return Indent.getNormalIndent();
        }*/

        if (parentType == ObjJ_ARRAY_LITERAL || parentType == ObjJ_OBJECT_LITERAL) {
            if (elementType == ObjJ_OPEN_BRACE ||
                    elementType == ObjJ_AT_OPEN_BRACE ||
                    elementType == ObjJ_CLOSE_BRACE ||
                    elementType == ObjJ_OPEN_BRACKET ||
                    elementType == ObjJ_AT_OPENBRACKET ||
                    elementType == ObjJ_CLOSE_BRACKET
            ) {
                return Indent.getNoneIndent();
            }
            // Be careful to preserve typing behavior.
            if (elementType == ObjJ_PROPERTY_ASSIGNMENT || elementType == ObjJ_EXPR || elementType == ObjJ_COMMA) {
                return Indent.getNormalIndent();
            }
            if (COMMENTS.contains(elementType)) {
                return Indent.getNormalIndent();
            }
            return Indent.getNoneIndent();
        }

        if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            switch (braceStyle) {
                case CommonCodeStyleSettings.END_OF_LINE:
                    if (elementType == ObjJ_OPEN_BRACE && FormatterUtil.isPrecededBy(parent, ObjJ_SINGLE_LINE_COMMENT, WHITE_SPACE)) {
                        // Use Nystrom style rather than Allman.
                        return Indent.getContinuationIndent();
                    } // FALL THROUGH
                case CommonCodeStyleSettings.NEXT_LINE:
                case CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED:
                    return Indent.getNoneIndent();
                case CommonCodeStyleSettings.NEXT_LINE_SHIFTED:
                case CommonCodeStyleSettings.NEXT_LINE_SHIFTED2:
                    return Indent.getNormalIndent();
                default:
                    return Indent.getNoneIndent();
            }
        }

        if (parentType == PARENTHESIZED_EXPRESSION) {
            if (elementType == LPAREN || elementType == RPAREN) {
                return Indent.getNoneIndent();
            }
            return Indent.getContinuationIndent();
        }

        if (elementType == CLASS_MEMBERS) {
            return Indent.getNormalIndent();
        }
        if (BLOCKS.contains(parentType)) {
            final PsiElement psi = node.getPsi();
            if (psi.getParent() instanceof PsiFile) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }
        if (elementType == LPAREN && (superParentType == METADATA || parentType == ARGUMENTS)) {
            return Indent.getNormalIndent();
        }
        if (parentType == ARGUMENTS) {
            if (COMMENTS.contains(elementType)) {
                return Indent.getNormalIndent();
            }
            return Indent.getNoneIndent();
        }
        if (parentType == ARGUMENT_LIST) {
            // see https://github.com/ObjJ-lang/ObjJ_style/issues/551
            return parent.getLastChildNode().getElementType() == COMMA ? Indent.getNormalIndent() : Indent.getContinuationIndent();
        }
        if (parentType == FORMAL_PARAMETER_LIST || parentType == PARAMETER_TYPE_LIST) {
            return Indent.getContinuationIndent();
        }
        if (parentType == OPTIONAL_FORMAL_PARAMETERS &&
                elementType != LBRACE && elementType != RBRACE &&
                elementType != LBRACKET && elementType != RBRACKET) {
            return Indent.getNormalIndent();
        }
        if (parentType == FOR_STATEMENT && prevSiblingType == FOR_LOOP_PARTS_IN_BRACES && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }
        if (parentType == SWITCH_STATEMENT && (elementType == SWITCH_CASE || elementType == DEFAULT_CASE)) {
            return Indent.getNormalIndent();
        }
        if ((parentType == SWITCH_CASE || parentType == DEFAULT_CASE) && elementType == STATEMENTS) {
            return Indent.getNormalIndent();
        }
        if (parentType == WHILE_STATEMENT && prevSiblingType == RPAREN && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }
        if (parentType == DO_WHILE_STATEMENT && prevSiblingType == DO && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }
        if ((parentType == RETURN_STATEMENT) &&
                prevSiblingType == RETURN &&
                !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }
        if (parentType == IF_STATEMENT && !BLOCKS.contains(elementType) &&
                (prevSiblingType == RPAREN || (prevSiblingType == ELSE && elementType != IF_STATEMENT))) {
            return Indent.getNormalIndent();
        }
        if (elementType == CASCADE_REFERENCE_EXPRESSION) {
            return Indent.getNormalIndent();
        }
        if (elementType == OPEN_QUOTE && prevSiblingType == CLOSING_QUOTE && parentType == STRING_LITERAL_EXPRESSION) {
            return Indent.getContinuationIndent();
        }
        if (BINARY_EXPRESSIONS.contains(parentType) && prevSibling != null) {
            return Indent.getContinuationIndent();
        }
        if (elementType == COLON || parentType == TERNARY_EXPRESSION && elementType == QUEST) {
            return Indent.getContinuationIndent();
        }
        if (elementType == HIDE_COMBINATOR || elementType == SHOW_COMBINATOR) {
            return Indent.getContinuationIndent();
        }
        if (parentType == FUNCTION_BODY) {
            if (FormatterUtil.isPrecededBy(node, EXPRESSION_BODY_DEF)) {
                return Indent.getContinuationIndent();
            }
        }
        if (elementType == CALL_EXPRESSION) {
            if (FormatterUtil.isPrecededBy(node, EXPRESSION_BODY_DEF)) {
                return Indent.getContinuationIndent();
            }
            if (FormatterUtil.isPrecededBy(node, ASSIGNMENT_OPERATOR)) {
                return Indent.getContinuationIndent();
            }
        }
        if ((elementType == REFERENCE_EXPRESSION || BINARY_EXPRESSIONS.contains(elementType)) &&
                (FormatterUtil.isPrecededBy(node, ASSIGNMENT_OPERATOR) || FormatterUtil.isPrecededBy(node, EQ))) {
            return Indent.getContinuationIndent();
        }
        if (elementType == VAR_DECLARATION_LIST_PART) {
            return Indent.getContinuationIndent();
        }

        if (elementType == SUPER_CALL_OR_FIELD_INITIALIZER) {
            return Indent.getContinuationIndent();
        }
        if (parentType == SUPER_CALL_OR_FIELD_INITIALIZER && elementType != COLON) {
            return Indent.getNormalIndent();
        }

        if (parentType == CLASS_DEFINITION) {
            if (elementType == SUPERCLASS || elementType == INTERFACES || elementType == MIXINS) {
                return Indent.getContinuationIndent();
            }
        }
        if (parentType == MIXIN_APPLICATION && elementType == MIXINS) {
            return Indent.getContinuationIndent();
        }

        if (parentType == LIBRARY_NAME_ELEMENT) {
            return Indent.getContinuationIndent();
        }

        if (elementType == SEMICOLON && FormatterUtil.isPrecededBy(node, SINGLE_LINE_COMMENT, WHITE_SPACE)) {
            return Indent.getContinuationIndent();
        }

        if (elementType == DOT || elementType == QUEST_DOT) {
            return Indent.getContinuationIndent();
        }

        if (parentType == TYPE_LIST && elementType == TYPE) {
            return Indent.getContinuationIndent();
        }

        if (elementType == OPEN_QUOTE && parentType == STRING_LITERAL_EXPRESSION && superParentType == VAR_INIT) {
            if (node.getText().length() < 3) {
                return Indent.getContinuationIndent();
            }
        }

        if (elementType == RAW_SINGLE_QUOTED_STRING && parentType == STRING_LITERAL_EXPRESSION && superParentType == VAR_INIT) {
            return Indent.getContinuationIndent();
        }

        if (parentType == LONG_TEMPLATE_ENTRY && EXPRESSIONS.contains(elementType)) {
            return Indent.getContinuationIndent();
        }
        return Indent.getNoneIndent();
    }

    private static boolean isBetweenBraces(@NotNull final ASTNode node) {
        final IElementType elementType = node.getElementType();
        if (elementType == LBRACE || elementType == RBRACE) return false;

        for (ASTNode sibling = node.getTreePrev(); sibling != null; sibling = sibling.getTreePrev()) {
            if (sibling.getElementType() == LBRACE) return true;
        }

        return false;
    }
}