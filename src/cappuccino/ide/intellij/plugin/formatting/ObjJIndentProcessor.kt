package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.*
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

import java.util.Arrays
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import com.intellij.psi.TokenType.WHITE_SPACE

class ObjJIndentProcessor(private val settings: CommonCodeStyleSettings) {

    fun getChildIndent(node: ASTNode, mode: FormattingMode): Indent {
        val elementType = node.elementType
        val prevSibling = node.getPreviousNonEmptySiblingIgnoringComments()
        val prevSiblingType = prevSibling?.elementType
        val parent = node.treeParent
        val parentType = parent?.elementType
        val superParent = parent?.treeParent
        val superParentType = superParent?.elementType

        val braceStyle = settings.BRACE_STYLE

        if (parent == null || parent.treeParent == null/* || parentType == EMBEDDED_CONTENT*/) {
            return Indent.getNoneIndent()
        }

        if ((parentType == ObjJ_PROTOCOL_DECLARATION || parentType == ObjJ_PROTOCOL_SCOPED_BLOCK) && elementType == ObjJ_METHOD_HEADER) {
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_METHOD_DECLARATION) {
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_BLOCK_COMMENT_BODY) {
            return Indent.getContinuationIndent()
        }
        if (elementType == ObjJ_BLOCK_COMMENT_LEADING_ASTERISK || elementType == ObjJ_BLOCK_COMMENT_END) {
            return Indent.getSpaceIndent(1, true)
        }
        if (settings.KEEP_FIRST_COLUMN_COMMENT && (elementType == ObjJ_SINGLE_LINE_COMMENT || elementType == ObjJ_BLOCK_COMMENT)) {
            val previousNode = node.treePrev
            if (previousNode != null && previousNode.elementType == WHITE_SPACE && previousNode.text.endsWith("\n")) {
                return Indent.getAbsoluteNoneIndent()
            }
        }

        if (ObjJTokenSets.COMMENTS.contains(elementType) && prevSiblingType == ObjJ_OPEN_BRACE && ObjJTokenSets.CLASS_DECLARATIONS.contains(parentType)) {
            return Indent.getNormalIndent()
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
                    elementType == ObjJ_CLOSE_BRACKET) {
                return Indent.getNoneIndent()
            }
            // Be careful to preserve typing behavior.
            if (elementType == ObjJ_PROPERTY_ASSIGNMENT || elementType == ObjJ_EXPR || elementType == ObjJ_COMMA) {
                return Indent.getNormalIndent()
            }
            return if (ObjJTokenSets.COMMENTS.contains(elementType)) {
                Indent.getNormalIndent()
            } else Indent.getNoneIndent()
        }

        if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            when (braceStyle) {
                CommonCodeStyleSettings.END_OF_LINE -> {
                    return if (elementType == ObjJ_OPEN_BRACE && FormatterUtil.isPrecededBy(parent, ObjJ_SINGLE_LINE_COMMENT, WHITE_SPACE)) {
                        // Use Nystrom style rather than Allman.
                        Indent.getContinuationIndent()
                    } else Indent.getNoneIndent() // FALL THROUGH
                }
                CommonCodeStyleSettings.NEXT_LINE, CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED -> return Indent.getNoneIndent()
                CommonCodeStyleSettings.NEXT_LINE_SHIFTED, CommonCodeStyleSettings.NEXT_LINE_SHIFTED2 -> return Indent.getNormalIndent()
                else -> return Indent.getNoneIndent()
            }
        }

        if (parentType == ObjJ_ENCLOSED_EXPR) {
            return if (elementType == ObjJ_OPEN_PAREN || elementType == ObjJ_CLOSE_PAREN || elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                Indent.getNoneIndent()
            } else Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_METHOD_CALL && elementType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            return Indent.getContinuationIndent()
        }


        if (ObjJTokenSets.BLOCKS.contains(parentType)) {
            val psi = node.psi
            return if (psi.parent is PsiFile) {
                Indent.getNoneIndent()
            } else Indent.getNormalIndent()
        }

        if (parentType == ObjJ_FOR_STATEMENT && prevSiblingType == ObjJ_FOR_LOOP_PARTS_IN_BRACES && !ObjJTokenSets.BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_SWITCH_STATEMENT && (elementType == ObjJ_CASE_CLAUSE || elementType == ObjJ_DEFAULT_CLAUSE)) {
            return Indent.getNormalIndent()
        }

        if ((parentType == ObjJ_CASE_CLAUSE || parentType == ObjJ_DEFAULT_CLAUSE) && elementType == ObjJ_BRACKET_LESS_BLOCK) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_WHILE_STATEMENT && prevSiblingType == ObjJ_CLOSE_PAREN && !ObjJTokenSets.BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_DO_WHILE_STATEMENT && prevSiblingType == ObjJ_DO && !ObjJTokenSets.BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_RETURN_STATEMENT &&
                prevSiblingType == ObjJ_RETURN &&
                !ObjJTokenSets.BLOCKS.contains(elementType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_IF_STATEMENT && !ObjJTokenSets.BLOCKS.contains(elementType) &&
                (prevSiblingType == ObjJ_CLOSE_PAREN || (prevSiblingType == ObjJ_ELSE && elementType !== ObjJ_IF_STATEMENT))) {
            return Indent.getNormalIndent()
        }
        /*
        if (elementType == ObjJ_OPEN_QUOTE && prevSiblingType == CLOSING_QUOTE && parentType == STRING_LITERAL_EXPRESSION) {
            return Indent.getContinuationIndent();
        }*/
        if (BINARY_EXPRESSIONS.contains(parentType) && prevSibling != null) {
            return Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_METHOD_CALL) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_CALL_TARGET, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (parentType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_COLON, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (elementType == ObjJ_FUNCTION_CALL) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (parentType == ObjJ_INHERITED_PROTOCOL_LIST) {
            return if (elementType == ObjJ_LESS_THAN || elementType == ObjJ_GREATER_THAN) {
                Indent.getNoneIndent()
            } else Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_ASSIGNMENT_EXPR_PRIME) {
            return Indent.getContinuationIndent()
        }

        if (ObjJTokenSets.CLASS_DECLARATIONS.contains(parentType)) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_IMPORT_FILE || parentType == ObjJ_IMPORT_FRAMEWORK) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_AT_IMPORT, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (elementType == ObjJ_SEMI_COLON && FormatterUtil.isPrecededBy(node, ObjJ_SINGLE_LINE_COMMENT, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        if (elementType == ObjJ_DOT) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }
        if (FormatterUtil.isPrecededBy(node, ObjJ_MATH_OP, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_COMMA, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACE, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACKET, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_PAREN, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }

        return if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            Indent.getNoneIndent()
        } else Indent.getNoneIndent()

    }

    companion object {


        val EXPRESSIONS = TokenSet.create(ObjJ_EXPR)

        private val HAS_NO_INDENT_PAREN = TokenSet.create(
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
        )

        private val BINARY_EXPRESSIONS = TokenSet.create(

        )

        private val HAS_NO_INDENT_BRACE = TokenSet.create(
                ObjJ_METHOD_BLOCK,
                ObjJ_FUNCTION_LITERAL,
                ObjJ_BLOCK_ELEMENT,
                ObjJ_SWITCH_STATEMENT
        )

        private fun isBetweenBraces(node: ASTNode): Boolean {
            val elementType = node.elementType
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) return false

            var sibling: ASTNode? = node.treePrev
            while (sibling != null) {
                if (sibling.elementType == ObjJ_OPEN_BRACE) return true
                sibling = sibling.treePrev
            }

            return false
        }
    }
}