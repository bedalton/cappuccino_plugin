@file:Suppress("unused", "UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenType
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.SortedList

import java.util.ArrayList
import java.util.Comparator
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import com.intellij.lang.parser.GeneratedParserUtilBase.DUMMY_BLOCK
import com.intellij.psi.TokenType.WHITE_SPACE
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FILE
import com.intellij.formatting.Indent
import java.util.logging.Logger

class ObjJSpacingProcessor(private val myNode: ASTNode, private val mySettings: CommonCodeStyleSettings, private val objJSettings: ObjJCodeStyleSettings) {

    val LOGGER:Logger by lazy {
        Logger.getLogger("#ObjJSPacingProcessor")
    }

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

        if (type2 == ObjJ_SEMI_COLON) {
            return Spacing.createSpacing(0,0, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_SINGLE_LINE_COMMENT) {
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.IMPORT_BLOCKS) {
            return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_METHOD_HEADER) {
            if (type1 == ObjJ_METHOD_SCOPE_MARKER) {
                return Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type1 in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
                return Spacing.createSpacing(1,1,0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0,0,0,false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 in ObjJTokenSets.BLOCKS && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0,0,1,mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            return Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (elementType == ObjJ_DO_WHILE_STATEMENT) {
            if (type1 == ObjJ_DO && type2 == ObjJ_STATEMENT_OR_BLOCK) {
                return if (mySettings.SPACE_BEFORE_DO_LBRACE && !objJSettings.BRACE_ON_NEW_LINE) {
                    Spacing.createSpacing(1, 1, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else if (objJSettings.BRACE_ON_NEW_LINE) {
                    Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (type2 == ObjJ_STATEMENT_OR_BLOCK && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0, 0, 1, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 in ObjJTokenSets.IMPORT_BLOCKS) {
            return Spacing.createSpacing(0, 0, 2, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
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

        if (type1 == ObjJ_BODY_VARIABLE_ASSIGNMENT && objJSettings.GROUP_STATMENTS) {
            if (type2 != ObjJ_BODY_VARIABLE_ASSIGNMENT) {
                return Spacing.createSpacing(0, 0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 in ObjJTokenSets.EXPRESSIONS && type2 == ObjJ_BODY_VARIABLE_ASSIGNMENT && objJSettings.GROUP_STATMENTS) {
            return Spacing.createSpacing(0, 0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_STATEMENT_OR_BLOCK && type1 in ObjJTokenSets.EXPRESSIONS && type2 !in ObjJTokenSets.EXPRESSIONS) {
            return Spacing.createSpacing(0, 0, 2, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 in ObjJTokenSets.NEW_LINE_AFTER && objJSettings.NEW_LINE_AFTER_BLOCKS) {
            return Spacing.createSpacing(0, 0, 2, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (parentType == ObjJTokenSets.BLOCKS) {
            if (elementType == ObjJ_OPEN_BRACE || elementType == ObjJ_CLOSE_BRACE) {
                return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return Spacing.createSpacing(0, Int.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_OPEN_BRACE && objJSettings.BRACE_ON_NEW_LINE) {
            return Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
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
                return Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }

            if (type1 == ObjJ_CASE_CLAUSE && (type2 == ObjJ_CASE_CLAUSE || type2 == ObjJ_DEFAULT_CLAUSE)) {
                return if (node1.lastChildNode?.elementType == ObjJ_BRACKET_LESS_BLOCK && mySettings.CASE_STATEMENT_ON_NEW_LINE) {
                    Spacing.createSpacing(0,0, 2, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                } else {
                    Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
            if (type1 == ObjJ_CLOSE_PAREN && type2 == ObjJ_OPEN_BRACE) {
                return when {
                    objJSettings.BRACE_ON_NEW_LINE -> Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    mySettings.SPACE_BEFORE_DO_LBRACE -> Spacing.createSpacing(1,1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                    else -> Spacing.createSpacing(0,1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
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
                return Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (elementType == ObjJ_IMPLEMENTATION_DECLARATION || elementType == ObjJ_PROTOCOL_DECLARATION) {
            if (type1 == ObjJ_CLASS_NAME && type2 == ObjJ_INSTANCE_VARIABLE_LIST) {
                return Spacing.createSpacing(0,0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
        }

        if (type2 == ObjJ_STATEMENT_OR_BLOCK) {
            return Spacing.createSpacing(0,0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        LOGGER.info("Getting Spacing for $type1 and $type2 in $elementType")
        return Spacing.createSpacing(0, Int.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
    }

}