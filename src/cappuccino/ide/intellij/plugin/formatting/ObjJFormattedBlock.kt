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

open class ObjJFormattedBlock(node: ASTNode,
                              wrap: Wrap?,
                              alignment: Alignment?,
                              private val spacingBuilder: SpacingBuilder,
                              private val indent: Indent?,
                              private val childrenWrap: Wrap?,
                              private val childrenAlignment: Alignment?) : AbstractBlock(node, wrap, alignment) {

    val elementType: IElementType = node.elementType

    constructor (node: ASTNode, spacingBuilder: SpacingBuilder) : this(node, null, null, spacingBuilder, null, null, null)


    override fun buildChildren(): MutableList<com.intellij.formatting.Block> {
        return buildChildren(myNode, myWrap, myAlignment, childrenWrap, childrenAlignment)
    }

    private fun buildChild(child: ASTNode): Block {
        return ObjJFormattedBlock(child, spacingBuilder)
    }

    private fun buildChild(child: ASTNode, alignment: Alignment?): Block {
        return ObjJFormattedBlock(child, null, alignment, spacingBuilder, null, null, null)
    }

    private fun buildChild(child: ASTNode, indent: Indent?): Block {
        return ObjJFormattedBlock(child, null, null, spacingBuilder, indent, null, null)
    }

    private fun buildChild(child: ASTNode, wrap: Wrap?): Block {
        return ObjJFormattedBlock(child, wrap, null, spacingBuilder, null, null, null)
    }

    private fun buildChild(child: ASTNode, wrap: Wrap?, alignment: Alignment?): Block {
        return ObjJFormattedBlock(child, wrap, alignment, spacingBuilder, null, null, null)
    }

    private fun buildChild(child: ASTNode,
                           wrap: Wrap?,
                           alignment: Alignment?,
                           childrenAlignment: Alignment?): Block {
        return ObjJFormattedBlock(child, wrap, alignment, spacingBuilder, null, null, childrenAlignment)
    }

    private fun buildChild(child: ASTNode, wrap: Wrap?, indent: Indent?): Block {
        return ObjJFormattedBlock(child, wrap, null, spacingBuilder, indent, null, null)
    }

    private fun buildChild(child: ASTNode, alignment: Alignment?, indent: Indent?): Block {
        return ObjJFormattedBlock(child, null, alignment, spacingBuilder, indent, null, null)
    }

    private fun buildChild(child: ASTNode,
                           wrap: Wrap?,
                           alignment: Alignment?,
                           indent: Indent?): Block {
        return ObjJFormattedBlock(child, wrap, alignment, spacingBuilder, indent, null, null)
    }

    private fun buildChild(child: ASTNode,
                           wrap: Wrap?,
                           indent: Indent?,
                           childrenWrap: Wrap?): Block {
        return ObjJFormattedBlock(
                child,
                wrap,
                null,
                spacingBuilder,
                indent,
                childrenWrap, null
        )
    }

    private fun buildChildren(
            parent: ASTNode,
            parentWrap: Wrap?,
            parentAlignment: Alignment?,
            childrenWrap: Wrap?,
            childrenAlignment: Alignment?): MutableList<com.intellij.formatting.Block> {
        val parentElementType: IElementType = parent.elementType
        return when (parentElementType) {
            ObjJ_METHOD_CALL -> {
                return buildMethodCallChildren(parent)
            }
            in BLOCK_TYPE_TOKEN_SET -> {
                return buildBlockChildren(parent, parentAlignment)
            }
            else -> {
                val commaWrap = Wrap.createChildWrap(childrenWrap, WrapType.NONE, true)

                /* all children need a shared alignment, so that the second child doesn't have an automatic continuation
                   indent */
                var finalChildrenAlignment: Alignment? = if (BLOCK_TYPE_TOKEN_SET.contains(parentElementType)) Alignment.createChildAlignment(alignment) else childrenAlignment
                if (finalChildrenAlignment == null) {
                    finalChildrenAlignment = Alignment.createChildAlignment(parentAlignment)
                }
                return buildChildren(parent) { child: ASTNode, childElementType: IElementType, blockList: MutableList<Block> ->
                    if (childElementType == ObjJ_COMMA) {
                        blockList.add(buildChild(child, commaWrap))
                    } else {
                        blockList.add(buildChild(child, childrenWrap, finalChildrenAlignment))
                    }
                    blockList
                }
            }
        }
    }

    private fun buildBlockChildren(block:ASTNode, alignment:Alignment?) : MutableList<Block> {
        val childrenWrap = Wrap.createWrap(WrapType.ALWAYS, true)
        return buildChildren(block) { child: ASTNode, childElementType: IElementType, blockList: MutableList<Block> ->
            if (childElementType in BLOCK_TYPE_TOKEN_SET) {
                blockList.add(buildChild(child, childrenWrap, alignment))
            }
            blockList
        }
    }

    private fun buildMethodCallChildren(methodCall: ASTNode): MutableList<Block> {
        return buildChildren(methodCall) { child: ASTNode, childElementType: IElementType, blockList: MutableList<Block> ->
            if (childElementType == ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
                blockList.addAll(buildSelectorChildren(child, Indent.getContinuationIndent(true)))
            }
            blockList.add(buildChild(child))
            blockList
        }
    }

    private fun buildSelectorChildren(selector: ASTNode, indent:Indent): MutableList<Block> {
        val selectorWrap = Wrap.createWrap(WrapType.NORMAL, true)
        val selectorColonWrap = Wrap.createChildWrap(selectorWrap, WrapType.NONE, true)
        val selectorValueWrap = Wrap.createWrap(WrapType.NORMAL, true)

        return buildChildren(selector) { child: ASTNode, childElementType: IElementType, blockList: MutableList<Block> ->
            when (childElementType) {
                ObjJ_SELECTOR -> blockList.add(buildChild(child, selectorWrap, null, indent))
                ObjJ_COLON -> blockList.add(buildChild(child, selectorColonWrap))
                else -> blockList.add(buildChild(child, selectorValueWrap, null, Indent.getContinuationIndent(false)))
            }
            blockList
        }
    }

    override fun getIndent(): Indent? {
        return indent
    }

    override fun getWrap(): Wrap? {
        return myWrap
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (newChildIndex == 0) {
            return DELEGATE_TO_PREV_CHILD
        }
        val nodeElementType = myNode.elementType
        val indent:Indent = when (nodeElementType) {
            in BLOCK_TYPE_TOKEN_SET -> Indent.getNormalIndent(true)
            else -> return super.getChildAttributes(newChildIndex)
        }
        return ChildAttributes(indent, this.getFirstChildAlignment())
    }

    private fun getFirstChildAlignment(): Alignment? {
        val subBlocks = this.subBlocks
        val iterator = subBlocks.iterator()

        var alignment: Alignment?
        do {
            if (!iterator.hasNext()) {
                return null
            }

            val subBlock = iterator.next() as Block
            alignment = subBlock.alignment
        } while (alignment == null)

        return alignment
    }

    override fun getSpacing(
            block: Block?,
            block1: Block): Spacing? {
        return spacingBuilder.getSpacing(this, block, block1)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode != null
    }

    companion object {

        private val BLOCK_TYPE_TOKEN_SET = TokenSet.create(
            ObjJ_BLOCK_ELEMENT,
            ObjJ_METHOD_BLOCK,
            ObjJ_IMPLEMENTATION_DECLARATION,
            ObjJ_PROTOCOL_DECLARATION,
            ObjJ_STATEMENT_OR_BLOCK
        )

        private val WHITESPACE_TOKEN_SET = TokenSet.create(TokenType.WHITE_SPACE, ObjJ_LINE_TERMINATOR)

        fun buildChildren(
                node: ASTNode,
                reduce: BlockListReducer): MutableList<com.intellij.formatting.Block> {
            var blockList: MutableList<com.intellij.formatting.Block> = ArrayList()

            var child: ASTNode? = node.firstChildNode

            while (child != null) {
                if (shouldBuildBlock(child)) {
                    blockList = reduce(child, child.elementType, blockList)
                }

                child = child.treeNext
            }

            return blockList
        }

        private fun shouldBuildBlock(@NotNull childElementType: IElementType): Boolean {
            return !WHITESPACE_TOKEN_SET.contains(childElementType)
        }

        private fun shouldBuildBlock(@NotNull child: ASTNode): Boolean {
            return shouldBuildBlock(child.elementType) && child.textLength > 0
        }
        private fun codeStyleSettings(node: ASTNode): ObjJCodeStyleSettings {
            return CodeStyle
                    .getCustomSettings(node.psi.containingFile, ObjJCodeStyleSettings::class.java)
        }

        private fun commonCodeStyleSettings(node: ASTNode): CommonCodeStyleSettings {
            return CodeStyle
                    .getLanguageSettings(node.psi.containingFile, ObjJLanguage.instance)
        }

    }
}

typealias BlockListReducer = (child: ASTNode,
               childElementType: IElementType,
               blockList: MutableList<com.intellij.formatting.Block>) -> MutableList<com.intellij.formatting.Block>

typealias ContainerBlockListReducer = (
            child: ASTNode,
            childElementType: IElementType,
            tailWrap: Wrap,
            childrenIndent: Indent,
            blockList: MutableList<com.intellij.formatting.Block>
    ) -> MutableList<com.intellij.formatting.Block>
