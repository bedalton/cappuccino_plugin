package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasBlockStatements
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.Filter
import com.google.common.collect.Lists
import java.util.*
import java.util.logging.Logger

private val LOGGER = Logger.getLogger("cappuccino.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil")

/**
 * Gets first child of type in block or child blocks, without filter
 *
 * @param firstBlock outermost block to get children from
 * @param aClass     class of items to filter by
 * @param <T>        child element type
 * @return first child of type in block or child blocks
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildOfType(aClass: Class<T>): T? {
    val out = getBlockChildrenOfType(aClass, true, null, true, -1)
    return if (!out.isEmpty()) out[0] else null
}

/**
 * Gets first child of type in block or child blocks, with filter
 *
 * @param firstBlock outermost block to get children from
 * @param aClass     class of items to filter by
 * @param filter     element filter
 * @param <T>        child element type
 * @return first child element matching element class and filter criteria
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildOfType(aClass: Class<T>,
                                                    filter: Filter<T>): T? {
    val out = getBlockChildrenOfType(aClass, true, filter, true, -1)
    return if (!out.isEmpty()) out[0] else null
}

/**
 * Gets all block children of a type, potentially recursively
 *
 * @param firstBlock outermost block to get children from
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
 * Gets all block children of a type, potentially recursively
 *
 * @param firstBlock outermost block to get children from
 * @param aClass     class of items to filter by
 * @param recursive  whether to check child blocks for matching child elements
 * @param <T>        child element type
 * @return list of child elements matching type
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>, recursive: Boolean,
        offset: Int): List<T> {
    return getBlockChildrenOfType(aClass, recursive, null, false, offset)
}

/**
 * Gets list of block children of type using a filter
 *
 * @param firstBlock outermost block to get children from
 * @param aClass     class of items to filter by
 * @param recursive  whether to check child blocks for matching child elements
 * @param filter     element filter
 * @param <T>        type of child element to work on
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>, recursive: Boolean,
        filter: Filter<T>,
        offset: Int): List<T> {
    return getBlockChildrenOfType(aClass, recursive, filter, false, offset)
}

/**
 * Gets list of block children of type using a filter
 *
 * @param firstBlock outermost block to get children from
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
 * @param firstBlock  outermost block to get children from
 * @param aClass      class of items to filter by
 * @param recursive   whether to check child blocks for matching child elements
 * @param filter      element filter
 * @param returnFirst return first item as singleton list
 * @param <T>         type of child element to work on
 * @return list of items matching class type and filter if applicable.
</T> */
private fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>, recursive: Boolean,
        filter: Filter<T>?,
        returnFirst: Boolean,
        offset: Int): List<T> {
    if (this == null) {
        return emptyList()
    }
    var currentBlocks: MutableList<ObjJBlock> = ArrayList()
    currentBlocks.add(this)
    val out = ArrayList<T>()
    var tempElements: List<T>
    do {
        val nextBlocks = ArrayList<ObjJBlock>()
        for (block in currentBlocks) {
            if (offset >= 0 && block.textRange.startOffset >= offset) {
                continue
            }
            tempElements = block.getChildrenOfType(aClass)
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
            } else if (returnFirst && tempElements.size > 0) {
                for (element in tempElements) {
                    if (offset < 0 || element.textRange.startOffset < offset) {
                        return listOf(element)
                    }
                }
            } else {
                for (element in tempElements) {
                    if (offset < 0 || element.textRange.startOffset < offset) {
                        out.add(element)
                    }
                }
            }
            if (recursive) {
                for (hasBlockStatements in block.getChildrenOfType(ObjJHasBlockStatements::class.java)) {
                    //LOGGER.log(Level.INFO, "Looping block recursive with text: <" + hasBlockStatements.getText() + ">");
                    nextBlocks.addAll(hasBlockStatements.blockList)
                }
            }
        }
        currentBlocks = nextBlocks
    } while (!currentBlocks.isEmpty())
    return out
}


fun <T : PsiElement> PsiElement.getParentBlockChildrenOfType(aClass: Class<T>, recursive: Boolean): List<T> {
    var block: ObjJBlock? = getParentOfType(ObjJBlock::class.java) ?: return emptyList()
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

fun getBlock(expr:ObjJExpr): ObjJBlock? {
    return expr.leftExpr?.functionLiteral?.block
}

/**
 * Gets the outer scope for a given element
 * Mostly used to determine whether two variable name elements have the same scope
 * @param psiElement element to find containing scope block for.
 * @return scope block for element
 */
fun PsiElement?.getScopeBlock(): ObjJBlock? {
    if (this == null) {
        return null
    }
    var block = getFunctionBlockRange(this)
    if (block != null) {
        return block
    }
    block = getMethodBlockRange(this)
    return if (block != null) {
        block
    } else null
}

private fun getFunctionBlockRange(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJFunctionDeclarationElement::class.java)
    return declaration?.block
}

private fun getMethodBlockRange(element: PsiElement): ObjJBlock? {
    val declaration = element.getParentOfType(ObjJMethodDeclaration::class.java)
    return declaration?.block
}
