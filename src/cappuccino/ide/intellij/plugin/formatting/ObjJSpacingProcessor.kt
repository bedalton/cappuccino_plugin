@file:Suppress("unused", "UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasBraces
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.lang.parser.GeneratedParserUtilBase.DUMMY_BLOCK
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import java.util.logging.Logger
import kotlin.math.max


private val LOGGER: Logger by lazy {
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

        if (type2 == ObjJ_STATEMENT_OR_BLOCK || type2 == ObjJ_BLOCK_ELEMENT) {
            val braceType = when (elementType) {
                ObjJ_IF_STATEMENT -> mySettings.IF_BRACE_FORCE
                ObjJ_ELSE_IF_STATEMENT -> mySettings.IF_BRACE_FORCE
                ObjJ_ELSE_STATEMENT -> mySettings.IF_BRACE_FORCE
                ObjJ_FOR_STATEMENT -> mySettings.FOR_BRACE_FORCE
                ObjJ_WHILE_STATEMENT -> mySettings.WHILE_BRACE_FORCE
                ObjJ_DO_WHILE_STATEMENT -> mySettings.DOWHILE_BRACE_FORCE
                ObjJ_FORMAL_PARAMETER_LIST -> objJSettings.FUNCTION_BRACE_FORCE
                ObjJ_CATCH_PRODUCTION -> objJSettings.CATCH_BRACE_FORCE
                ObjJ_FINALLY_PRODUCTION -> objJSettings.FINALLY_BRACE_FORCE
                ObjJ_FUNCTION_LITERAL -> objJSettings.FUNCTION_BRACE_FORCE
                ObjJ_FUNCTION_DECLARATION -> objJSettings.FUNCTION_BRACE_FORCE
                ObjJ_TRY_STATEMENT -> if (objJSettings.TRY_ON_NEW_LINE) CommonCodeStyleSettings.FORCE_BRACES_ALWAYS else CommonCodeStyleSettings.DO_NOT_FORCE
                else -> {
                    LOGGER.severe("Failed to match parentType($elementType) for element: $type1")
                    CommonCodeStyleSettings.DO_NOT_FORCE
                }
            }
            val force = braceType == CommonCodeStyleSettings.FORCE_BRACES_ALWAYS
                    || (braceType == CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE && node2.psi.getChildrenOfType(ObjJCompositeElement::class.java).size > 1)
            val needsSpace = when (elementType) {
                ObjJ_IF_STATEMENT -> mySettings.SPACE_BEFORE_IF_LBRACE
                ObjJ_ELSE_IF_STATEMENT -> mySettings.SPACE_BEFORE_ELSE_LBRACE
                ObjJ_ELSE_STATEMENT -> mySettings.SPACE_BEFORE_ELSE_LBRACE
                ObjJ_FOR_STATEMENT -> mySettings.SPACE_BEFORE_FOR_LBRACE
                ObjJ_WHILE_STATEMENT -> mySettings.SPACE_BEFORE_WHILE_LBRACE
                ObjJ_DO_WHILE_STATEMENT -> mySettings.SPACE_BEFORE_DO_LBRACE
                ObjJ_FORMAL_PARAMETER_LIST -> mySettings.SPACE_BEFORE_METHOD_LBRACE
                ObjJ_TRY_STATEMENT -> mySettings.SPACE_BEFORE_TRY_LBRACE
                ObjJ_CATCH_PRODUCTION -> mySettings.SPACE_BEFORE_CATCH_LBRACE
                ObjJ_FINALLY_PRODUCTION -> mySettings.SPACE_BEFORE_FINALLY_LBRACE
                ObjJ_FUNCTION_LITERAL -> mySettings.SPACE_BEFORE_METHOD_LBRACE
                ObjJ_FUNCTION_DECLARATION -> mySettings.SPACE_BEFORE_METHOD_LBRACE
                ObjJ_TRY_STATEMENT -> mySettings.SPACE_BEFORE_TRY_LBRACE
                else -> false
            } && !force
            val spacing = if (needsSpace) 1 else 0
            val lines = if (force) 1 else 0
            return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_SWITCH) {
            val spacing = if (mySettings.SPACE_BEFORE_SWITCH_PARENTHESES) 1 else 0
            return Spacing.createSpacing(spacing, spacing, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if (type1 == ObjJ_CLOSE_PAREN && elementType == ObjJ_SWITCH_STATEMENT) {
            val spacing = if (mySettings.SPACE_BEFORE_SWITCH_LBRACE) 1 else 0
            val lines = if (objJSettings.SWITCH_BRACE_FORCE != CommonCodeStyleSettings.DO_NOT_FORCE) 1 else 0
            return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_CONDITION_EXPRESSION) {
            val addSpace = when (type1) {
                ObjJ_IF -> mySettings.SPACE_BEFORE_IF_PARENTHESES
                ObjJ_ELSE -> mySettings.SPACE_BEFORE_IF_PARENTHESES
                ObjJ_WHILE -> mySettings.SPACE_BEFORE_WHILE_PARENTHESES
                else -> false
            }
            val spacing = if (addSpace) 1 else 0
            return Spacing.createSpacing(spacing, spacing, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_FUNCTION_NAME && type2 == ObjJ_ARGUMENTS) {
            val spacing = if (mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES) 1 else 0
            return Spacing.createSpacing(spacing, spacing, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_FORMAL_PARAMETER_LIST) {
            val spacing = if (mySettings.SPACE_BEFORE_METHOD_PARENTHESES) 1 else 0
            return Spacing.createSpacing(spacing, spacing, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (type1 == ObjJ_TRY || type2 == ObjJ_TRY) {
            val spacing = if (mySettings.SPACE_BEFORE_TRY_LBRACE) 1 else 0
            val lines = if (objJSettings.TRY_ON_NEW_LINE) 1 else 0
            return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_INSTANCE_VARIABLE_LIST) {
            return if (objJSettings.INSTANCE_VARIABLE_LIST_BRACE_FORCE == CommonCodeStyleSettings.DO_NOT_FORCE)
                Spacing.createSpacing(0, Int.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
            else
                Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_STATEMENT_OR_BLOCK && type2 == ObjJ_ELSE_IF_STATEMENT) {
            val spacing = if(mySettings.SPACE_BEFORE_ELSE_KEYWORD) 1 else 0
            return getSpacingIfNewLine(mySettings.ELSE_ON_NEW_LINE, spacing)
        }

        if (type2 == ObjJ_WHILE && elementType == ObjJ_DO_WHILE_STATEMENT) {
            val spacing = if (mySettings.SPACE_BEFORE_WHILE_KEYWORD) 1 else 0
            val lines = if (mySettings.WHILE_ON_NEW_LINE) 1 else 0
            return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_CATCH) {
            val spacing = if (mySettings.SPACE_BEFORE_CATCH_PARENTHESES) 1 else 0
            return Spacing.createSpacing(spacing, spacing, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_CATCH || type2 == ObjJ_CATCH_PRODUCTION) {
            val spacing = if (mySettings.SPACE_BEFORE_CATCH_KEYWORD) 1 else 0
            return getSpacingIfNewLine(mySettings.CATCH_ON_NEW_LINE, spacing)
        }

        if (type2 == ObjJ_FINALLY || type2 == ObjJ_FINALLY_PRODUCTION) {
            val spacing = if (mySettings.SPACE_BEFORE_FINALLY_KEYWORD) 1 else 0
            return getSpacingIfNewLine(mySettings.FINALLY_ON_NEW_LINE, spacing)
        }

        if (type2 == ObjJ_ELSE_STATEMENT || type2 == ObjJ_ELSE_IF_STATEMENT) {
            val spacing = if (type2 == ObjJ_ELSE_STATEMENT && mySettings.SPACE_BEFORE_ELSE_KEYWORD) 1 else 0
            val lines = if (mySettings.ELSE_ON_NEW_LINE) 1 else 0
            return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)

        }
        if (type1 == ObjJ_IF && mySettings.SPACE_BEFORE_IF_PARENTHESES) {
            return Spacing.createSpacing(1, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        } else if (type1 == ObjJ_IF) {
            return Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if (type1 == ObjJ_CONDITION_EXPRESSION && type2 == ObjJ_OPEN_BRACE && mySettings.SPACE_BEFORE_IF_LBRACE) {
            return Spacing.createSpacing(1, Int.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_FORMAL_PARAMETER_LIST && type2 == ObjJ_BLOCK_ELEMENT) {
            val isMultiLine = node2.psi.getChildrenOfType(ObjJCompositeElement::class.java).size > 1
            val force = objJSettings.FUNCTION_BRACE_FORCE == CommonCodeStyleSettings.FORCE_BRACES_ALWAYS
                    || (objJSettings.FUNCTION_BRACE_FORCE == CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE && isMultiLine)
            return if (force)
                Spacing.createSpacing(0, Int.MAX_VALUE, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
            else
                Spacing.createSpacing(0, Int.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (myNode.psi.hasParentOfType(ObjJPreprocessorDefineFunction::class.java))
            return null

        if (type1 == ObjJ_AT_IMPLEMENTATION) {
            return Spacing.createSpacing(1, Int.MAX_VALUE, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_AT_IMPORT || type1 == ObjJ_PP_INCLUDE) {
            return Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_COMMA) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_SEMI_COLON) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 in ObjJTokenSets.COMMENTS) {
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.IMPORT_BLOCKS) {
            return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (myNode.psi?.isOrHasParentOfType(ObjJSelectorLiteral::class.java).orFalse()) {
            return Spacing.createSpacing(0, 0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if ((type1 == ObjJ_BLOCK_ELEMENT || type2 == ObjJ_BLOCK_ELEMENT) && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if ((type1 == ObjJ_CATCH_PRODUCTION || type2 == ObjJ_CATCH_PRODUCTION) && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if ((type1 == ObjJ_FINALLY_PRODUCTION || type2 == ObjJ_FINALLY_PRODUCTION) && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if ((type1 == ObjJ_BLOCK_ELEMENT || type2 == ObjJ_BLOCK_ELEMENT) && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
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
                return Spacing.createSpacing(spacing, spacing, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
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

        if (type2 in ObjJTokenSets.HAS_BRACES && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
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
                return if (objJSettings.SPACE_BEFORE_LBRACE && objJSettings.BRACE_ON_NEW_LINE == CommonCodeStyleSettings.DO_NOT_FORCE) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else if (objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
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

        if (type2 == ObjJ_STATEMENT_OR_BLOCK && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_FINALLY && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
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
        if (type1 == ObjJ_COMMA && mySettings.SPACE_AFTER_COMMA) {
            Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
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

        if (type1 == ObjJ_AND || type1 == ObjJ_OR) {
            return Spacing.createSpacing(1, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
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

        if ((type2 == ObjJ_OPEN_BRACE || type2 == ObjJ_CLOSE_BRACE) && objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE) {
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
                    objJSettings.BRACE_ON_NEW_LINE != CommonCodeStyleSettings.DO_NOT_FORCE -> Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
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

    private fun isEmptyBlock(element: PsiElement?): Boolean {
        val hasBraces = element?.getSelfOrParentOfType(ObjJHasBraces::class.java) ?: return true
        var childBraceCount = 0
        if (hasBraces.openBrace != null) {
            childBraceCount++
        }
        if (hasBraces.closeBrace != null)
            childBraceCount++
        return element.children.filterNot { it.elementType == TokenType.WHITE_SPACE }.size == childBraceCount
    }

    private fun getSpacingIfNewLine(newLine: Boolean, spacing: Int = 0): Spacing {
        val lines = if (newLine) 1 else 0
        return Spacing.createSpacing(spacing, spacing, lines, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
    }

}

@Suppress("DEPRECATION")
private fun getObjJCodeStyleSettings(project: Project): ObjJCodeStyleSettings {
    return CodeStyleSettingsManager.getSettings(project).getCustomSettings(ObjJCodeStyleSettings::class.java)
}

fun ObjJPropertyAssignment.getObjectLiteralPropertySpacing(): Int? {
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
        longestLengthToColon - propertyNameLength
    } else {
        0
    }
}


/**
 * Gets the longest length from start of line to colon in this selector set
 */
fun ObjJPropertyAssignment.getLongestLengthToColon(): Int? {
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
fun ObjJPropertyAssignment.distanceToFirstColon(): Int? {
    val objectLiteral = getParentOfType(ObjJObjectLiteral::class.java) ?: return null
    val firstProperty = objectLiteral.propertyAssignmentList.getOrNull(0) ?: return null
    val prevSibling = firstProperty.getPreviousNonEmptySibling(false) ?: return firstProperty.propertyName.textLength
    val distance = prevSibling.distanceFromStartOfLine() ?: return null
    return distance + prevSibling.textLength + firstProperty.propertyName.textLength + 1
}

/**
 * Calculates the spacing necessary to align selector colons in this declaration
 */
fun ObjJMethodDeclarationSelector.getSelectorAlignmentSpacing(objJSettings: ObjJCodeStyleSettings): Int? {

    val offset = (if (objJSettings.SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR) 1 else 0) +
            (if (objJSettings.SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE) 1 else 0)

    val longestLengthToColon = this.getParentOfType(ObjJHasMethodSelector::class.java)?.getLongestLengthToColon(offset).add(EditorUtil.tabSize(this).or(0))
            ?: return null
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
fun ObjJQualifiedMethodCallSelector.getSelectorAlignmentSpacing(indentFirstSelector: Boolean): Int? {

    val methodCall = this.parent as? ObjJMethodCall ?: return null

    val shouldSpace = methodCall.node?.getChildren(ObjJTokenSets.WHITE_SPACE).orEmpty().any {
        it.text.contains("\n") && it.treeNext.elementType != ObjJ_CLOSE_BRACE
    }
    if (!shouldSpace)
        return 1
    // Find the longest length to a colon in this call
    // This is used as the basis for aligning objects
    val tabSize = EditorUtil.tabSize(this).orElse(0)
    val longestLengthToColon = methodCall.getLongestLengthToColon(tabSize)
    val thisSelectorLength = this.selector?.text?.length?.plus(1) ?: return null
    // Determine values for first in line or first selector
    val isFirstSelectorInCall: Boolean = this.node.getPreviousNonEmptyNode(true)?.elementType == ObjJ_CALL_TARGET
    val isFirstInLine = this.node.isDirectlyPrecededByNewline()

    // Return single space if not first in line and not first selector
    if (!isFirstInLine && !isFirstSelectorInCall)
        return 1

    val distanceToFirstColon = methodCall.distanceToFirstColon().orElse(0)
    // If node is on same line, and not first selector, add a single space
    if (!isFirstInLine && isFirstSelectorInCall) {
        if (!indentFirstSelector) {
            return 1
        }
        val out = if (longestLengthToColon > 0 && distanceToFirstColon < longestLengthToColon) {
            (longestLengthToColon - distanceToFirstColon) + tabSize
        } else {
            1
        }

        return if (out > 0) out else 1
    }
    // Calculate offset between this selectors colon, and the longest length to colon
    val offset = if (longestLengthToColon == distanceToFirstColon) -2 else 1
    return if (longestLengthToColon > 0 && thisSelectorLength < longestLengthToColon) {
        (longestLengthToColon - thisSelectorLength) + offset + tabSize
    } else {
        0
    }
}

/**
 * Gets the longest length from start of line to colon in this selector set
 */
fun ObjJHasMethodSelector.getLongestLengthToColon(tabSize: Int, offsetFromStart: Int = 0): Int {
    val distanceToFirstColon = when (this) {
        is ObjJMethodCall -> this.distanceToFirstColon(offsetFromStart)
        is ObjJMethodHeaderDeclaration<*> -> this.distanceToFirstColon(tabSize, offsetFromStart)
        else -> null
    }
    val selectorList: List<ObjJSelector?>? = this.selectorList
    val maxLengthOfSelector = (selectorList ?: listOf()).filterNotNull()
            .filter { it.node?.isDirectlyPrecededByNewline().orFalse() }
            .map { it.textLength }
            .max() ?: 0
    if (this.selectorList.size < 2) {
        return 0
    }
    // Offset secondary colons by the indent size
    // Hopefully this stays true
    if (distanceToFirstColon != null)
        return max(0, max(maxLengthOfSelector, distanceToFirstColon))
    return max(0, maxLengthOfSelector)
}


/**
 * Gets distance to first colon in this selector set
 */
fun ObjJMethodCall.distanceToFirstColon(offsetFromStart: Int = 0): Int? {
    val firstSelector = selectorList.firstOrNull() ?: return null
    if (firstSelector.getPreviousNonEmptyNode(false) == null)
        return null
    val offsetCallTarget = ((this.distanceFromStartOfLine().orElse(0) + 1) - callTarget.distanceFromStartOfLine().orElse(0)) + callTarget.text.length
    return offsetCallTarget + 1 + firstSelector.text.length
}

/**
 * Gets distance to first colon in this selector set
 */
fun ObjJMethodHeaderDeclaration<*>.distanceToFirstColon(tabSize: Int, offsetFromStart: Int = 0): Int? {
    val selectors = this.selectorList
    val firstSelector = selectors.firstOrNull() ?: return null
    if (firstSelector.node.isDirectlyPrecededByNewline()) {
        return firstSelector.textLength + tabSize
    }
    val prevSibling = firstSelector.getPreviousNonEmptySibling(false) ?: return firstSelector.textLength + tabSize
    val distance = prevSibling.distanceFromStartOfLine() ?: return null
    return distance + prevSibling.textLength + firstSelector.textLength + 1
}