@file:Suppress("unused", "UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasBraces
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.lang.parser.GeneratedParserUtilBase.DUMMY_BLOCK
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import java.util.logging.Logger
import kotlin.math.max


private val LOGGER:Logger by lazy {
    Logger.getLogger("#ObjJSpacingProcessor")
}

class ObjJSpacingProcessor(private val myNode: ASTNode, private val mySettings: CommonCodeStyleSettings, private val objJSettings: ObjJCodeStyleSettings) {

    fun getSpacing(child1: Block?, child2: Block?): Spacing? {
        if (child1 !is AbstractBlock || child2 !is AbstractBlock) {
            return null
        }

        val elementType = myNode.elementType
        val parentType = if (myNode.treeParent == null) null else myNode.treeParent.elementType
        val node1 = child1.node
        val type1 = node1.elementType
        val node2 = child2.node
        val type2 = node2.elementType


        if (type1 == ObjJ_AT_IMPLEMENTATION) {
            return Spacing.createSpacing(1, Int.MAX_VALUE, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_AT_IMPORT || type1 == ObjJ_PP_INCLUDE) {
            return Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_COMMA) {
            return Spacing.createSpacing(0,0,0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_SEMI_COLON) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_SINGLE_LINE_COMMENT) {
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.IMPORT_BLOCKS) {
            return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (myNode.psi?.isOrHasParentOfType(ObjJSelectorLiteral::class.java).orFalse()) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if ((type1 == ObjJ_BLOCK_ELEMENT || type2 == ObjJ_BLOCK_ELEMENT) && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if ((type1 == ObjJ_CATCH_PRODUCTION || type2 == ObjJ_CATCH_PRODUCTION) && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if ((type1 == ObjJ_FINALLY_PRODUCTION || type2 == ObjJ_FINALLY_PRODUCTION) && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if ((type1 == ObjJ_BLOCK_ELEMENT || type2 == ObjJ_BLOCK_ELEMENT) && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_OBJECT_LITERAL) {
            if (type1 == ObjJ_OPEN_BRACE || type1 == ObjJ_AT_OPEN_BRACE || type2 == ObjJ_CLOSE_BRACE) {
                return Spacing.createSpacing(0, Int.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type1 == ObjJ_COMMA) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_AT_END) {
            if (node2.isDirectlyPrecededByNewline())
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            if (node2.getPreviousNonEmptyNode(false)?.isDirectlyPrecededByNewline().orElse(true)) {
                return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(1, 1, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_METHOD_HEADER) {
            if (type1 == ObjJ_METHOD_SCOPE_MARKER) {
                val spacing = if (objJSettings.SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE) 1 else 0
                return Spacing.createSpacing(spacing,  spacing, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }

            if (type1 == ObjJ_CLOSE_PAREN) {
                return if (objJSettings.SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }
        if (objJSettings.ALIGN_SELECTORS_IN_METHOD_DECLARATION && type2 in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            val selectorSpacing = node2.psi?.getSelfOrParentOfType(ObjJMethodDeclarationSelector::class.java)?.getSelectorAlignmentSpacing(objJSettings)
            if (selectorSpacing != null) {
                //LOGGER.info("Creating method dec selector spacing: $selectorSpacing for selector ${node2.text}")
                return Spacing.createSpacing(selectorSpacing, selectorSpacing, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            } else {
                LOGGER.warning("Failed to get selector spacing.")
            }
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (objJSettings.ALIGN_SELECTORS_IN_METHOD_CALL && type2 == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {

            val methodCallSelector = node2.psi?.getSelfOrParentOfType(ObjJQualifiedMethodCallSelector::class.java)
            val spacing = methodCallSelector?.getSelectorAlignmentSpacing(objJSettings.ALIGN_FIRST_SELECTOR_IN_METHOD_CALL)
            if (spacing != null) {
                return Spacing.createSpacing(spacing, spacing, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 in ObjJTokenSets.HAS_BRACES && objJSettings.BRACE_ON_NEW_LINE) {
            if (isEmptyBlock(node2.psi)) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            if (type2 == ObjJ_METHOD_HEADER_SELECTOR_FORMAL_VARIABLE_TYPE) {
                val spacing = if (objJSettings.SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE) 1 else 0
                return Spacing.createSpacing(spacing, spacing, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type2 == ObjJ_VARIABLE_NAME) {
                val spacing = if (objJSettings.SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME) 1 else 0
                return Spacing.createSpacing(spacing, spacing, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            return Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_DO_WHILE_STATEMENT) {
            if (type1 == ObjJ_DO && type2 == ObjJ_STATEMENT_OR_BLOCK) {
                return if (objJSettings.SPACE_BEFORE_LBRACE && !objJSettings.BRACE_ON_NEW_LINE) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else if (objJSettings.BRACE_ON_NEW_LINE) {
                    Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (elementType == ObjJ_IN && parentType == ObjJ_IN_EXPR) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (elementType == ObjJ_TERNARY_EXPR_PRIME) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_RIGHT_EXPR) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_MATH_OP) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_ASSIGNMENT_EXPR_PRIME || type2 == ObjJ_ASSIGNMENT_EXPR_PRIME) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_VARIABLE_ASSIGNMENT_LOGICAL) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.ASSIGNMENT_OPERATORS || type1 in ObjJTokenSets.ASSIGNMENT_OPERATORS || type2 in ObjJTokenSets.ASSIGNMENT_OPERATORS) {
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_STATEMENT_OR_BLOCK && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_FINALLY && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            if (type1 == ObjJ_COLON) {
                val spacing = if (objJSettings.SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL) {
                    1
                } else {
                    0
                }
                return Spacing.createSpacing(spacing, spacing, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type2 == ObjJ_COLON) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (type1 in ObjJTokenSets.IMPORT_BLOCKS) {
            return Spacing.createSpacing(0, 0, 2, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_PROPERTY_ASSIGNMENT) {
            if (type1 != ObjJ_PROPERTY_ASSIGNMENT) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (elementType == ObjJ_FOR_STATEMENT && type1 == ObjJ_FOR && type2 == ObjJ_FOR_LOOP_HEADER) {
            return if (mySettings.SPACE_BEFORE_FOR_PARENTHESES || objJSettings.SPACE_BEFORE_PAREN_STATEMENT) {
                Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            } else {
                Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (elementType == ObjJ_WHILE_STATEMENT || elementType == ObjJ_DO_WHILE_STATEMENT) {
            if (type1 == ObjJ_WHILE && type2 == ObjJ_CONDITION_EXPRESSION) {
                return if (mySettings.SPACE_BEFORE_WHILE_PARENTHESES || objJSettings.SPACE_BEFORE_PAREN_STATEMENT) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (elementType == ObjJ_IF_STATEMENT) {
            if (type1 == ObjJ_IF && type2 == ObjJ_CONDITION_EXPRESSION) {
                return if (mySettings.SPACE_BEFORE_IF_PARENTHESES || objJSettings.SPACE_BEFORE_PAREN_STATEMENT) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (elementType == ObjJ_VARIABLE_DECLARATION_LIST) {
            if (type2 == ObjJ_COMMA) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type1 == ObjJ_COMMA && type2 == ObjJ_VARIABLE_DECLARATION) {
                return when {
                    objJSettings.DECLARATIONS_ON_NEW_LINE -> Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    mySettings.SPACE_AFTER_COMMA -> Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    else -> Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (elementType == ObjJ_DO_WHILE_STATEMENT) {
            if (type1 == ObjJ_STATEMENT_OR_BLOCK && type2 == ObjJ_WHILE) {
                return if (mySettings.WHILE_ON_NEW_LINE) {
                    Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (type1 == ObjJ_BODY_VARIABLE_ASSIGNMENT && objJSettings.GROUP_STATEMENTS) {
            if (type2 != ObjJ_BODY_VARIABLE_ASSIGNMENT) {
                return Spacing.createSpacing(0, 0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (type1 in ObjJTokenSets.EXPRESSIONS && type2 == ObjJ_BODY_VARIABLE_ASSIGNMENT && objJSettings.GROUP_STATEMENTS) {
            return Spacing.createSpacing(0, 0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_STATEMENT_OR_BLOCK && type1 in ObjJTokenSets.EXPRESSIONS && type2 !in ObjJTokenSets.EXPRESSIONS) {
            val lineFeeds = if (objJSettings.NEW_LINE_AFTER_BLOCKS && !isEmptyBlock(node2.psi)) 2 else 1
            return Spacing.createSpacing(0, 0, lineFeeds, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 in ObjJTokenSets.NEW_LINE_AFTER && objJSettings.NEW_LINE_AFTER_BLOCKS) {

            return Spacing.createSpacing(0, 0, 2, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (parentType in ObjJTokenSets.INDENT_CHILDREN) {
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0, Int.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if ((type2 == ObjJ_OPEN_BRACE || type2 == ObjJ_CLOSE_BRACE) && objJSettings.BRACE_ON_NEW_LINE) {
            val minSpace = if (type2 == ObjJ_OPEN_BRACE && objJSettings.SPACE_BEFORE_LBRACE && node1.textContains(')')) 1 else 0
            return Spacing.createSpacing(minSpace, Int.MAX_VALUE, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == DUMMY_BLOCK || type1 == DUMMY_BLOCK || type2 == DUMMY_BLOCK) {
            return Spacing.createSpacing(0, Int.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_FUNCTION_NAME && type2 == ObjJ_ARGUMENTS) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_ARGUMENTS) {
            if (type1 == ObjJ_COMMA) {
                return Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (elementType == ObjJ_SWITCH_STATEMENT) {
            if (type1 == ObjJ_SWITCH && type2 == ObjJ_OPEN_PAREN) {
                return if (mySettings.SPACE_BEFORE_SWITCH_PARENTHESES || objJSettings.SPACE_BEFORE_PAREN_STATEMENT) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
            if (type2 == ObjJ_EXPR || type2 == ObjJ_CLOSE_PAREN) {
                return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }

            if (type1 == ObjJ_OPEN_BRACE && type2 == ObjJ_CASE_CLAUSE) {
                return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }

            if (type1 == ObjJ_CASE_CLAUSE && (type2 == ObjJ_CASE_CLAUSE || type2 == ObjJ_DEFAULT_CLAUSE)) {
                return if (node1.lastChildNode?.elementType == ObjJ_BRACKET_LESS_BLOCK && mySettings.CASE_STATEMENT_ON_NEW_LINE) {
                    Spacing.createSpacing(0, 0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
            if (type1 == ObjJ_CLOSE_PAREN && type2 == ObjJ_OPEN_BRACE) {
                return when {
                    objJSettings.BRACE_ON_NEW_LINE -> Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    objJSettings.SPACE_BEFORE_LBRACE -> Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    else -> Spacing.createSpacing(0, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
            if (type2 == ObjJ_CLOSE_BRACE) {
                return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (type1 == ObjJ_RETURN && type2 != ObjJ_SEMI_COLON) {
            return Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_CASE_CLAUSE || elementType == ObjJ_DEFAULT_CLAUSE) {
            if (type1 == ObjJ_COLON && type2 == ObjJ_BRACKET_LESS_BLOCK) {
                return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (elementType == ObjJ_IMPLEMENTATION_DECLARATION || elementType == ObjJ_PROTOCOL_DECLARATION) {
            if (type1 == ObjJ_CLASS_NAME && type2 == ObjJ_INSTANCE_VARIABLE_LIST) {
                if (isEmptyBlock(node2.psi)) {
                    Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
                return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (type2 == ObjJ_STATEMENT_OR_BLOCK) {
            return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        //LOGGER.info("Getting Spacing for $type1 and $type2 in $elementType")
        return Spacing.createSpacing(0, Int.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
    }

    private fun isEmptyBlock(element:PsiElement?) : Boolean {
        val hasBraces = element?.getSelfOrParentOfType(ObjJHasBraces::class.java) ?: return true
        var childBraceCount = 0
        if (hasBraces.openBrace != null) {
            childBraceCount++
        }
        if (hasBraces.closeBrace != null)
            childBraceCount++
        return element.children.filterNot { it.elementType == TokenType.WHITE_SPACE }.size == childBraceCount
    }

}

@Suppress("DEPRECATION")
private fun getObjJCodeStyleSettings(project:Project):ObjJCodeStyleSettings {
    return CodeStyleSettingsManager.getSettings(project).getCustomSettings(ObjJCodeStyleSettings::class.java)
}

fun ObjJPropertyAssignment.getObjectLiteralPropertySpacing() : Int? {
    // Find the longest length to a colon in this call
    // This is used as the basis for aligning objects
    val longestLengthToColon = this.getLongestLengthToColon() ?: return null
    val propertyNameLength = this.propertyName.textLength

    // Is first qualified selector, and on same line as prev sibling
    // Align this selector with those coming after
    val prevSibling = getPreviousNonEmptySibling(false)
    if (prevSibling != null && prevSibling.elementType != ObjJ_COMMA && !this.node.isDirectlyPrecededByNewline()) {
        val prevSiblingDistance = prevSibling.distanceFromStartOfLine() ?: return null
        return longestLengthToColon - prevSiblingDistance - propertyNameLength
    }
    // If node is on same line, and not first selector, add a single space
    if (!this.node.isDirectlyPrecededByNewline()) {
        return 1
    }

    // Calculate offset between this selectors colon, and the longest length to colon
    return if (longestLengthToColon > 0 && propertyNameLength < longestLengthToColon) {
        longestLengthToColon - propertyNameLength + EditorUtil.tabSize(this).orElse(0)
    } else {
        0
    }
}


/**
 * Gets the longest length from start of line to colon in this selector set
 */
fun ObjJPropertyAssignment.getLongestLengthToColon() : Int? {
    val objectLiteral = getParentOfType(ObjJObjectLiteral::class.java) ?: return null
    val distanceToFirstColon = 0
    val furthestColon = objectLiteral.propertyAssignmentList
            .filter { it.node?.isDirectlyPrecededByNewline() == true }
            .map { it.propertyName.textLength }
            .max()
    val tabSize = EditorUtil.tabSize(this) ?: 0
    // Offset secondary colons by the indent size
    // Hopefully this stays true
    return if (furthestColon != null)
        max(furthestColon + tabSize, distanceToFirstColon)
    else furthestColon ?: distanceToFirstColon
}

/**
 * Gets distance to first colon in this selector set
 */
fun ObjJPropertyAssignment.distanceToFirstColon() : Int? {
    val objectLiteral = getParentOfType(ObjJObjectLiteral::class.java) ?: return null
    val firstProperty = objectLiteral.propertyAssignmentList.getOrNull(0) ?: return null
    val prevSibling = firstProperty.getPreviousNonEmptySibling(false) ?: return firstProperty.propertyName.textLength
    val distance = prevSibling.distanceFromStartOfLine() ?: return null
    return distance + prevSibling.textLength + firstProperty.propertyName.textLength + 1
}

/**
 * Calculates the spacing necessary to align selector colons in this declaration
 */
fun ObjJMethodDeclarationSelector.getSelectorAlignmentSpacing(objJSettings: ObjJCodeStyleSettings) : Int? {

    val offset = (if (objJSettings.SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR) 1 else 0) +
            (if (objJSettings.SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE) 1 else 0)

    val longestLengthToColon = this.getParentOfType(ObjJHasMethodSelector::class.java)?.getLongestLengthToColon(offset).add(EditorUtil.tabSize(this).or(0)) ?: return null
    val thisSelectorLength = this.selector?.textLength ?: return null

    // If node is on same line, and not first selector, add a single space
    if (!this.node.isDirectlyPrecededByNewline()) {
        //LOGGER.info("Method Dec Selector: ${this.selector?.text ?: this.text} is not directly preceded by new line.")
        return 1
    }

    return if (longestLengthToColon > 0 && thisSelectorLength <= longestLengthToColon) {
        //LOGGER.info("Method Declaration selector spacing = ${longestLengthToColon - thisSelectorLength}; longest length to colon: $longestLengthToColon")
        longestLengthToColon - thisSelectorLength
    } else {
        null
    }
}

/**
 * Calculates the spacing necessary to align selector colons in this method call
 */
fun ObjJQualifiedMethodCallSelector.getSelectorAlignmentSpacing(indentFirstSelector:Boolean) : Int? {

    val selector = this.getParentOfType(ObjJHasMethodSelector::class.java) ?: return null

    // Find the longest length to a colon in this call
    // This is used as the basis for aligning objects
    val tabSize = EditorUtil.tabSize(this)
    val longestLengthToColon = selector.getLongestLengthToColon().add(tabSize) ?: return null
    val thisSelectorLength = this.selector?.text?.length ?: return null

    // If node is on same line, and not first selector, add a single space
    if (!this.node.isDirectlyPrecededByNewline()) {
        if (!indentFirstSelector || this.node.treeNext?.elementType != ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            return 1
        }
        val prevNode = this.node.getPreviousNonEmptyNode(false)?.psi ?: return 1
        val distanceToPrevNode = prevNode.distanceFromStartOfLine().add(1) ?: 0
        val out = if (longestLengthToColon > 0 && thisSelectorLength < longestLengthToColon) {
            longestLengthToColon - distanceToPrevNode - thisSelectorLength - EditorUtil.tabSize(this).orElse(0) + 1
        } else {
            1
        }
        return if (out > 0) out else 1
    }

    // Calculate offset between this selectors colon, and the longest length to colon
    return if (longestLengthToColon > 0 && thisSelectorLength < longestLengthToColon) {
        longestLengthToColon - thisSelectorLength - EditorUtil.tabSize(this).orElse(0)
    } else {
        0
    }
}

/**
 * Gets the longest length from start of line to colon in this selector set
 */
fun ObjJHasMethodSelector.getLongestLengthToColon(offsetFromStart:Int = 0) : Int {
    val indentSize = EditorUtil.tabSize(this).orElse(0)
    val distanceToFirstColon = distanceToFirstColon(offsetFromStart).orElse(indentSize) - indentSize
    val selectorList:List<ObjJSelector?>? = this.selectorList
    //LOGGER.info("Distance to first colon: $distanceToFirstColon")
    val maxLengthOfSelector = (selectorList?:listOf()).filterNotNull()
            .filter { it.node?.isDirectlyPrecededByNewline().orFalse() }
            .map { it.textLength + indentSize}
            .max() ?: 0
    if (this.selectorList.size < 2) {
        return 0
    }
    // Offset secondary colons by the indent size
    // Hopefully this stays true
    return max(maxLengthOfSelector, distanceToFirstColon)
}

/**
 * Gets distance to first colon in this selector set
 */
fun ObjJHasMethodSelector.distanceToFirstColon(offsetFromStart:Int = 0) : Int? {
    val selectors = this.selectorList
    val firstSelector = selectors.firstOrNull() ?: return null
    if (firstSelector.node.isDirectlyPrecededByNewline()) {
        return firstSelector.textLength + offsetFromStart + EditorUtil.tabSize(this).orElse(0)
    }
    val prevSibling = firstSelector.getPreviousNonEmptySibling(false) ?: return firstSelector.textLength
    val distance = prevSibling.distanceFromStartOfLine() ?: return null
    return distance + prevSibling.textLength + firstSelector.textLength + offsetFromStart
}