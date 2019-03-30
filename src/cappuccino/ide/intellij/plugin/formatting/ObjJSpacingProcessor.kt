@file:Suppress("unused", "UNUSED_PARAMETER")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
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


        if (elementType == DUMMY_BLOCK || type1 == DUMMY_BLOCK || type2 == DUMMY_BLOCK) {
            return Spacing.createSpacing(0, Int.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_AT_IMPLEMENTATION) {
            return Spacing.createSpacing(1, Int.MAX_VALUE, 0, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type2 == ObjJ_SINGLE_LINE_COMMENT && !isDirectlyPrecededByNewline(node2)) {
            // line comment after code on the same line: do not add line break here, it may be used to ignore warning
            // but after '{' in class or function definition Dart Style inserts line break, so let's do the same
            if (type1 !== ObjJ_OPEN_BRACE || ObjJTokenSets.CLASS_DECLARATIONS.contains(elementType) && !ObjJTokenSets.BLOCKS.contains(elementType)) {
                return Spacing.createSpacing(1, 1, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE, 0)
            }
        }

        if (type1 == ObjJ_SINGLE_LINE_COMMENT) {
            return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (IMPORT_STATEMENTS.contains(type1)) {
            if (type2 == ObjJ_BLOCK_COMMENT) {
                val next = FormatterUtil.getNextNonWhitespaceSibling(node2)
                if (next != null && next.elementType == type1) {
                    val needsNewline = isEmbeddedComment(type2, child2) && !isDirectlyPrecededByNewline(next)
                    val space = if (needsNewline) 0 else 1
                    val newline = if (needsNewline) 1 else 0
                    return Spacing.createSpacing(0, space, newline, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
            if (!ObjJTokenSets.IMPORT_STATEMENTS.contains(type2) && !isEmbeddedComment(type2, child2)) {
                run {
                    val numNewlines = if (ObjJTokenSets.COMMENTS.contains(type2) && isBlankLineAfterComment(node2)) 1 else 2
                    return Spacing.createSpacing(0, 0, numNewlines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
            }
        }

        if (ObjJ_SEMI_COLON == type2) {
            return if (type1 == ObjJ_SEMI_COLON && elementType in ObjJTokenSets.STATEMENTS) {
                addSingleSpaceIf(condition = false, linesFeed = true) // Empty statement on new line.
            } else Spacing.createSpacing(0, 0, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (ObjJ_AT_PROTOCOL == type1 || ObjJ_AT_END == type1 || ObjJ_AT_IMPLEMENTATION == type1)
            return Spacing.createSpacing(0, 0, 0, false, 0)

        if (ObjJTokenSets.FUNCTION_DECLARATIONS.contains(type2)) {
            val needsBlank = needsBlankLineBeforeFunction(elementType)
            val lineFeeds = if (ObjJTokenSets.COMMENTS.contains(type1) || !needsBlank) 1 else 2
            return Spacing.createSpacing(0, 0, lineFeeds, needsBlank, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        /*
        if (DOC_COMMENT_CONTENTS.contains(type2)) {
            return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }*/
        if (ObjJTokenSets.BLOCKS_EXT.contains(elementType)) {
            val topLevel = elementType == FILE
            var lineFeeds = if (node1 is PsiErrorElement || node2 is PsiErrorElement) 0 else 1
            var spaces = 0
            var blanks = mySettings.KEEP_BLANK_LINES_IN_CODE
            var keepBreaks = false
            /*if (type1 !in ObjJTokenSets.COMMENTS && (elementType in ObjJTokenSets.STATEMENTS || topLevel && type2 in ObjJTokenSets.DECLARATIONS)) {
                if (type1 == ObjJ_SEMI_COLON && type2 == ObjJ_INSTANCE_VARIABLE_DECLARATION) {
                    val node1TreePrev = node1.treePrev
                    if (node1TreePrev == null || node1TreePrev.elementType !== ObjJ_VARIAB:E) {
                        lineFeeds = 2
                    }
                } else {
                    if (type2 == VAR_DECLARATION_LIST && hasEmptyBlock(node1) || type1 == FUNCTION_TYPE_ALIAS && type2 == FUNCTION_TYPE_ALIAS) {
                        lineFeeds = 1
                    } else {
                        lineFeeds = 2
                    }
                }
            } else */if (type1 == ObjJ_OPEN_BRACE && type2 == ObjJ_CLOSE_BRACE) {
                if (parentType in ObjJTokenSets.FUNCTION_DECLARATIONS) {
                    if (myNode.treeParent.treeParent != null &&
                            myNode.treeParent.treeParent.elementType == ObjJ_METHOD_DECLARATION &&
                            mySettings.KEEP_SIMPLE_METHODS_IN_ONE_LINE) {
                        lineFeeds = 0 // Empty method.
                        keepBreaks = mySettings.KEEP_LINE_BREAKS
                        blanks = if (keepBreaks) mySettings.KEEP_BLANK_LINES_IN_CODE else 0
                    } else if (mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                        lineFeeds = 0 // Empty function, either top-level or statement.
                        keepBreaks = mySettings.KEEP_LINE_BREAKS
                        blanks = if (keepBreaks) mySettings.KEEP_BLANK_LINES_IN_CODE else 0
                    }
                } else if (parentType == ObjJ_IF_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_FOR_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_WHILE_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_DO_WHILE_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_TRY_STATEMENT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_FINALLY && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType in ObjJTokenSets.BLOCKS_EXT && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType in ObjJTokenSets.STATEMENTS && mySettings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
                    lineFeeds = 0
                } else if (parentType == ObjJ_INSTANCE_VARIABLE_LIST) {
                    lineFeeds = 0
                }
            } else if (topLevel && type2 in ObjJTokenSets.COMMENTS) {
                lineFeeds = 0
                spaces = 1
                keepBreaks = true
            } else if (type1 !== ObjJ_OPEN_BRACE && isEmbeddedComment(type2, child2)) {
                lineFeeds = 0
                spaces = 1
                keepBreaks = false
            } else if (type1 == ObjJ_OPEN_BRACE && type2 == ObjJTokenSets.STATEMENTS || type2 == ObjJ_CLOSE_BRACE && type1 == ObjJTokenSets.STATEMENTS) {
                lineFeeds = 0
                keepBreaks = false
                blanks = 0
            } else if (type1 == ObjJ_OPEN_BRACE && type2 == ObjJ_SINGLE_LINE_COMMENT) {
                lineFeeds = 1
                keepBreaks = false
                blanks = 0
            } else if (type1 == ObjJ_BLOCK_COMMENT && type2 in ObjJTokenSets.STATEMENTS) {
                spaces = 1
                lineFeeds = 0
                keepBreaks = true
            }

            val maxSpaces = if (node1 is PsiErrorElement || node2 is PsiErrorElement) Int.MAX_VALUE else spaces
            return Spacing.createSpacing(spaces, maxSpaces, lineFeeds, keepBreaks, blanks)
        }
        if (elementType == ObjJTokenSets.STATEMENTS && (parentType == ObjJ_CASE_CLAUSE || parentType == ObjJ_DEFAULT_CLAUSE)) {
            return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
        if (!ObjJTokenSets.COMMENTS.contains(type2) && ObjJTokenSets.BLOCKS.contains(parentType)
                && node1.treeNext !is PsiErrorElement && node1.lastChildNode !is PsiErrorElement) {
            return addLineBreak()
        }
        // Special checks for switch formatting according to dart_style, which conflicts with settings.
        if (type2 == ObjJ_CLOSE_BRACE && (type1 == ObjJ_CASE_CLAUSE || type1 == ObjJ_DEFAULT_CLAUSE)) {
            // No blank line before closing brace in switch statement.
            return Spacing.createSpacing(0, 0, 1, false, 0)
        }
        if (type1 == ObjJ_COLON && (elementType == ObjJ_CASE_CLAUSE || elementType == ObjJ_DEFAULT_CLAUSE)) {
            // No blank line before first statement of a case.
            return Spacing.createSpacing(0, 0, 1, false, 0)
        }
        if (elementType == ObjJ_SWITCH_STATEMENT && type1 == ObjJ_OPEN_BRACE) {
            // No blank line before first case of a switch.
            return Spacing.createSpacing(0, 0, 1, false, 0)
        }

        if (type1 == ObjJTokenSets.STATEMENTS || type2 == ObjJTokenSets.STATEMENTS) {
            return addLineBreak()
        }
        if (type1 in ObjJTokenSets.STATEMENTS || type2 in ObjJTokenSets.STATEMENTS) {
            return if (type1 == ObjJ_BLOCK_COMMENT) {
                addSingleSpaceIf(condition = true, linesFeed = false)
            } else {
                addSingleSpaceIf(condition = false, linesFeed = true)
            }
        }

        if (parentType == ObjJ_METHOD_HEADER_SELECTOR_FORMAL_VARIABLE_TYPE) {
            return noSpace()
        }

        if (type2 == ObjJ_OPEN_PAREN) {
            if (elementType == ObjJ_IF_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_BEFORE_IF_PARENTHESES)
            } else if (elementType == ObjJ_WHILE_STATEMENT || elementType == ObjJ_DO_WHILE_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_PARENTHESES)
            } else if (elementType == ObjJ_SWITCH_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_BEFORE_SWITCH_PARENTHESES)
            }/* else if (elementType == ON_PART || elementType == ObjJ_CATCH_PART) {
                return addSingleSpaceIf(mySettings.SPACE_BEFORE_CATCH_PARENTHESES)
            }*/
        }
        if (elementType == ObjJ_IF_STATEMENT) {
            if (type1 == ObjJ_CLOSE_PAREN && mySettings.BRACE_STYLE == CommonCodeStyleSettings.END_OF_LINE) {
                // Always have a single space following the closing paren of an if-condition.
                val nsp = if (mySettings.SPACE_BEFORE_IF_LBRACE) 1 else 0
                var lf = 0
                if (!ObjJTokenSets.BLOCKS.contains(type2) && mySettings.SPECIAL_ELSE_IF_TREATMENT) {
                    if (FormatterUtil.isFollowedBy(node2, ObjJ_ELSE, ObjJ_SEMI_COLON)) lf = 1
                }
                return Spacing.createSpacing(nsp, nsp, lf, !ObjJTokenSets.BLOCKS.contains(type2) && mySettings.KEEP_LINE_BREAKS, 0)
            }
            if (type1 == ObjJ_SEMI_COLON && type2 == ObjJ_ELSE) {
                // If the then-part is on the line with the condition put the else-part on the next line.
                return Spacing.createSpacing(0, 0, 1, false, 0)
            }
        }

        if (type2 == ObjJ_FOR_LOOP_HEADER) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_FOR_PARENTHESES)
        }

        if (type2 == ObjJ_FORMAL_PARAMETER_LIST && elementType in ObjJTokenSets.FUNCTION_DECLARATIONS) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_PARENTHESES)
        }

        if (elementType == ObjJ_METHOD_HEADER) {
            if (type1 == ObjJ_OPEN_PAREN && type2 == ObjJ_METHOD_HEADER_RETURN_TYPE_ELEMENT)
                return addSingleSpaceIf(objJSettings.SPACE_BETWEEN_TYPE_AND_PARENS)
            else if (type1 == ObjJ_METHOD_HEADER_RETURN_TYPE_ELEMENT && type2 == ObjJ_CLOSE_PAREN)
                return addSingleSpaceIf(objJSettings.SPACE_BETWEEN_TYPE_AND_PARENS)
            return noSpace()
        }

        if (type2 == ObjJ_FORMAL_VARIABLE_TYPE && elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            return addSingleSpaceIf(objJSettings.SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE)
        }

        if (type2 == ObjJ_VARIABLE_NAME && elementType in ObjJTokenSets.METHOD_HEADER_DECLARATION_SELECTOR) {
            return addSingleSpaceIf(objJSettings.SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME)
        }

        if (type2 == ObjJ_METHOD_HEADER_SELECTOR_FORMAL_VARIABLE_TYPE && elementType == ObjJ_METHOD_DECLARATION_SELECTOR) {
            return addSingleSpaceIf(objJSettings.SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE)
        }

        if (elementType == ObjJ_SELECTOR_LITERAL && type1 == ObjJ_SELECTOR && type2 == ObjJ_COLON) {
            return Spacing.createSpacing(0, 0, 1, false, 0)
        }

        /*
        if (type2 == ARGUMENTS && elementType == ObjJ_FUNCTION_CALL) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES)
        }*/

        //
        //Spacing before left braces
        //
        if (ObjJTokenSets.BLOCKS.contains(type2)) {
            if (elementType == ObjJ_IF_STATEMENT && type1 !== ObjJ_ELSE) {
                return setBraceSpace(mySettings.SPACE_BEFORE_IF_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_IF_STATEMENT && type1 == ObjJ_ELSE) {
                return setBraceSpace(mySettings.SPACE_BEFORE_ELSE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_WHILE_STATEMENT || elementType == ObjJ_DO_WHILE_STATEMENT) {
                return setBraceSpace(mySettings.SPACE_BEFORE_WHILE_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_FOR_STATEMENT) {
                return setBraceSpace(mySettings.SPACE_BEFORE_FOR_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_TRY_STATEMENT) {
                return setBraceSpace(mySettings.SPACE_BEFORE_TRY_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_FINALLY) {
                return setBraceSpace(mySettings.SPACE_BEFORE_FINALLY_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            } else if (elementType == ObjJ_CATCH) {
                return setBraceSpace(mySettings.SPACE_BEFORE_CATCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
            }
        }

        if (type2 == ObjJ_OPEN_BRACE && elementType == ObjJ_SWITCH_STATEMENT) {
            return setBraceSpace(mySettings.SPACE_BEFORE_SWITCH_LBRACE, mySettings.BRACE_STYLE, child1.getTextRange())
        }

        if (elementType in ObjJTokenSets.FUNCTION_DECLARATIONS && type2 == ObjJ_BLOCK_ELEMENT) {
            return setBraceSpace(mySettings.SPACE_BEFORE_METHOD_LBRACE, mySettings.METHOD_BRACE_STYLE, child1.getTextRange())
        }


        if (type1 == ObjJ_OPEN_PAREN || type2 == ObjJ_CLOSE_PAREN) {
            if (elementType == ObjJ_IF_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_IF_PARENTHESES)
            } else if (elementType == ObjJ_WHILE_STATEMENT || elementType == ObjJ_DO_WHILE_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_WHILE_PARENTHESES)
            } else if (elementType == ObjJ_FOR_LOOP_PARTS_IN_BRACES) {
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_FOR_PARENTHESES)
            } else if (elementType == ObjJ_SWITCH_STATEMENT) {
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_SWITCH_PARENTHESES)
            } /*else if (elementType == ObjJ_CATCH_PART) {
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_CATCH_PARENTHESES)
            } */else if (elementType == ObjJ_FORMAL_PARAMETER_LIST) {
                val newLineNeeded = if (type1 == ObjJ_OPEN_PAREN) mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE else mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE
                return if (newLineNeeded || mySettings.SPACE_WITHIN_METHOD_PARENTHESES) {
                    addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_PARENTHESES, newLineNeeded)
                } else Spacing.createSpacing(0, 0, 0, false, 0)
            } else if (elementType == ObjJ_ARGUMENTS) {
                val newLineNeeded = if (type1 == ObjJ_OPEN_PAREN) mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE else mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE
                return addSingleSpaceIf(mySettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES, newLineNeeded)
            } else if (mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP && elementType == ObjJ_ENCLOSED_EXPR) {
                val newLineNeeded = if (type1 == ObjJ_OPEN_PAREN) mySettings.PARENTHESES_EXPRESSION_LPAREN_WRAP else mySettings.PARENTHESES_EXPRESSION_RPAREN_WRAP
                return addSingleSpaceIf(false, newLineNeeded)
            }
        }

        if (elementType == ObjJ_TERNARY_EXPR_PRIME) {
            when {
                type2 == ObjJ_QUESTION_MARK -> return addSingleSpaceIf(mySettings.SPACE_BEFORE_QUEST)
                type2 == ObjJ_COLON -> return addSingleSpaceIf(mySettings.SPACE_BEFORE_COLON)
                type1 == ObjJ_QUESTION_MARK -> return addSingleSpaceIf(mySettings.SPACE_AFTER_QUEST)
                type1 == ObjJ_COLON -> return addSingleSpaceIf(mySettings.SPACE_AFTER_COLON)
            }
        }

        //
        // Spacing around assignment operators (=, -=, etc.)
        //

        if (type1 == ObjJ_ASSIGNMENT_OPERATOR || type2 == ObjJ_ASSIGNMENT_OPERATOR) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
        }

        if (type1 == ObjJ_EQUALS && elementType == ObjJ_EXPR) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
        }

        if (type2 == ObjJ_EXPR) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
        }

        //
        // Spacing around  logical operators (&&, OR, etc.)
        //
        if (ObjJTokenSets.LOGIC_OPERATORS.contains(type1) || ObjJTokenSets.LOGIC_OPERATORS.contains(type2)) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_LOGICAL_OPERATORS)
        }
        //
        // Spacing around  equality operators (==, != etc.)
        //
        if (type1 in ObjJTokenSets.EQUALITY_OPERATORS || type2 in ObjJTokenSets.EQUALITY_OPERATORS) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_EQUALITY_OPERATORS)
        }
        //
        // Spacing around  relational operators (<, <= etc.)
        //
        if (type1 == ObjJTokenSets.COMPARISON_OPERATORS || type2 == ObjJTokenSets.COMPARISON_OPERATORS) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_RELATIONAL_OPERATORS)
        }
        //
        // Spacing around  bitwise operators ( &, |, ^, etc.)
        //
        if (ObjJTokenSets.BITWISE_OPERATORS.contains(type1) || ObjJTokenSets.BITWISE_OPERATORS.contains(type2)) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_BITWISE_OPERATORS)
        }
        //
        // Spacing around  additive operators ( +, -, etc.)
        //
        if ((type1 in ObjJTokenSets.MATH_OPERATORS || type2 in ObjJTokenSets.MATH_OPERATORS) && elementType !== ObjJ_PREFIXED_EXPR) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_ADDITIVE_OPERATORS)
        }
        //
        // Spacing between successive unary operators ( -, + )
        //
        if (type1 in ObjJTokenSets.PREFIX_OPERATOR && type2 == ObjJ_EXPR) {
            val childs = node2.getChildren(ObjJTokenSets.PREFIX_OPERATOR)
            if (childs.isNotEmpty()) {
                return addSingleSpaceIf(isSpaceNeededBetweenPrefixOps(node1, childs[0]))
            }
        }
        //
        // Spacing around  unary operators ( NOT, ++, etc.)
        //
        if (type1 in ObjJTokenSets.UNARY_OPERATORS || type2 in ObjJTokenSets.UNARY_OPERATORS) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_UNARY_OPERATOR)
        }
        //
        // Spacing around  shift operators ( <<, >>, >>>, etc.)
        //
        if (type1 in ObjJTokenSets.SHIFT_OPERATOR_SET || type2 in ObjJTokenSets.SHIFT_OPERATOR_SET) {
            return addSingleSpaceIf(mySettings.SPACE_AROUND_SHIFT_OPERATORS)
        }

        //
        //Spacing before keyword (else, catch, etc)
        //
        if (type2 == ObjJ_ELSE) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_ELSE_KEYWORD, mySettings.ELSE_ON_NEW_LINE)
        }
        if (type2 == ObjJ_WHILE) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_WHILE_KEYWORD, mySettings.WHILE_ON_NEW_LINE)
        }
        if (type2 == ObjJ_FINALLY) {
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_FINALLY_KEYWORD, mySettings.CATCH_ON_NEW_LINE)
        }

        //
        //Other
        //

        if (type1 == ObjJ_ELSE) {
            if (type2 == ObjJ_IF_STATEMENT) {
                return Spacing.createSpacing(1, 1, if (mySettings.SPECIAL_ELSE_IF_TREATMENT) 0 else 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            if (type2 !== ObjJ_OPEN_BRACE) {
                // Keep single-statement else-part on same line?
                val lf = if (mySettings.SPECIAL_ELSE_IF_TREATMENT) 1 else 0
                return Spacing.createSpacing(1, 1, lf, !ObjJTokenSets.BLOCKS.contains(type2) && mySettings.KEEP_LINE_BREAKS, 0)
            }
        }

        val isBraces = type1 == ObjJ_OPEN_BRACE || type2 == ObjJ_CLOSE_BRACE
        if (ObjJTokenSets.COMMENTS.contains(type1)) {
            if (isBraces || type2 == ObjJ_SEMI_COLON) {
                return addLineBreak()
            }
            if (parentType == FILE &&
                    ObjJTokenSets.FUNCTION_DECLARATIONS.contains(elementType) &&
                    !(type1 == ObjJ_BLOCK_COMMENT && type2 == ObjJ_EXPR)) {
                return addLineBreak()
            }
        }

        if (elementType == ObjJ_INSTANCE_VARIABLE_LIST && type2 == ObjJ_INSTANCE_VARIABLE_DECLARATION) {
            return Spacing.createDependentLFSpacing(1, 1, myNode.treeParent.textRange, mySettings.KEEP_LINE_BREAKS,
                    mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (type1 == ObjJ_OPEN_BRACKET && type2 == ObjJ_CLOSE_BRACKET) {
            return noSpace()
        }
        if (type1 == ObjJ_COMMA && (elementType == ObjJ_FORMAL_PARAMETER_LIST || elementType == ObjJ_ARGUMENTS)) {
            return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA)
        }

        if (type1 == ObjJ_COMMA) {
            if (type2 == ObjJ_CLOSE_BRACKET) {
                val range = myNode.textRange
                return Spacing.createDependentLFSpacing(0, 0, range, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
            }
            return addSingleSpaceIf(mySettings.SPACE_AFTER_COMMA && type2 !== ObjJ_CLOSE_BRACE && type2 !== ObjJ_CLOSE_BRACKET)
        }

        if (type2 == ObjJ_COMMA) {
            if (type1 == ObjJ_COMMA) {
                return noSpace()
            }
            return addSingleSpaceIf(mySettings.SPACE_BEFORE_COMMA)
        }

        if (type1 == ObjJ_FOR_LOOP_PARTS_IN_BRACES && !ObjJTokenSets.BLOCKS_EXT.contains(type2)) {
            return addLineBreak()
        }

        if (type1 == ObjJ_IF_STATEMENT ||
                type1 == ObjJ_SWITCH_STATEMENT ||
                type1 == ObjJ_TRY_STATEMENT ||
                type1 == ObjJ_DO_WHILE_STATEMENT ||
                type1 == ObjJ_FOR_STATEMENT ||
                type1 == ObjJ_CASE_CLAUSE ||
                type1 == ObjJ_DEFAULT_CLAUSE ||
                type1 == ObjJ_WHILE_STATEMENT ||
                type1 in ObjJTokenSets.CLASS_DECLARATIONS ||
                type1 in ObjJTokenSets.FUNCTION_DECLARATIONS) {
            return addLineBreak()
        }

        if (ObjJTokenSets.COMMENTS.contains(type2)) {
            return Spacing.createSpacing(1, 1, 0, true, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (TOKENS_WITH_SPACE_AFTER.contains(type1) || KEYWORDS_WITH_SPACE_BEFORE.contains(type2)) {
            return Spacing.createDependentLFSpacing(1, 1, node1.textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }

        if (elementType == ObjJ_FOR_LOOP_PARTS_IN_BRACES && type1 == ObjJ_SEMI_COLON) {
            return addSingleSpaceIf(true)
        }

        if (((type1 == ObjJ_DOUBLE_QUO && type2 == ObjJ_DOUBLE_QUO)|| (type1 == ObjJ_SINGLE_QUO && type2 == ObjJ_SINGLE_QUO)) && elementType == ObjJ_STRING_LITERAL) {
            var sib:ASTNode? = node1
            var preserveNewline = 0
            // Adjacent strings on the same line should not be split.
            sib = sib?.treeNext
            while (sib != null) {
                // Comments are handled elsewhere.
                // TODO Create a test for this loop after adjacent-string wrapping is implemented.
                if (sib.elementType == WHITE_SPACE) {
                    val ws = sib.text
                    if (ws.contains("\n")) {
                        preserveNewline++
                        break
                    }
                    sib = sib.treeNext
                    continue
                }
                break
            }
            // Adjacent strings on separate lines should not include blank lines.
            return Spacing.createSpacing(0, 1, preserveNewline, true, 0)
        }

        if (elementType == ObjJ_ARRAY_LITERAL && type2 == ObjJ_CLOSE_BRACKET) {
            return Spacing.createDependentLFSpacing(0, 0, node1.textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }


        if (type2 == ObjJ_CLOSE_BRACKET && type1 == ObjJ_EXPR) {
            return noSpace()
        }

        if (elementType == ObjJ_QUALIFIED_REFERENCE && type2 == ObjJ_DOT) {
            return createSpacingForCallChain(collectSurroundingMessageSends(), node2)
        }
        if (type1 == ObjJ_DOT) {
            return noSpace()
        }

        return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)

    }

    private fun addLineBreak(): Spacing {
        return Spacing.createSpacing(0, 0, 1, false, mySettings.KEEP_BLANK_LINES_IN_CODE)
    }

    private fun addSingleSpaceIf(condition: Boolean, linesFeed: Boolean = false): Spacing {
        val spaces = if (condition) 1 else 0
        val lines = if (linesFeed) 1 else 0
        return Spacing.createSpacing(spaces, spaces, lines, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
    }

    private fun noSpace(): Spacing {
        return Spacing.createSpacing(0, 0, 0, mySettings.KEEP_LINE_BREAKS, 0)
    }

    private fun setBraceSpace(needSpaceSetting: Boolean,
                              @CommonCodeStyleSettings.BraceStyleConstant braceStyleSetting: Int,
                              textRange: TextRange?): Spacing {
        val spaces = if (needSpaceSetting) 1 else 0
        return if (braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED && textRange != null) {
            Spacing.createDependentLFSpacing(spaces, spaces, textRange, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        } else {
            val lineBreaks = if (braceStyleSetting == CommonCodeStyleSettings.END_OF_LINE || braceStyleSetting == CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED)
                0
            else
                1
            Spacing.createSpacing(spaces, spaces, lineBreaks, false, 0)
        }
    }

    private fun collectSurroundingMessageSends(): CallChain {
        val calls = CallChain()
        collectPredecessorMessageSends(calls)
        collectSuccessorMessageSends(calls)
        return calls
    }

    private fun collectPredecessorMessageSends(calls: CallChain) {
        var node: ASTNode? = myNode
        while (node != null) {
            val type = node.elementType
            if (type == ObjJ_QUALIFIED_REFERENCE) {
                collectDotIfMessageSend(calls, node)
                node = node.treeParent
            } else if (type == ObjJ_FUNCTION_CALL) {
                if (hasMultilineFunctionArgument(node)) {
                    calls.isFollowedByHardNewline = true
                    break
                }
                node = node.treeParent
            } else {
                break
            }
        }
    }

    private fun collectSuccessorMessageSends(calls: CallChain) {
        var node: ASTNode? = myNode
        while (node != null) {
            val type = node.elementType
            if (type == ObjJ_FUNCTION_CALL) {
                if (hasMultilineFunctionArgument(node)) {
                    calls.isPrecededByHardNewline = true
                    break
                }
                node = node.firstChildNode
            } else if (type == ObjJ_QUALIFIED_REFERENCE) {
                collectDotIfMessageSend(calls, node)
                node = node.firstChildNode
            } else {
                break
            }
        }
    }

    private fun createSpacingForCallChain(calls: CallChain, node2: ASTNode): Spacing {
        // The rules involving call chains, like m.a.b().c.d(), are complex.
        if (calls.list.size < 2) {
            return noSpace()
        }
        //if (calls.isPrecededByHardNewline) {
        //  // Rule: allow an inline chain before a hard newline but not after.
        //  return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
        //}
        var isAllProperties = true
        var mustSplit = false
        var mustStopAtNextMethod = false
        val ranges = ArrayList<TextRange>()
        for (node in calls.list) {
            if (doesMessageHaveArguments(node)) {
                if (mustStopAtNextMethod) {
                    return Spacing.createDependentLFSpacing(0, 0, ranges, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
                }
                isAllProperties = false
            } else {
                if (!isAllProperties) {
                    // Rule: split properties in a method chain.
                    mustSplit = true
                }
            }
            val range = node.textRange
            ranges.add(TextRange(range.startOffset - 1, range.endOffset))
            if (node2 == node && isAllProperties) {
                // Rule: do not split leading properties (unless too long to fit).
                mustStopAtNextMethod = true
            }
        }
        // Not sure how to implement rule: split before all properties if they don't fit on two lines. TWO lines !?
        if (isAllProperties && ranges.size > 7) {
            mustSplit = true
        }
        return if (mustSplit) {
            Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        } else {
            Spacing.createDependentLFSpacing(0, 0, ranges, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE)
        }
    }

    private class CallChain {
        internal var list = SortedList(textRangeSorter())
        internal var isPrecededByHardNewline = false
        internal var isFollowedByHardNewline = false

        internal fun add(node: ASTNode) {
            if (!list.contains(node)) {
                list.add(node)
            }
        }
    }

    companion object {
        private val TOKENS_WITH_SPACE_AFTER = TokenSet
                .create(ObjJ_VAR,
                        ObjJ_AT_FRAGMENT,
                        ObjJ_AT_IMPORT,
                        ObjJ_AT_CLASS,
                        ObjJ_AT_GLOBAL,
                        ObjJ_AT_TYPE_DEF,
                        ObjJ_AT_PROTOCOL,
                        ObjJ_AT_IMPLEMENTATION,
                        ObjJ_AT_INTERFACE,
                        ObjJ_AT_OPTIONAL,
                        ObjJ_AT_OUTLET,
                        ObjJ_PP_DEFINE,
                        ObjJ_PP_DEFINED,
                        ObjJ_PP_ELSE_IF,
                        ObjJ_PP_ERROR,
                        ObjJ_PP_IF,
                        ObjJ_PP_IF_DEF,
                        ObjJ_PP_IF_NDEF,
                        ObjJ_PP_DEFINED,
                        ObjJ_PP_WARNING,
                        ObjJ_PP_UNDEF,
                        ObjJ_PP_PRAGMA,
                        ObjJ_PP_INCLUDE,
                        ObjJ_INSTANCE_OF,
                        ObjJ_TYPE_OF,
                        ObjJ_CASE,
                        ObjJ_DEFAULT,
                        ObjJ_THROW,
                        ObjJ_IN,
                        ObjJ_LET,
                        ObjJ_CONST,
                        ObjJ_QUALIFIED_METHOD_CALL_SELECTOR
                )

        private val KEYWORDS_WITH_SPACE_BEFORE = TokenSet.create(
                ObjJ_AT_ACCESSORS,
                ObjJ_AT_REF,
                ObjJ_AT_DEREF,
                ObjJ_INSTANCE_OF,
                ObjJ_IN,
                ObjJ_MARK
        )

        private val ID_SET = TokenSet.create(ObjJ_ID)
        private val VAR_TYPE_SET = TokenSet.create(
                ObjJ_VAR_TYPE_BOOL,
                ObjJ_VAR_TYPE_BYTE,
                ObjJ_VAR_TYPE_CHAR,
                ObjJ_VAR_TYPE_DOUBLE,
                ObjJ_VAR_TYPE_FLOAT,
                ObjJ_VAR_TYPE_IBACTION,
                ObjJ_VAR_TYPE_UNSIGNED,
                ObjJ_VAR_TYPE_SIGNED,
                ObjJ_VAR_TYPE_SHORT,
                ObjJ_VAR_TYPE_SEL,
                ObjJ_VAR_TYPE_LONG_LONG,
                ObjJ_VAR_TYPE_LONG,
                ObjJ_VAR_TYPE_INT,
                ObjJ_VAR_TYPE_ID,
                ObjJ_VAR_TYPE_IBOUTLET
        )

        private val SIMPLE_LITERAL_SET = TokenSet.create(
                ObjJ_STRING_LITERAL,
                ObjJ_INTEGER_LITERAL,
                ObjJ_DECIMAL_LITERAL,
                ObjJ_BOOLEAN_LITERAL,
                ObjJ_NULL_LITERALS,
                ObjJ_UNDEFINED,
                ObjJ_THIS,
                ObjJ_ARRAY_LITERAL,
                ObjJ_OBJECT_LITERAL,
                ObjJ_SELECTOR_LITERAL,
                ObjJ_IMPORT_FRAMEWORK_LITERAL,
                ObjJ_REGULAR_EXPRESSION_LITERAL_TOKEN
        )
        private val SKIP_COMMA = TokenSet.create(ObjJ_COMMA)
        private val IMPORT_STATEMENTS = TokenSet.create(ObjJ_IMPORT_FRAMEWORK, ObjJ_IMPORT_FILE)


        private fun doesMessageHaveArguments(node: ASTNode): Boolean {
            // node is a DOT
            val parent = node.treeParent.treeParent ?: return false
            if (parent.elementType !== ObjJ_FUNCTION_CALL) return false
            val args = parent.lastChildNode ?: return false
            return args.elementType == ObjJ_ARGUMENTS
        }

        private fun textRangeSorter(): Comparator<ASTNode> {
            return Comparator.comparingInt { o -> o.textRange.startOffset }
        }

        private fun collectDotIfMessageSend(calls: CallChain, node: ASTNode) {
            var child: ASTNode? = node.firstChildNode
            child = FormatterUtil.getNextNonWhitespaceSibling(child)
            if (child != null) {
                val childType = child.elementType
                if (childType == ObjJ_DOT) {
                    calls.add(child)
                }
            }
        }

        private fun hasMultilineFunctionArgument(node: ASTNode): Boolean {
            var args: ASTNode? = node.lastChildNode
            val first = args?.firstChildNode
            args = first?.treeNext
            if (args != null && args.elementType == ObjJ_ARGUMENTS) {
                var arg: ASTNode? = args.firstChildNode
                var n = 1
                while (arg != null) {
                    // TODO Max 9 args is totally arbitrary, possibly not even desirable.
                    if (n++ == 10 || arg.elementType == ObjJ_EXPR) {
                        if (arg.text.indexOf('\n') >= 0) {
                            return true
                        }
                    }
                    arg = arg.treeNext
                }
            }
            return false
        }

        private fun allCascadesAreSameMethod(children: Array<ASTNode>): Boolean {
            for (i in 1 until children.size) {
                if (!cascadesAreSameMethod(children[i - 1], children[i])) {
                    return false
                }
            }
            return true
        }

        private fun cascadesAreSameMethod(child1: ASTNode, child2: ASTNode): Boolean {
            val call1 = child1.lastChildNode
            if (call1.elementType == ObjJ_FUNCTION_CALL) {
                val call2 = child2.lastChildNode
                if (call2.elementType == ObjJ_FUNCTION_CALL) {
                    val name1 = getImmediateCallName(call1)
                    if (name1 != null) {
                        val name2 = getImmediateCallName(call2)
                        if (name1 == name2) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        private fun getImmediateCallName(callNode: ASTNode): String? {
            var childs = callNode.getChildren(ObjJTokenSets.REFERENCE_EXPRESSION_SET)
            if (childs.size != 1) return null
            var child = childs[0]
            childs = child.getChildren(ID_SET)
            if (childs.size != 1) return null
            child = childs[0]
            return child.text
        }

        private fun isSpaceNeededBetweenPrefixOps(node1: ASTNode, node2: ASTNode): Boolean {
            val op1 = node1.text
            val op2 = node2.text
            return op1.endsWith(op2.substring(op2.length - 1))
        }

        private fun needsBlankLineBeforeFunction(elementType: IElementType): Boolean {
            return true
        }

        private fun isEmbeddedComment(type: IElementType, block: Block): Boolean {
            return ObjJTokenSets.COMMENTS.contains(type) && (!isDirectlyPrecededByNewline(block) || isDirectlyPrecededByBlockComment(block))
        }

        private fun isDirectlyPrecededByNewline(child: Block): Boolean {
            // The child is a line comment whose parent is the FILE.
            // Return true if it is (or will be) at the beginning of the line, or
            // following a block comment that is at the beginning of the line.
            val node = (child as ObjJFormattedBlock).node
            return isDirectlyPrecededByNewline(node)
        }

        private fun isDirectlyPrecededByNewline(nodeIn: ASTNode): Boolean {
            var node:ASTNode? = nodeIn
            node = node?.treePrev
            while (node != null) {
                if (node.elementType == WHITE_SPACE) {
                    if (node.text.contains("\n")) return true
                    node = node.treePrev
                    continue
                }
                if (node.elementType == ObjJ_BLOCK_COMMENT) {
                    if (node.treePrev == null) {
                        return true
                    }
                    node = node.treePrev
                    continue
                }
                break
            }
            return false
        }

        private fun getPrevSiblingOnTheSameLineSkipCommentsAndWhitespace(nodeIn: ASTNode): ASTNode? {
            var node:ASTNode? = nodeIn.treePrev
            while (node != null) {
                return if (node.elementType == WHITE_SPACE || ObjJTokenSets.COMMENTS.contains(node.elementType)) {
                    if (node.text.contains("\n")) {
                        null
                    } else {
                        node = node.treePrev
                        continue
                    }
                } else node
            }

            return null
        }

        private fun isDirectlyPrecededByBlockComment(child: Block): Boolean {
            val node = (child as ObjJFormattedBlock).node
            return isDirectlyPrecededByBlockComment(node)
        }

        private fun isDirectlyPrecededByBlockComment(nodeIn: ASTNode): Boolean {
            var node:ASTNode? = nodeIn
            while (node != null) {
                if (node.elementType == WHITE_SPACE) {
                    if (node.text.contains("\n")) return false
                    node = node.treePrev
                    continue
                }
                if (node.elementType == ObjJ_BLOCK_ELEMENT) {
                    return true
                }
                break
            }
            return false
        }

        private fun isBlankLineAfterComment(node: ASTNode): Boolean {
            // Assumes whitespace has been normalized.
            val next = node.treeNext
            if (next == null || next.elementType !== WHITE_SPACE) return false
            val comment = next.text
            val n = comment.indexOf('\n')
            return comment.indexOf('\n', n + 1) > 0
        }
    }
}