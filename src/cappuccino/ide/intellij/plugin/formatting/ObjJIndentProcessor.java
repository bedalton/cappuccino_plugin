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

    private static final TokenSet BLOCKS = TokenSet.create(ObjJ_BLOCK_ELEMENT, ObjJ_BRACKET_LESS_BLOCK, ObjJ_METHOD_BLOCK, ObjJ_PROTOCOL_SCOPED_BLOCK,
            ObjJ_STATEMENT_OR_BLOCK);

    private static final TokenSet COMMENTS = ObjJParserDefinition.Companion.getCOMMENTS();

    private static final TokenSet CLASS_DECLARATIONS = TokenSet.create(ObjJ_IMPLEMENTATION_DECLARATION, ObjJ_PROTOCOL_DECLARATION);

    private static final TokenSet HAS_NO_INDENT_PAREN = TokenSet.create(
            ObjJ_FUNCTION_DECLARATION,
            ObjJ_ENCLOSED_EXPR,
            ObjJ_PREPROCESSOR_DEFINE_FUNCTION,
            ObjJ_FUNCTION_LITERAL,
            ObjJ_REF_EXPRESSION,
            ObjJ_DEREF_EXPRESSION,
            ObjJ_NEW_EXPRESSION,
            ObjJ_FUNCTION_CALL,
            ObjJ_TYPE_OF,
            ObjJ_SELECTOR_LITERAL,
            ObjJ_ACCESSOR,
            ObjJ_CONDITION_EXPRESSION,
            ObjJ_FOR,
            ObjJ_SWITCH_STATEMENT
    );

    private static final TokenSet BINARY_EXPRESSIONS = TokenSet.create(

    );

    private static final TokenSet HAS_NO_INDENT_BRACE  = TokenSet.create(
            ObjJ_METHOD_BLOCK,
            ObjJ_FUNCTION_LITERAL,
            ObjJ_BLOCK_ELEMENT,
            ObjJ_SWITCH_STATEMENT
    );
    private final CommonCodeStyleSettings settings;

    public ObjJIndentProcessor(CommonCodeStyleSettings settings) {
        this.settings = settings;
    }

    @SuppressWarnings("Duplicates")
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

        if (parentType == ObjJ_ENCLOSED_EXPR) {
            if (elementType == ObjJ_OPEN_PAREN|| elementType == ObjJ_CLOSE_PAREN || elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                return Indent.getNoneIndent();
            }
            return Indent.getContinuationIndent();
        }


        if (BLOCKS.contains(parentType)) {
            final PsiElement psi = node.getPsi();
            if (psi.getParent() instanceof PsiFile) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_ITERATION_STATEMENT && (prevSiblingType == ObjJ_CLOSE_PAREN || prevSibling == ObjJ_DO || prevSibling == ObjJ_WHILE) && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_SWITCH_STATEMENT && (elementType == ObjJ_CASE_CLAUSE || elementType == ObjJ_DEFAULT_CLAUSE)) {
            return Indent.getNormalIndent();
        }

        if ((parentType == ObjJ_CASE_CLAUSE || parentType == ObjJ_DEFAULT_CLAUSE) && elementType == ObjJ_BRACKET_LESS_BLOCK) {
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_WHILE_STATEMENT && prevSiblingType == ObjJ_CLOSE_PAREN && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_DO_WHILE_STATEMENT && prevSiblingType == ObjJ_DO && !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }

        if ((parentType == ObjJ_RETURN_STATEMENT) &&
                prevSiblingType == ObjJ_RETURN &&
                !BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_IF_STATEMENT && !BLOCKS.contains(elementType) &&
                (prevSiblingType == ObjJ_CLOSE_PAREN || (prevSiblingType == ObjJ_ELSE && elementType != ObjJ_IF_STATEMENT))) {
            return Indent.getNormalIndent();
        }
        /*
        if (elementType == ObjJ_OPEN_QUOTE && prevSiblingType == CLOSING_QUOTE && parentType == STRING_LITERAL_EXPRESSION) {
            return Indent.getContinuationIndent();
        }*/
        if (BINARY_EXPRESSIONS.contains(parentType) && prevSibling != null) {
            return Indent.getContinuationIndent();
        }

        if (parentType == ObjJ_METHOD_CALL) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_CALL_TARGET)) {
                return Indent.getContinuationIndent();
            }
        }

        if (parentType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_COLON)) {
                return Indent.getContinuationIndent();
            }
        }

        if (elementType == ObjJ_FUNCTION_CALL) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR)) {
                return Indent.getContinuationIndent();
            }
        }

        if (parentType == ObjJ_INHERITED_PROTOCOL_LIST) {
            if (elementType == ObjJ_LESS_THAN || elementType == ObjJ_GREATER_THAN) {
                return Indent.getNoneIndent();
            }
            return Indent.getContinuationIndent();
        }

        if (parentType == ObjJ_ASSIGNMENT_EXPR_PRIME) {
            return Indent.getContinuationIndent();
        }

        if (CLASS_DECLARATIONS.contains(parentType)) {
            return Indent.getNormalIndent();
        }

        if (parentType == ObjJ_IMPORT_FILE || parentType == ObjJ_IMPORT_FRAMEWORK) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_AT_IMPORT)) {
                return Indent.getContinuationIndent();
            }
        }

        if (elementType == ObjJ_SEMI_COLON && FormatterUtil.isPrecededBy(node, ObjJ_SINGLE_LINE_COMMENT, WHITE_SPACE)) {
            return Indent.getContinuationIndent();
        }

        if (elementType == ObjJ_DOT) {
            return Indent.getContinuationIndent();
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR)) {
            return Indent.getContinuationIndent();
        }
        if (FormatterUtil.isPrecededBy(node, ObjJ_MATH_OP)) {
            return Indent.getContinuationIndent();
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_COMMA)) {
            return Indent.getContinuationIndent();
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACE)) {
            return Indent.getContinuationIndent();
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACKET)) {
            return Indent.getContinuationIndent();
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_PAREN)) {
            return Indent.getContinuationIndent();
        }

        if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            return Indent.getNoneIndent();
        }

        return Indent.getNoneIndent();
    }

    private static boolean isBetweenBraces(@NotNull final ASTNode node) {
        final IElementType elementType = node.getElementType();
        if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) return false;

        for (ASTNode sibling = node.getTreePrev(); sibling != null; sibling = sibling.getTreePrev()) {
            if (sibling.getElementType() == ObjJ_OPEN_BRACE) return true;
        }

        return false;
    }
}