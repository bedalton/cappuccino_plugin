@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.psi.ObjJArguments
import cappuccino.ide.intellij.plugin.psi.ObjJIfStatement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet
import com.intellij.formatting.Wrap
import java.util.ArrayList
import com.intellij.formatting.templateLanguages.BlockWithParent
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.WrapType
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import java.util.logging.Logger

class ObjJFormattedBlock internal constructor(node: ASTNode, wrap: Wrap?, alignment: Alignment?, private val mySettings: CodeStyleSettings, private val myContext: ObjJBlockContext) : AbstractBlock(node, wrap, alignment), BlockWithParent {
    private val myIndentProcessor: ObjJIndentProcessor
    private val mySpacingProcessor: ObjJSpacingProcessor
    private val myWrappingProcessor: ObjJWrappingProcessor
    private val myAlignmentProcessor: ObjJAlignmentProcessor
    private var myChildWrap: Wrap? = null
    private val myIndent: Indent?
    private var myParent: BlockWithParent? = null
    private var mySubObjJFormattedBlocks: MutableList<ObjJFormattedBlock>? = null
    private val _empty = mutableListOf<ObjJFormattedBlock>()

    private val subObjJFormattedBlocks: List<ObjJFormattedBlock>?
        get() {
            if (mySubObjJFormattedBlocks == null) {
                mySubObjJFormattedBlocks = ArrayList()
                for (block in subBlocks) {
                    mySubObjJFormattedBlocks!!.add(block as ObjJFormattedBlock)
                }
                mySubObjJFormattedBlocks = if (!mySubObjJFormattedBlocks!!.isEmpty()) mySubObjJFormattedBlocks else _empty
            }
            return mySubObjJFormattedBlocks
        }

    init {
        @Suppress("DEPRECATION")
        val objjSettings = CodeStyleSettingsManager.getSettings(node.psi.project).getCustomSettings(ObjJCodeStyleSettings::class.java)
        myIndentProcessor = ObjJIndentProcessor(myContext.objJSettings, objjSettings)
        mySpacingProcessor = ObjJSpacingProcessor(node, myContext.objJSettings, objjSettings)
        myWrappingProcessor = ObjJWrappingProcessor(node, myContext.objJSettings)
        myAlignmentProcessor = ObjJAlignmentProcessor(node, myContext.objJSettings, objjSettings)
        myIndent = myIndentProcessor.getChildIndent(myNode)
    }

    override fun getIndent(): Indent? {
        return myIndent
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return mySpacingProcessor.getSpacing(child1, child2) ?: Spacing.getReadOnlySpacing()
    }

    override fun buildChildren(): List<Block> {
        if (isLeaf) {
            return AbstractBlock.EMPTY
        }
        val children = ArrayList<Block>()
        var childNode: ASTNode? = node.firstChildNode
        while (childNode != null) {
            if (FormatterUtil.containsWhiteSpacesOnly(childNode)) {
                childNode = childNode.treeNext
                continue
            }
            val childBlock = ObjJFormattedBlock(childNode, createChildWrap(childNode), createChildAlignment(childNode), mySettings, myContext)
            childBlock.setParent(this)
            children.add(childBlock)
            childNode = childNode.treeNext
        }
        return children
    }

    private fun createChildWrap(child: ASTNode): Wrap {
        val childType = child.elementType
        val wrap = myWrappingProcessor.createChildWrap(child, Wrap.createWrap(WrapType.NONE, false), myChildWrap)

        if (childType in ObjJTokenSets.ASSIGNMENT_OPERATORS) {
            myChildWrap = wrap
        }
        return wrap
    }

    private fun createChildAlignment(child: ASTNode): Alignment? {
        return myAlignmentProcessor.createChildAlignment(child)
    }

    override fun isIncomplete(): Boolean {
        return when (node.elementType) {
            ObjJ_IF_STATEMENT -> (node.psi as? ObjJIfStatement)?.blockList?.isEmpty() ?: return false
            ObjJ_ARGUMENTS -> (node.psi as? ObjJArguments)?.closeParen == null
            else -> super.isIncomplete()
        }
    }

    override fun getChildAttributes(newIndex: Int): ChildAttributes {
        val elementType = myNode.elementType
        val parentType = myNode.treeParent?.elementType
        val previousBlock = if (newIndex == 0) null else subObjJFormattedBlocks!![newIndex - 1]
        val previousType = previousBlock?.node?.elementType

        //return ChildAttributes(myIndentProcessor.getChildIndent(myNode), null)

        //LOGGER.info("Child Attribute for: $elementType in $parentType")

        if (previousType === ObjJ_OPEN_BRACE || previousType === ObjJ_OPEN_BRACKET) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType == TokenType.ERROR_ELEMENT) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (previousType == ObjJ_FOR_LOOP_HEADER || previousType == ObjJ_CONDITION_EXPRESSION) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType in ObjJTokenSets.INDENT_CHILDREN) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType == ObjJStubTypes.FILE) {
            return ChildAttributes(Indent.getNoneIndent(), null)
        }

        if (previousType == ObjJ_OPEN_BRACE) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType == ObjJ_INSTANCE_VARIABLE_DECLARATION) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType == ObjJ_QUALIFIED_REFERENCE || parentType == ObjJ_QUALIFIED_REFERENCE || previousType == ObjJ_BODY_VARIABLE_ASSIGNMENT) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }


        if (parentType == ObjJ_INHERITED_PROTOCOL_LIST || elementType == ObjJ_INHERITED_PROTOCOL_LIST) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType == ObjJTokenSets.CLASS_DECLARATIONS) {
            if (previousType == ObjJ_COMMA || previousType == ObjJ_CLASS_NAME || previousType == ObjJ_LESS_THAN) {
                return ChildAttributes(Indent.getNormalIndent(), null)
            }
            return ChildAttributes(Indent.getNoneIndent(), null)
        }

        if (parentType == ObjJ_METHOD_HEADER) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType == ObjJ_INSTANCE_VARIABLE_LIST) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType == ObjJ_INSTANCE_VARIABLE_DECLARATION) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (parentType == ObjJ_ARGUMENTS ||
                parentType == ObjJ_ACCESSOR || elementType == ObjJ_ACCESSOR ||
                elementType == ObjJ_ACCESSOR_PROPERTY || parentType == ObjJ_ACCESSOR_PROPERTY) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (parentType == ObjJ_QUALIFIED_REFERENCE || parentType == ObjJ_QUALIFIED_REFERENCE) {
            if (myNode.treePrev != null) {
                return ChildAttributes(Indent.getContinuationIndent(), null)
            }
        }

        if (parentType == ObjJ_RIGHT_EXPR) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (elementType == ObjJ_METHOD_CALL) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (previousType === ObjJ_CLOSE_PAREN && STATEMENTS_WITH_OPTIONAL_BRACES.contains(elementType)) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (elementType == ObjJ_IF_STATEMENT) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType === ObjJ_COLON && (elementType === ObjJ_CASE_CLAUSE || elementType === ObjJ_DEFAULT_CLAUSE)) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (parentType == ObjJ_METHOD_CALL) {
            return if (elementType == ObjJ_COLON) {
                ChildAttributes(Indent.getContinuationIndent(), null)
            } else if (elementType == ObjJ_OPEN_BRACKET || elementType == ObjJ_CLOSE_BRACKET) {
                ChildAttributes(Indent.getNoneIndent(), null)
            } else {
                ChildAttributes(Indent.getNormalIndent(), null)
            }
        }

        if (previousType == ObjJ_CALL_TARGET || previousType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType === ObjJ_CASE_CLAUSE || previousType === ObjJ_DEFAULT_CLAUSE) {
            if (previousBlock != null) {
                val subBlocks = previousBlock.subObjJFormattedBlocks
                if (!subBlocks!!.isEmpty()) {
                    val lastChildInPrevBlock = subBlocks[subBlocks.size - 1]
                    val subSubBlocks = lastChildInPrevBlock.subObjJFormattedBlocks
                    if (isLastTokenInSwitchCase(subSubBlocks!!)) {
                        return ChildAttributes(Indent.getNormalIndent(), null)  // e.g. Enter after BREAK_STATEMENT
                    }
                }
            }

            val indentSize = mySettings.getIndentSize(ObjJFileType.INSTANCE) * 2
            return ChildAttributes(Indent.getIndent(Indent.Type.SPACES, indentSize, false, false), null)
        }

        if (previousBlock == null) {
            return ChildAttributes(Indent.getNoneIndent(), null)
        }

        if (previousType == ObjJ_OPEN_BRACE || previousType == ObjJ_OPEN_PAREN || previousType == ObjJ_OPEN_BRACKET) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (elementType == ObjJ_ACCESSOR_PROPERTY) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (elementType == ObjJ_ACCESSOR_PROPERTY_TYPE) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (elementType == ObjJ_EQUALS) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (elementType == ObjJ_IN && parentType == ObjJ_IN_EXPR) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType == ObjJ_COMMA) {
            return ChildAttributes(previousBlock.indent, previousBlock.alignment)
        }


        if (!previousBlock.isIncomplete && newIndex < subObjJFormattedBlocks!!.size && previousType !== TokenType.ERROR_ELEMENT) {
            return ChildAttributes(previousBlock.indent, previousBlock.alignment)
        }

        if (previousType in ObjJTokenSets.ALL_OPERATORS) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        if (myParent is ObjJFormattedBlock && (myParent as ObjJFormattedBlock).isIncomplete) {
            val child = myNode.firstChildNode
            if (child == null || !((child.elementType === ObjJ_DOUBLE_QUO || child.elementType === ObjJ_SINGLE_QUO) && child.textLength == 3)) {
                return ChildAttributes(Indent.getContinuationIndent(), null)
            }
        }

        return if (myParent == null && isIncomplete) {
            ChildAttributes(Indent.getContinuationIndent(), null)
        } else ChildAttributes(previousBlock.indent, previousBlock.alignment)
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getParent(): BlockWithParent? {
        return myParent
    }

    override fun setParent(newParent: BlockWithParent) {
        myParent = newParent
    }

    companion object {
        val OBJJ_EMPTY:List<ObjJFormattedBlock> = emptyList()

        private val STATEMENTS_WITH_OPTIONAL_BRACES = TokenSet.create(ObjJ_IF_STATEMENT, ObjJ_WHILE_STATEMENT, ObjJ_FOR_STATEMENT)

        private val LAST_TOKENS_IN_SWITCH_CASE = TokenSet.create(ObjJ_BREAK_STATEMENT, ObjJ_CONTINUE, ObjJ_RETURN_STATEMENT)

        private fun isLastTokenInSwitchCase(blocks: List<ObjJFormattedBlock>): Boolean {
            val size = blocks.size
            // No blocks.
            if (size == 0) {
                return false
            }
            // [return x;]
            val lastBlock = blocks[size - 1]
            val type = lastBlock.node.elementType
            if (LAST_TOKENS_IN_SWITCH_CASE.contains(type)) {
                return true
            }
            // [throw expr][;]
            if (type === ObjJ_SEMI_COLON && size > 1) {
                val lastBlock2 = blocks[size - 2]
                return lastBlock2.node.elementType === ObjJ_THROW_STATEMENT
            }
            return false
        }
        val LOGGER:Logger = Logger.getLogger(ObjJFormattedBlock::class.java.canonicalName)
    }
}