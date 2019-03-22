package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.utils.Filter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Gets all block children of a type, potentially recursively
 *
 * @param aClass     class of items to filter by
 * @param recursive  whether to check child blocks for matching child elements
 * @param <T>        child element type
 * @return list of child elements matching type
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>, recursive: Boolean): List<T> {
    return getBlockChildrenOfType(aClass, recursive, null, false, -1)
}

/**
 * Gets list of block children of type using a filter
 *
 * @param aClass     class of items to filter by
 * @param recursive  whether to check child blocks for matching child elements
 * @param filter     element filter
 * @param <T>        type of child element to work on
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>, recursive: Boolean,
        filter: Filter<T>): List<T> {
    return getBlockChildrenOfType(aClass, recursive, filter, false, -1)
}

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
private fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>,
        recursive: Boolean,
        filter: Filter<T>?,
        returnFirst: Boolean,
        offset: Int): List<T> {
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
                out.addAll(block.getBlockChildrenOfType(aClass, recursive, filter,returnFirst,offset))

            }
        }
        currentBlocks = nextBlocks
    } while (currentBlocks.isNotEmpty())
    return out
}

private fun getBlocksBlocks(block:ObjJBlock) : List<ObjJBlock> {
    val out = ArrayList<ObjJBlock>()
    for (hasBlockStatements in block.getChildrenOfType(ObjJHasBlockStatements::class.java)) {
        out.addAll(hasBlockStatements.blockList)
    }
    return out
}

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

fun getBlockList(iterationStatement:ObjJIterationStatement): List<ObjJBlock> {
    val block = iterationStatement.block
    return if (block != null) {
        listOf(block)
    } else emptyList()
}

fun getBlockList(expr:ObjJExpr): List<ObjJBlock> {
    val block = getBlock(expr)
    return if (block != null) listOf(block) else listOf()
}


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

fun getBlockList(defineFunction:ObjJPreprocessorDefineFunction) : List<ObjJBlock> {
    return Arrays.asList<ObjJBlock>(defineFunction.block)
}

fun getBlockList(switchStatement:ObjJSwitchStatement) : List<ObjJBlock> {
    val out = ArrayList<ObjJBlock>()
    for (case in switchStatement.caseClauseList) {
        out.addAll(case.blockList)
    }
    return out
}

fun getBlock(expr:ObjJExpr): ObjJBlock? {
    return expr.leftExpr?.functionLiteral?.block
}

fun getBlockList(hasBlockStatements:ObjJHasBlockStatement) : List<ObjJBlock> {
    return hasBlockStatements.getChildrenOfType(ObjJBlock::class.java) as MutableList
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
    val block = getFunctionBlockRange(this)
    if (block != null) {
        return block
    }
    return getMethodBlockRange(this)
}

private fun getFunctionBlockRange(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJFunctionDeclarationElement::class.java)
    return declaration?.block
}

private fun getMethodBlockRange(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJMethodDeclaration::class.java)
    return declaration?.block
}
