@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import com.intellij.application.options.CodeStyle
import com.intellij.formatting.*
import com.intellij.formatting.ChildAttributes.DELEGATE_TO_PREV_CHILD
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.annotations.NotNull
import com.intellij.formatting.Wrap
import java.util.ArrayList
import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.formatting.templateLanguages.BlockWithParent
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent.getNoneIndent
import com.intellij.formatting.Indent.Type.SPACES
import com.intellij.application.options.CodeStyle.getIndentSize
import com.intellij.psi.impl.source.tree.ChildRole.ARGUMENT_LIST
import com.intellij.formatting.Alignment.createChildAlignment
import com.intellij.formatting.WrapType
import com.intellij.formatting.Wrap.createChildWrap
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.java.JavaSpacePropertyProcessor.getSpacing
import com.intellij.formatting.Spacing
import com.sun.webkit.Timer.getMode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.tree.JavaElementType.RETURN_STATEMENT
import com.intellij.psi.impl.source.tree.JavaElementType.CONTINUE_STATEMENT
import com.intellij.psi.impl.source.tree.JavaElementType.BREAK_STATEMENT
import com.intellij.psi.impl.source.tree.JavaElementType.FOR_STATEMENT
import com.intellij.psi.impl.source.tree.JavaElementType.WHILE_STATEMENT
import com.intellij.psi.impl.source.tree.JavaElementType.IF_STATEMENT

class ObjJFormattedBlock protected constructor(node: ASTNode, wrap: Wrap, alignment: Alignment, private val mySettings: CodeStyleSettings, private val myContext: DartBlockContext) : AbstractBlock(node, wrap, alignment), BlockWithParent {

    private val myIndentProcessor: DartIndentProcessor
    private val mySpacingProcessor: DartSpacingProcessor
    private val myWrappingProcessor: DartWrappingProcessor
    private val myAlignmentProcessor: DartAlignmentProcessor
    private var myChildWrap: Wrap? = null
    private val myIndent: Indent
    private var myParent: BlockWithParent? = null
    private var mySubDartBlocks: MutableList<DartBlock>? = null

    val subDartBlocks: List<DartBlock>?
        get() {
            if (mySubDartBlocks == null) {
                mySubDartBlocks = ArrayList()
                for (block in subBlocks) {
                    mySubDartBlocks!!.add(block as DartBlock)
                }
                mySubDartBlocks = if (!mySubDartBlocks!!.isEmpty()) mySubDartBlocks else DART_EMPTY
            }
            return mySubDartBlocks
        }

    init {
        myIndentProcessor = DartIndentProcessor(myContext.getDartSettings())
        mySpacingProcessor = DartSpacingProcessor(node, myContext.getDartSettings())
        myWrappingProcessor = DartWrappingProcessor(node, myContext.getDartSettings())
        myAlignmentProcessor = DartAlignmentProcessor(node, myContext.getDartSettings())
        myIndent = myIndentProcessor.getChildIndent(myNode, myContext.getMode())
    }

    override fun getIndent(): Indent? {
        return myIndent
    }

    fun getSpacing(child1: Block, child2: Block): Spacing {
        return mySpacingProcessor.getSpacing(child1, child2)
    }

    override fun buildChildren(): List<Block> {
        if (isLeaf) {
            return AbstractBlock.EMPTY
        }
        val tlChildren = ArrayList<Block>()
        var childNode: ASTNode? = node.firstChildNode
        while (childNode != null) {
            if (FormatterUtil.containsWhiteSpacesOnly(childNode)) {
                childNode = childNode.treeNext
                continue
            }
            val childBlock = DartBlock(childNode, createChildWrap(childNode), createChildAlignment(childNode), mySettings, myContext)
            childBlock.setParent(this)
            tlChildren.add(childBlock)
            childNode = childNode.treeNext
        }
        return tlChildren
    }

    fun createChildWrap(child: ASTNode): Wrap {
        val childType = child.elementType
        val wrap = myWrappingProcessor.createChildWrap(child, Wrap.createWrap(WrapType.NONE, false), myChildWrap)

        if (childType === ASSIGNMENT_OPERATOR) {
            myChildWrap = wrap
        }
        return wrap
    }

    @Nullable
    protected fun createChildAlignment(child: ASTNode): Alignment? {
        val type = child.elementType
        return if (type !== LPAREN && !BLOCKS.contains(type)) {
            myAlignmentProcessor.createChildAlignment()
        } else null
    }

    override fun isIncomplete(): Boolean {
        return super.isIncomplete() || myNode.elementType == ARGUMENT_LIST
    }

    override fun getChildAttributes(newIndex: Int): ChildAttributes {
        val elementType = myNode.elementType
        val previousBlock = if (newIndex == 0) null else subDartBlocks!![newIndex - 1]
        val previousType = previousBlock?.node?.elementType

        if (previousType === LBRACE || previousType === LBRACKET) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType === RPAREN && STATEMENTS_WITH_OPTIONAL_BRACES.contains(elementType)) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType === COLON && (elementType === SWITCH_CASE || elementType === DEFAULT_CASE)) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        if (previousType === SWITCH_CASE || previousType === DEFAULT_CASE) {
            if (previousBlock != null) {
                val subBlocks = previousBlock.subDartBlocks
                if (!subBlocks!!.isEmpty()) {
                    val lastChildInPrevBlock = subBlocks[subBlocks.size - 1]
                    val subSubBlocks = lastChildInPrevBlock.subDartBlocks
                    if (isLastTokenInSwitchCase(subSubBlocks!!)) {
                        return ChildAttributes(Indent.getNormalIndent(), null)  // e.g. Enter after BREAK_STATEMENT
                    }
                }
            }

            val indentSize = mySettings.getIndentSize(DartFileType.INSTANCE) * 2
            return ChildAttributes(Indent.getIndent(Indent.Type.SPACES, indentSize, false, false), null)
        }

        if (previousBlock == null) {
            return ChildAttributes(Indent.getNoneIndent(), null)
        }

        if (!previousBlock.isIncomplete && newIndex < subDartBlocks!!.size && previousType !== TokenType.ERROR_ELEMENT) {
            return ChildAttributes(previousBlock.indent, previousBlock.alignment)
        }
        if (myParent is DartBlock && (myParent as DartBlock).isIncomplete) {
            val child = myNode.firstChildNode
            if (child == null || !(child.elementType === OPEN_QUOTE && child.textLength == 3)) {
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
        val DART_EMPTY = Collections.emptyList()

        private val STATEMENTS_WITH_OPTIONAL_BRACES = TokenSet.create(IF_STATEMENT, WHILE_STATEMENT, FOR_STATEMENT)

        private val LAST_TOKENS_IN_SWITCH_CASE = TokenSet.create(BREAK_STATEMENT, CONTINUE_STATEMENT, RETURN_STATEMENT)

        private fun isLastTokenInSwitchCase(blocks: List<DartBlock>): Boolean {
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
            if (type === SEMICOLON && size > 1) {
                val lastBlock2 = blocks[size - 2]
                return lastBlock2.node.elementType === THROW_EXPRESSION
            }
            return false
        }
    }

    companion object {

        private val BLOCK_TYPE_TOKEN_SET = TokenSet.create(
                ObjJ_BLOCK_ELEMENT,
                ObjJ_METHOD_BLOCK,
                ObjJ_IMPLEMENTATION_DECLARATION,
                ObjJ_PROTOCOL_DECLARATION,
                ObjJ_STATEMENT_OR_BLOCK
        )

        private val CHILDREN_SHOULD_OFFSET = TokenSet.create(
                ObjJ_METHOD_CALL,
                ObjJ_METHOD_HEADER,
                ObjJ_EXPR
        )

    }
}