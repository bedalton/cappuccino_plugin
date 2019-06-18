package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.*
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.tree.TokenSet

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.TokenType.WHITE_SPACE
import java.util.logging.Logger

class ObjJIndentProcessor(private val settings: CommonCodeStyleSettings, private val objjSettings:ObjJCodeStyleSettings) {
    fun getChildIndent(node: ASTNode): Indent? {
        val elementType = node.elementType
        val prevSibling = node.getPreviousNonEmptySiblingIgnoringComments()
        val prevSiblingType = prevSibling?.elementType
        val nextSibling = node.getNextNonEmptyNode(true)
        val nextSiblingType = nextSibling?.elementType
        val parent = node.treeParent
        val parentType = parent?.elementType
        val superParent = parent?.treeParent
        val superParentType = superParent?.elementType

        if (parentType == ObjJStubTypes.FILE) {
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            if (node.treeParent == ObjJ_VARIABLE_DECLARATION ||
                    node.treeParent?.treeParent == ObjJ_VARIABLE_DECLARATION ||
                    node.treeParent?.treeParent?.treeParent == ObjJ_VARIABLE_DECLARATION) {
                return Indent.getNormalIndent()
            }
            return Indent.getNoneIndent()
        }

        if (prevSibling == ObjJ_IF_STATEMENT || prevSibling == ObjJ_ELSE_IF_STATEMENT || prevSibling == ObjJ_STATEMENT_OR_BLOCK)
            Indent.getNoneIndent()

        if (prevSibling == ObjJ_OPEN_BRACE || nextSiblingType == ObjJ_CLOSE_BRACE) {
            Indent.getNormalIndent()
        }

        if (parentType == ObjJ_INSTANCE_VARIABLE_LIST) {
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE)
                return Indent.getNoneIndent()
            return Indent.getNormalIndent()
        }


        if (parentType == ObjJ_FOR_STATEMENT || parentType == ObjJ_WHILE_STATEMENT || parentType == ObjJ_DO_WHILE_STATEMENT) {
            if (elementType == ObjJ_STATEMENT_OR_BLOCK)
                return Indent.getNoneIndent()
            if (elementType == ObjJ_STATEMENT_OR_BLOCK && node.firstChildNode?.elementType !in ObjJTokenSets.INDENT_CHILDREN) {
                return Indent.getNormalIndent()
            }
            return Indent.getNoneIndent()
        } else if (parentType == ObjJ_STATEMENT_OR_BLOCK) {
            return Indent.getNoneIndent()
        }

        if (elementType in ObjJTokenSets.INDENT_CHILDREN)
            return Indent.getNoneIndent()

        if (elementType == ObjJ_STATEMENT_OR_BLOCK) {
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_RIGHT_EXPR)
            return Indent.getContinuationWithoutFirstIndent()

        if (parentType in ObjJTokenSets.INDENT_CHILDREN) {
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE)
                return Indent.getNoneIndent()
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_IMPLEMENTATION_DECLARATION) {
            if ((prevSibling == ObjJ_OPEN_BRACE || prevSibling == ObjJ_SEMI_COLON || prevSibling == ObjJ_INSTANCE_VARIABLE_DECLARATION) && elementType != ObjJ_CLOSE_BRACE) {
                return Indent.getNormalIndent()
            }
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

        if (parentType == ObjJ_LOGIC_EXPR_PRIME) {
            return Indent.getContinuationIndent()
        }

        if (ObjJTokenSets.COMMENTS.contains(elementType) && prevSiblingType == ObjJ_OPEN_BRACE && ObjJTokenSets.CLASS_DECLARATIONS.contains(parentType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_ARRAY_LITERAL || parentType == ObjJ_OBJECT_LITERAL) {
            if (elementType == ObjJ_OPEN_BRACE ||
                    elementType == ObjJ_AT_OPEN_BRACE ||
                    elementType == ObjJ_CLOSE_BRACE) {
                return Indent.getNoneIndent()
            }

            if (elementType == ObjJ_PROPERTY_ASSIGNMENT) {
                if (objjSettings.ALIGN_PROPERTIES) {
                    val propertyAssignment =
                            node.psi?.getSelfOrParentOfType(ObjJPropertyAssignment::class.java)
                                    ?: return Indent.getNormalIndent()
                    val spacing = propertyAssignment.getObjectLiteralPropertySpacing()
                    if (spacing != null && spacing > 0) {
                        return Indent.getSpaceIndent(spacing)
                    }
                }
                return Indent.getNormalIndent()
            }

            // Be careful to preserve typing behavior.
            if (elementType == ObjJ_PROPERTY_ASSIGNMENT || elementType == ObjJ_COMMA) {
                return Indent.getNormalIndent()
            }
            if (elementType == ObjJ_EXPR)
                return Indent.getContinuationIndent()
            return if (ObjJTokenSets.COMMENTS.contains(elementType)) {
                Indent.getNormalIndent()
            } else Indent.getNoneIndent()
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

        if (parentType in ObjJTokenSets.INDENT_CHILDREN) {
            val psi = node.psi
            return if (psi.parent is PsiFile) {
                Indent.getNoneIndent()
            } else Indent.getNormalIndent()
        }

        if (elementType == ObjJ_METHOD_DECLARATION) {
            return Indent.getNoneIndent()
        }


        if (elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            if (objjSettings.ALIGN_SELECTORS_IN_METHOD_DECLARATION) {
                val selectorSpacing = node.psi?.getSelfOrParentOfType(ObjJMethodDeclarationSelector::class.java)?.getSelectorAlignmentSpacing(objjSettings)
                //LOGGER.info("Get Method Dec Selector Indent Spacing: $selectorSpacing")
                if (selectorSpacing != null && selectorSpacing > 0) {
                    return Indent.getSpaceIndent(selectorSpacing)
                } else {
                    //LOGGER.info("Failed to get selector spacing for: ${node.text}")
                }
            }
            return Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_PAREN_ENCLOSED_EXPR || parentType == ObjJ_ARRAY_INDEX_SELECTOR) {
            return if (elementType == ObjJ_OPEN_PAREN || elementType == ObjJ_CLOSE_PAREN || elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                Indent.getNoneIndent()
            } else Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_FOR_STATEMENT && (prevSiblingType == ObjJ_FOR_LOOP_PARTS_IN_BRACES || prevSiblingType == ObjJ_FOR_LOOP_HEADER)) {
            if (elementType !in ObjJTokenSets.INDENT_CHILDREN)
                return Indent.getNormalIndent()
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_FOR_LOOP_PARTS_IN_BRACES || elementType == ObjJ_FOR_LOOP_HEADER) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_IN_EXPR) {
            return when {
                elementType == ObjJ_IN -> Indent.getContinuationIndent()
                elementType == ObjJ_OPEN_PAREN -> Indent.getNoneIndent()
                prevSiblingType == ObjJ_OPEN_PAREN -> Indent.getNormalIndent()
                elementType == ObjJ_EXPR -> Indent.getContinuationIndent()
                elementType == ObjJ_OPEN_PAREN || elementType == ObjJ_CLOSE_PAREN -> Indent.getNoneIndent()
                else -> Indent.getNoneIndent()
            }
        }

        if (parentType == ObjJ_SWITCH_STATEMENT && (elementType == ObjJ_CASE_CLAUSE || elementType == ObjJ_DEFAULT_CLAUSE)) {
            return Indent.getNormalIndent()
        }

        if ((parentType == ObjJ_CASE_CLAUSE || parentType == ObjJ_DEFAULT_CLAUSE) && elementType == ObjJ_BRACKET_LESS_BLOCK) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_WHILE_STATEMENT && prevSiblingType == ObjJ_CLOSE_PAREN && !ObjJTokenSets.INDENT_CHILDREN.contains(elementType)) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_DO_WHILE_STATEMENT && prevSiblingType == ObjJ_DO && !ObjJTokenSets.INDENT_CHILDREN.contains(elementType)) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_RETURN_STATEMENT &&
                prevSiblingType == ObjJ_RETURN &&
                !ObjJTokenSets.INDENT_CHILDREN.contains(elementType)) {
            return Indent.getNoneIndent()
        }

        if (parentType == ObjJ_IF_STATEMENT && !ObjJTokenSets.INDENT_CHILDREN.contains(elementType) &&
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


        if (elementType == ObjJ_FUNCTION_CALL) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (parentType == ObjJ_INHERITED_PROTOCOL_LIST) {
            return if (elementType == ObjJ_LESS_THAN || elementType == ObjJ_GREATER_THAN)
                Indent.getNoneIndent()
            else
                Indent.getNormalIndent()
        }



        if (elementType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            if (objjSettings.ALIGN_SELECTORS_IN_METHOD_CALL) {
                val selectorSpacing = node.psi?.getSelfOrParentOfType(ObjJQualifiedMethodCallSelector::class.java)?.getSelectorAlignmentSpacing(objjSettings.ALIGN_FIRST_SELECTOR_IN_METHOD_CALL)
                if (selectorSpacing != null && selectorSpacing >= 4) {
                    return Indent.getSpaceIndent(selectorSpacing)
                }
            }
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            if (FormatterUtil.isPrecededBy(node, ObjJ_COLON, WHITE_SPACE)) {
                return Indent.getContinuationIndent()
            }
        }

        if (parentType == ObjJ_METHOD_CALL) {
            if (elementType == ObjJ_CALL_TARGET)
                return Indent.getNormalIndent()
            if (FormatterUtil.isPrecededBy(node, ObjJ_CALL_TARGET, WHITE_SPACE)) {
                return Indent.getNormalIndent()
            }
            if (elementType == ObjJ_COLON)
                return Indent.getContinuationIndent()
            if (elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                return Indent.getNoneIndent()
            }
        }

        if (elementType == ObjJ_METHOD_HEADER_SELECTOR_FORMAL_VARIABLE_TYPE || parentType == ObjJ_METHOD_HEADER_SELECTOR_FORMAL_VARIABLE_TYPE) {
            return Indent.getNormalIndent()
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
            return Indent.getNormalIndent()
        }

        if (elementType == ObjJ_DOT) {
            return Indent.getContinuationIndent()
        }

        if (elementType == ObjJ_ACCESSOR) {
            return Indent.getNormalIndent()
        }

        if (elementType == ObjJ_AT_ACCESSORS) {
            return Indent.getNormalIndent()
        }

        if (parentType == ObjJ_ACCESSOR) {
            return Indent.getNormalIndent()
        }

        if (superParentType == ObjJ_STATEMENT_OR_BLOCK) {
            if (elementType != ObjJ_BLOCK_ELEMENT) {
                return Indent.getNormalIndent()
            }
            return Indent.getNoneIndent()
        }

        if (parent == ObjJ_STATEMENT_OR_BLOCK || superParent == ObjJ_STATEMENT_OR_BLOCK) {
            return if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE || elementType in ObjJTokenSets.INDENT_CHILDREN)
                Indent.getNoneIndent()
            else
                Indent.getNormalIndent()
        }

        if (parentType == ObjJ_COMPARISON_EXPR_PRIME) {
            return Indent.getContinuationIndent()
        }

        if (elementType == ObjJ_RIGHT_EXPR) {
            return if (node.psi.hasParentOfType(ObjJRightExpr::class.java)) {
                Indent.getNoneIndent()
            } else
                Indent.getNormalIndent()
        }

        if (parentType == ObjJ_RIGHT_EXPR) {
            return Indent.getNoneIndent()
        }

        if (elementType == ObjJ_ACCESSOR_PROPERTY) {
            return Indent.getContinuationIndent()
        }

        if (elementType == ObjJ_ACCESSOR_PROPERTY_TYPE) {
            return Indent.getContinuationIndent()
        }

        if (elementType == ObjJ_EQUALS) {
            return Indent.getContinuationIndent()
        }

        if (parentType == ObjJ_TERNARY_EXPR_PRIME) {
            if (prevSiblingType == ObjJ_COLON || elementType == ObjJ_COLON) {
                return Indent.getContinuationIndent()
            }
            return Indent.getNormalIndent()
        }


        if (FormatterUtil.isPrecededBy(node, ObjJ_ASSIGNMENT_OPERATOR, WHITE_SPACE)) {
            return Indent.getContinuationIndent()
        }
        if (FormatterUtil.isPrecededBy(node, ObjJ_MATH_OP, WHITE_SPACE)) {
            return Indent.getNormalIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_COMMA, WHITE_SPACE) && elementType != ObjJ_VARIABLE_DECLARATION) {
            return Indent.getContinuationIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACE, WHITE_SPACE)) {
            return Indent.getNormalIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_BRACKET, WHITE_SPACE)) {
            return Indent.getNormalIndent()
        }

        if (FormatterUtil.isPrecededBy(node, ObjJ_OPEN_PAREN, WHITE_SPACE)) {
            return Indent.getNormalIndent()
        }

        //Logger.getLogger("#ObjJIndentProcessor").info("Indent-> type: $elementType(${node.text}); parent: $parentType; prev: $prevSiblingType(${prevSibling?.text}); next: $nextSiblingType(${nextSibling?.text}); ")
        return if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
            Indent.getNoneIndent()
        } else Indent.getNoneIndent()

    }

    companion object {
        val EXPRESSIONS = TokenSet.create(ObjJ_EXPR)
        val LOGGER:Logger by lazy {
            Logger.getLogger("#"+ObjJIndentProcessor::class.java.canonicalName)
        }
    }
}