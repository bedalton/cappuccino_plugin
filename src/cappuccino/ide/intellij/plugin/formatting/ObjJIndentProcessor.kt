package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.*
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.tree.TokenSet

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.TokenType.WHITE_SPACE
import java.util.logging.Logger

class ObjJIndentProcessor(private val settings: CommonCodeStyleSettings) {
    @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
    fun getChildIndent(node: ASTNode,  mode: FormattingMode): Indent? {
        val elementType = node.elementType
        val prevSibling = node.getPreviousNonEmptySiblingIgnoringComments()
        val prevSiblingType = prevSibling?.elementType
        val parent = node.treeParent
        val parentType = parent?.elementType
        val superParent = parent?.treeParent
        val superParentType = superParent?.elementType


        val braceStyle = settings.BRACE_STYLE

        if (parentType == ObjJStubTypes.FILE) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_IMPLEMENTATION_DECLARATION) {
            if ((prevSibling == ObjJ_OPEN_BRACE || prevSibling == ObjJ_SEMI_COLON || prevSibling == ObjJ_INSTANCE_VARIABLE_DECLARATION) && elementType != ObjJ_CLOSE_BRACE) {
                Indent.getNormalIndent()
            }
        }



        if (parentType == ObjJ_INSTANCE_VARIABLE_LIST) {
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE)
                return Indent.getNoneIndent()
            return Indent.getNormalIndent()
        }

        if (parent == null || parent.treeParent == null) {
            return Indent.getNoneIndent()
        }

        if ((parentType == ObjJ_PROTOCOL_DECLARATION || parentType == ObjJ_PROTOCOL_SCOPED_METHOD_BLOCK) && elementType == ObjJ_METHOD_HEADER) {
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
            return when (braceStyle) {
                CommonCodeStyleSettings.END_OF_LINE -> {
                    if (elementType == ObjJ_OPEN_BRACE && FormatterUtil.isPrecededBy(parent, ObjJ_SINGLE_LINE_COMMENT, WHITE_SPACE)) {
                        // Use Nystrom style rather than Allman.
                        Indent.getContinuationIndent()
                    } else Indent.getNoneIndent() // FALL THROUGH
                }
                CommonCodeStyleSettings.NEXT_LINE, CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED -> Indent.getNoneIndent()
                CommonCodeStyleSettings.NEXT_LINE_SHIFTED, CommonCodeStyleSettings.NEXT_LINE_SHIFTED2 -> Indent.getNormalIndent()
                else -> Indent.getNoneIndent()
            }
        }

        if (parentType == ObjJ_METHOD_BLOCK)
            return Indent.getNormalIndent()
        if (parentType == ObjJ_VARIABLE_DECLARATION_LIST) {
            return Indent.getSpaceIndent(4)
        }


        if (elementType == ObjJ_PREPROCESSOR_IF_STATEMENT) {
            if (parentType == ObjJ_METHOD_BLOCK) {
                //return Indent.getNoneIndent()
            }
            return Indent.getNormalIndent(false)
        }

        if (ObjJTokenSets.BLOCKS.contains(parentType)) {
            val psi = node.psi
            return if (psi.parent is PsiFile) {
                Indent.getNoneIndent()
            } else Indent.getNormalIndent()
        }

        if (elementType == ObjJ_METHOD_DECLARATION) {
            return Indent.getNoneIndent()
        }


        if (parentType == ObjJ_ENCLOSED_EXPR) {
            return if (elementType == ObjJ_OPEN_PAREN || elementType == ObjJ_CLOSE_PAREN || elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                Indent.getNoneIndent()
            } else Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_METHOD_CALL && elementType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            return Indent.getContinuationIndent()
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
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_IF_STATEMENT && !ObjJTokenSets.BLOCKS.contains(elementType) &&
                (prevSiblingType == ObjJ_CLOSE_PAREN || (prevSiblingType == ObjJ_ELSE && elementType != ObjJ_IF_STATEMENT))) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_PREPROCESSOR_BODY_STATEMENTS || elementType == ObjJ_PREPROCESSOR_BODY_STATEMENTS) {
            return Indent.getNormalIndent()
        }

        if (elementType  == ObjJ_PREPROCESSOR_IF_CONDITION) {
            return Indent.getNoneIndent()
        }

        /*
        if (elementType == ObjJ_OPEN_QUOTE && prevSiblingType == CLOSING_QUOTE && parentType == STRING_LITERAL_EXPRESSION) {
            return Indent.getContinuationIndent();
        }*/

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

        if (FormatterUtil.isPrecededBy(node, ObjJ_COMMA, WHITE_SPACE) && elementType != ObjJ_VARIABLE_DECLARATION) {
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
    }
}