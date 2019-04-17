package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.utils.Filter
import com.intellij.openapi.progress.ProgressIndicatorProvider
import java.util.*
import kotlin.collections.ArrayList

/**
 * Gets all block children of a type
 * Can filter if desired, and can return first matching item as a singleton list
 *
 * @param aClass      class of items to filter by
 * @param recursive   whether to check child blocks for matching child elements
 * @param filter      element filter
 * @param returnFirst return first item as singleton list
 * @param <T>         type of child element to work on
 * @return list of items matching class type and filter if applicable.
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>,
        recursive: Boolean,
        returnFirst: Boolean = false,
        offset: Int = -1,
        filter: Filter<T>? = null): List<T> {
    if (this == null) {
        return emptyList()
    }
    val out = ArrayList<T>()

    var currentBlocks: MutableList<ObjJBlock> = ArrayList()
    currentBlocks.add(this)
    currentBlocks.addAll(getBlocksBlocks(this))

    var tempElements: List<T>
    do {
        val nextBlocks = ArrayList<ObjJBlock>()
        //Loop through current level of blocks
        for (block in currentBlocks) {
            if (offset >= 0 && block.textRange.startOffset >= offset) {
                continue
            }
            //Get this blocks children of type
            tempElements = this.getChildrenOfType(aClass)

            //Filter/return/add children
            if (filter != null) {
                for (element in tempElements) {
                    if (filter(element)) {
                        if (returnFirst) {
                            return listOf(element)
                        }
                        if (offset < 0 || element.textRange.startOffset < offset) {
                            out.add(element)
                        }
                    }
                }
            } else if (returnFirst && tempElements.isNotEmpty()) {
                for (element in tempElements) {
                    if (offset < 0 || element.textRange.startOffset > offset) {
                        return listOf(element)
                    }
                }
            } else if (offset < 0) {
                out.addAll(tempElements)
            } else {
                for (element in tempElements) {
                    if (element.textRange.startOffset > offset) {
                        out.add(element)
                    }
                }
            }
            if (!this.isEquivalentTo(block) && recursive) {
                out.addAll(block.getBlockChildrenOfType(aClass, recursive,returnFirst,offset, filter))

            }
        }
        currentBlocks = nextBlocks
    } while (currentBlocks.isNotEmpty())
    return out
}

/**
 * Gets block list within a block
 */
private fun getBlocksBlocks(block:ObjJBlock) : List<ObjJBlock> {
    val out = ArrayList<ObjJBlock>()
    for (hasBlockStatements in block.getChildrenOfType(ObjJHasBlockStatements::class.java)) {
        out.addAll(hasBlockStatements.blockList)
    }
    return out
}

/**
 * Gets the all children matching a type within a psi element's parent block
 */
fun <T : PsiElement> PsiElement.getParentBlockChildrenOfType(aClass: Class<T>, recursive: Boolean): List<T> {
    var block: ObjJBlock? = getParentOfType(ObjJBlock::class.java) ?: return (this.containingFile as? ObjJFile)?.getFileChildrenOfType(aClass, recursive) ?: return listOf()
    val out = ArrayList<T>()
    do {
        if (block != null) {
            out.addAll(block.getChildrenOfType(aClass))
            block = block.getParentOfType(ObjJBlock::class.java)
        }
    } while (block != null && recursive)
    return out
}

/**
 * Gets a block list given an iteration statement
 */
fun getBlockList(iterationStatement:ObjJIterationStatement): List<ObjJBlock> {
    val block = iterationStatement.block
    return if (block != null) {
        listOf(block)
    } else emptyList()
}

/**
 * Gets a block list given an expr element
 */
fun getBlockList(expr:ObjJExpr): List<ObjJBlock> {
    val block = getBlock(expr)
    return if (block != null) listOf(block) else listOf()
}


/**
 * Gets a block list given a try statement
 */
fun getBlockList(tryStatement:ObjJTryStatement): List<ObjJBlock> {
    val out = ArrayList<ObjJBlock>()
    var block: ObjJBlock? = tryStatement.block ?: return out
    out.add(block!!)
    block = tryStatement.catchProduction?.block
    if (block != null) {
        out.add(block)
    }
    block = tryStatement.finallyProduction?.block
    if (block != null) {
        out.add(block)
    }
    return out
}


/**
 * Gets a list of blocks, given a preproc function definition
 */
fun getBlockList(defineFunction:ObjJPreprocessorDefineFunction) : List<ObjJBlock> {
    return Arrays.asList<ObjJBlock>(defineFunction.block)
}


/**
 * Gets a block given an expression
 */
fun getBlock(expr:ObjJExpr): ObjJBlock? {
    return expr.leftExpr?.functionLiteral?.block
}

/**
 * Gets a list of all ObjJBlocks within a given element
 */
fun getBlockList(hasBlockStatements:ObjJHasBlockStatement) : List<ObjJBlock> {
    return hasBlockStatements.getChildrenOfType(ObjJBlock::class.java) as MutableList
}
fun getBlockList(ifStatement: ObjJIfStatement): List<ObjJBlock> {
    val out = java.util.ArrayList<ObjJBlock>()
    out.addAll(ifStatement.getChildrenOfType(ObjJBlock::class.java))
    for (elseIfBlock in ifStatement.elseIfStatementList) {
        ProgressIndicatorProvider.checkCanceled()
        val block = elseIfBlock.block
        if (block != null) {
            out.add(block)
        }
    }
    return out
}

/**
 * Gets a list of blocks given a switch statement
 */
fun getBlockList(switchStatement: ObjJSwitchStatement): List<ObjJBlock> {
    val out = java.util.ArrayList<ObjJBlock>()
    for (clause in switchStatement.caseClauseList) {
        ProgressIndicatorProvider.checkCanceled()
        val block = clause.block ?: continue
        out.add(block)
    }
    return out
}


/**
 * Gets the outer scope for a given element
 * Mostly used to determine whether two variable name elements have the same scope
 * @return scope block for element
 */
fun PsiElement?.getScopeBlock(): ObjJBlock? {
    if (this == null) {
        return null
    }
    val block = getBlockForFunction(this)
    if (block != null) {
        return block
    }
    return getBlockForMethod(this)
}

/**
 * Gets the block for an elements parent function element
 */
private fun getBlockForFunction(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJFunctionDeclarationElement::class.java)
    return declaration?.block
}

/**
 * Gets the block for an elements parent method element
 */
private fun getBlockForMethod(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJMethodDeclaration::class.java)
    return declaration?.block
}


private fun getOpenBraceOrAtOpenBrace(element:ObjJHasBraces) : PsiElement? {
    return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE) ?: element.getChildByType(ObjJTypes.ObjJ_AT_OPEN_BRACE)
}

private fun getOpenBrace(element:ObjJHasBraces) : PsiElement? {
    return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE)
}

private fun getCloseBrace(element:ObjJHasBraces) : PsiElement? {
    return element.getChildByType(ObjJTypes.ObjJ_CLOSE_BRACE)
}