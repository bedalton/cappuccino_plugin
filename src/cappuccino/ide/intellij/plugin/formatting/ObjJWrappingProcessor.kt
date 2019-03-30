package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.WrappingUtil

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*


// TODO Eliminate redundancy. This gets called multiple times by CodeStyleManagerImpl.reformatText().
// The first is by a call to CodeFormatterFacade.processText() at line 235.
// The second is from a call to EditorEx.reinitSettings() at line 251.
// The second is only done when reformatting the entire file; however, when
// reformatting a selection this may be called three times.
class ObjJWrappingProcessor(private val myNode: ASTNode, private val mySettings: CommonCodeStyleSettings) {

    private val parent: ASTNode?
        get() = myNode.treeParent

    internal fun createChildWrap(child: ASTNode, defaultWrap: Wrap, childWrap: Wrap?): Wrap {
        val childType = child.elementType
        val elementType = myNode.elementType
        if (childType === ObjJ_COMMA || childType === ObjJ_SEMI_COLON) return defaultWrap
        //
        // Function definition/call
        //
        if (elementType === ObjJ_FORMAL_PARAMETER_LIST) {
            if (child is PsiErrorElement) {
                myNode.putUserData(ObjJ_ARGUMENT_LIST_WRAP_KEY, null)
            }

            if (mySettings.CALL_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
                if (!mySettings.PREFER_PARAMETERS_WRAP && childWrap != null) {
                    // Not used; PREFER_PARAMETERS_WRAP cannot be changed in the UI.
                    return Wrap.createChildWrap(childWrap, WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), true)
                }
                var wrap: Wrap? = null
                // First, do persistent object management.
                if (myNode.firstChildNode === child && childType !== ObjJ_FORMAL_PARAMETER_ARG) {
                    val childs = myNode.getChildren(ObjJIndentProcessor.EXPRESSIONS)
                    wrap = if (childs.size >= 7) { // Approximation; dart_style uses dynamic programming with cost-based analysis to choose.
                        Wrap.createWrap(WrapType.ALWAYS, true)
                    } else {
                        Wrap.createWrap(WrapType.NORMAL, true) // NORMAL,CHOP_DOWN_IF_LONG
                    }
                    if (myNode.lastChildNode !== child) {
                        myNode.putUserData(ObjJ_ARGUMENT_LIST_WRAP_KEY, wrap)
                    }
                }
                // Second, decide what object to return.
                return if (childType === ObjJ_BLOCK_COMMENT || childType === ObjJ_FUNCTION_DECLARATION || childType === ObjJ_FUNCTION_LITERAL) {
                    Wrap.createWrap(WrapType.NONE, false)
                } else wrap ?: Wrap.createWrap(WrappingUtil.getWrapType(mySettings.CALL_PARAMETERS_WRAP), false)
            }
        }

        if (elementType === ObjJ_FORMAL_PARAMETER_LIST) {
            if (mySettings.METHOD_PARAMETERS_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
                if (myNode.firstChildNode === child) {
                    return createWrap(mySettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE)
                }
                return if (childType === ObjJ_CLOSE_PAREN) {
                    createWrap(mySettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE)
                } else Wrap.createWrap(WrappingUtil.getWrapType(mySettings.METHOD_PARAMETERS_WRAP), true)
            }
        }

        // Lists in schematic s-expr notation:
        // (LIST_LITERAL_EXPRESSION '[ (EXPRESSION_LIST expr ', expr) '])
        /*if (elementType == ObjJ_EXPRESSION_LIST) {
            Wrap wrap;
            // First, do persistent object management.
            if (myNode.getFirstChildNode() == child) {
                wrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
                if (myNode.getLastChildNode() != child) {
                    myNode.putUserData(ObjJ_EXPRESSION_LIST_WRAP_KEY, wrap);
                }
            }
            else {
                wrap = myNode.getUserData(ObjJ_EXPRESSION_LIST_WRAP_KEY);
            }
            // Second, decide what object to return.
            if (childType == MULTI_LINE_COMMENT || childType == CONST) {
                return Wrap.createWrap(WrapType.NONE, false);
            }
            return wrap != null ? wrap : Wrap.createWrap(WrapType.NORMAL, true);
        }
        else if (elementType == LIST_LITERAL_EXPRESSION && childType == RBRACKET) {
            ASTNode exprList = FormatterUtil.getPreviousNonWhitespaceSibling(child);
            Wrap wrap = null;
            if (exprList != null && exprList.getElementType() == EXPRESSION_LIST) {
                wrap = exprList.getUserData(ObjJ_EXPRESSION_LIST_WRAP_KEY);
                exprList.putUserData(ObjJ_EXPRESSION_LIST_WRAP_KEY, null);
            }
            return wrap != null ? wrap : Wrap.createWrap(WrapType.NORMAL, true);
        }*/

        // Maps in schematic s-expr notation:
        // (MAP_LITERAL_EXPRESSION '{ (MAP_LITERAL_ENTRY expr ': expr) ', (MAP_LITERAL_ENTRY expr ': expr) '})
        /*if (elementType == MAP_LITERAL_EXPRESSION) {
            // First, do persistent object management.
            Wrap wrap = sharedWrap(child, ObjJ_EXPRESSION_LIST_WRAP_KEY);
            // Second, decide what object to return.
            if (childType == LBRACE || childType == LBRACKET) {
                return Wrap.createWrap(WrapType.NONE, false);
            }
            if (childType == MULTI_LINE_COMMENT || childType == CONST) {
                return Wrap.createWrap(WrapType.NONE, false);
            }
            return wrap != null ? wrap : Wrap.createWrap(WrapType.NORMAL, true);
        }*/

        //
        // If
        //
        if (elementType === ObjJ_IF_STATEMENT) {
            if (childType === ObjJ_ELSE) {
                return createWrap(mySettings.ELSE_ON_NEW_LINE)
            } else if (!ObjJTokenSets.BLOCKS.contains(childType) && child === child.treeParent.lastChildNode) {
                return createWrap(true)
            }
        }

        //
        //Binary expressions
        //
        if (ObjJ_EXPR === elementType && mySettings.BINARY_OPERATION_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
            if (mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && childType == ObjJ_EXPR || !mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE && isRightOperand(child)) {
                return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.BINARY_OPERATION_WRAP), true)
            }
        }

        //
        // Assignment
        //
        if (elementType === ObjJ_ASSIGNMENT_EXPR_PRIME && mySettings.ASSIGNMENT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP) {
            if (childType !== ObjJ_ASSIGNMENT_OPERATOR) {
                return if (FormatterUtil.isPrecededBy(child, ObjJ_ASSIGNMENT_OPERATOR) && mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE) {
                    Wrap.createWrap(WrapType.NONE, true)
                } else Wrap.createWrap(WrappingUtil.getWrapType(mySettings.ASSIGNMENT_WRAP), true)
            } else if (mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE) {
                return Wrap.createWrap(WrapType.NORMAL, true)
            }
        }

        //
        // Ternary expressions
        //
        if (elementType === ObjJ_TERNARY_EXPR_PRIME) {
            if (myNode.firstChildNode !== child) {
                if (mySettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE) {
                    if (childType === ObjJ_QUESTION_MARK) {
                        val wrap = Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true)
                        myNode.putUserData(ObjJ_TERNARY_EXPRESSION_WRAP_KEY, wrap)
                        return wrap
                    }

                    if (childType === ObjJ_COLON) {
                        val wrap = myNode.getUserData(ObjJ_TERNARY_EXPRESSION_WRAP_KEY)
                        myNode.putUserData(ObjJ_TERNARY_EXPRESSION_WRAP_KEY, null)
                        return wrap
                                ?: Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true)
                    }
                } else if (childType !== ObjJ_QUESTION_MARK && childType !== ObjJ_COLON) {
                    return Wrap.createWrap(WrappingUtil.getWrapType(mySettings.TERNARY_OPERATION_WRAP), true)
                }
            }
            return Wrap.createWrap(WrapType.NONE, true)
        }

        if (childType === ObjJ_VARIABLE_DECLARATION_LIST && elementType !== ObjJ_FOR_LOOP_PARTS_IN_BRACES) {
            return if (varDeclListContainsVarInit(child)) {
                Wrap.createWrap(WrapType.ALWAYS, true)
            } else {
                Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
            }
        }
        if (childType === ObjJ_VARIABLE_DECLARATION) {
            val parent = parent
            return if (parent != null && parent.elementType === ObjJ_FOR_LOOP_PARTS_IN_BRACES) {
                Wrap.createWrap(WrapType.NORMAL, true)
            } else {
                if (varDeclListContainsVarInit(myNode)) {
                    Wrap.createWrap(WrapType.ALWAYS, true)
                } else {
                    Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
                }
            }
        }

        if (elementType === ObjJ_IMPLEMENTATION_DECLARATION) {
            if (childType === ObjJ_INHERITED_PROTOCOL_LIST) {
                return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
            }
        }
        if (elementType === ObjJ_INHERITED_PROTOCOL_LIST) {
            if (childType === ObjJ_CLASS_NAME) {
                return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
            }
        }

        if (elementType === ObjJ_METHOD_HEADER) {
            if (childType === ObjJ_METHOD_DECLARATION_SELECTOR) {
                return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
            }
        }
        return defaultWrap
    }

    private fun isRightOperand(child: ASTNode): Boolean {
        return myNode.lastChildNode === child
    }

    companion object {

        // Consider using a single key -- the grammar doesn't allow mis-use.
        private val ObjJ_TERNARY_EXPRESSION_WRAP_KEY = Key.create<Wrap>("TERNARY_EXPRESSION_WRAP_KEY")
        private val ObjJ_ARGUMENT_LIST_WRAP_KEY = Key.create<Wrap>("ARGUMENT_LIST_WRAP_KEY")

        private fun createWrap(isNormal: Boolean): Wrap {
            return Wrap.createWrap(if (isNormal) WrapType.NORMAL else WrapType.NONE, true)
        }

        private fun varDeclListContainsVarInit(decl: ASTNode): Boolean {
            if (decl.findChildByType(ObjJ_VAR) != null) return true
            var child: ASTNode? = decl.firstChildNode
            while (child != null) {
                if (child.findChildByType(ObjJ_VAR) != null) return true
                child = child.treeNext
            }
            return false
        }

    }
}