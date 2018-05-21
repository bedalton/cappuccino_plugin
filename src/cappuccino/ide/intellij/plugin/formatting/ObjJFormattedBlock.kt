package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.ObjJIfStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasBlockStatement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import com.intellij.formatting.*
import com.intellij.formatting.ChildAttributes.DELEGATE_TO_PREV_CHILD
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import java.util.logging.Level
import java.util.logging.Logger

class ObjJFormattedBlock(node:ASTNode, thisAlignment:Alignment?, private val thisWrap:Wrap?,private val thisIndent:Indent?, private val spacingBuilder: SpacingBuilder, private val childAlignment:Alignment? = null,private val childWrap:Wrap? = null) : AbstractBlock(node,thisWrap,thisAlignment) {

    val elementType:IElementType = node.elementType

    override fun buildChildren(): List<Block>? {
        val blocks: MutableList<Block> = mutableListOf()
        val indent:Indent = when (node.psi) {
            is ObjJBlock
                    -> Indent.getNormalIndent(false)
            else -> Indent.getNoneIndent()
        }
        var child:ASTNode? = node.firstChildNode
        while (child != null) {
            val block:Block = createBlock(child, null, null, indent)/*when (elementType) {
                ObjJ_BLOCK -> createBlockStatementBlockChild(child)
                ObjJ_METHOD_HEADER -> createMethodHeaderBlockChild(child)
                else -> when (child.elementType) {
                    ObjJ_METHOD_DECLARATION_SELECTOR,
                    ObjJ_FORMAL_PARAMETER_ARG
                        -> createBlock(child, childAlignment, Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, false), indent)
                    else -> createBlock(child, childAlignment, null, indent)
                }
            }*/
            blocks.add(block)
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent? {
        return thisIndent
    }

    override fun getWrap(): Wrap? {
        return thisWrap
    }

    private fun createBlockStatementBlockChild(node:ASTNode) : Block {
        return when (node.elementType) {
            ObjJ_OPEN_BRACE,
            ObjJ_CLOSE_BRACE -> createBlock(node, null, null, Indent.getNormalIndent(false))
            else ->
                createBlock(node, Alignment.createAlignment(false), null, Indent.getNormalIndent())
        }
    }

    private fun createMethodHeaderBlockChild(node:ASTNode):Block {
        return createBlock(node, null, Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, false), Indent.getNormalIndent())
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (newChildIndex == 0) {
            return DELEGATE_TO_PREV_CHILD
        }
        val nodeElementType = myNode.elementType
        val relativeToDirectParent:Boolean = when (nodeElementType) {
            ObjJ_IF,
            ObjJ_DO,
            ObjJ_WHILE,
            ObjJ_OPEN_BRACE,
            ObjJ_OPEN_BRACKET,
            ObjJ_OPEN_PAREN,
            ObjJ_CLOSE_BRACE,
            ObjJ_CLOSE_BRACKET,
            ObjJ_CLOSE_PAREN -> true
            else -> false
        }
        val indent:Indent = Indent.getNormalIndent(relativeToDirectParent)
        return ChildAttributes(indent, Alignment.createAlignment())
    }

    private fun createBlock(node:ASTNode, alignment:Alignment? = null, wrap:Wrap? = null, indent:Indent? = null, childsChildAlignment:Alignment? = null, childsChildWrap:Wrap? = null) : Block {
        return ObjJFormattedBlock(node, alignment ?: childAlignment, wrap ?: childWrap, indent, spacingBuilder, childsChildAlignment, childsChildWrap)
    }

    override fun getSpacing(
            block: Block?,
            block1: Block): Spacing? {
        return spacingBuilder.getSpacing(this, block, block1)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode != null
    }
}
