package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJVariableNameByScopeIndex
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.utils.Filter
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

/**
 * Gets all block children of a type
 * Can filter if desired, and can return first matching item as a singleton list
 *
 * @param aClass      class of items to filter by
 * @param recursive   whether to check child blocks for matching child elements
 * @param aFilter      element filter
 * @param returnFirst return first item as singleton list
 * @param <T>         type of child element to work on
 * @return list of items matching class type and filter if applicable.
</T> */
fun <T : PsiElement> ObjJBlock?.getBlockChildrenOfType(
        aClass: Class<T>,
        recursive: Boolean,
        returnFirst: Boolean = false,
        startOffset:Int? = null,
        endOffset: Int? = null,
        aFilter: Filter<T>? = null): List<T> {
    if (this == null) {
        return emptyList()
    }


    val inRange = { child:PsiElement ->
        (endOffset == null || child.textRange.startOffset < endOffset) && (startOffset == null || child.textRange.startOffset > startOffset)
    }
    val children = if (recursive)
        PsiTreeUtil.collectElementsOfType(this, aClass).toList()
    else
        this.getChildrenOfType(aClass)
    if (returnFirst) {
        children.firstOrNull { child -> inRange(child) && aFilter?.let { filter -> filter(child)}.orTrue() }?.let {
            return listOf(it)
        }
    }
    return if (aFilter != null) {
        children.filter {
            inRange(it) && aFilter(it)
        }
    } else {
        children.filter(inRange)
    }
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
    if (aClass == ObjJVariableName::class.java) {
        @Suppress("UNCHECKED_CAST")
        return getParentBlockVariableNameChildren(recursive) as List<T>
    }
    val block: ObjJBlock? = getParentOfType(ObjJBlock::class.java)
            ?: return (this.containingFile as? ObjJFile)?.getFileChildrenOfType(aClass, recursive) ?: return listOf()
    if (recursive) {
        return PsiTreeUtil.collectElementsOfType(block, aClass).toList()
    }
    return block?.getChildrenOfType(aClass)?.toList().orEmpty()
}

private fun PsiElement.getParentBlockVariableNameChildren(recursive: Boolean): List<ObjJVariableName> {

    val file = this.containingFile
    val project = this.project
    val fileName = file.name
    val defaultSearchFunction:(block: ObjJBlock) -> List<ObjJVariableName> = { block ->
        block.getChildrenOfType(ObjJVariableName::class.java)
    }
    val useIndex = !DumbService.isDumb(this.project) && fileName.isNotNullOrBlank()
    val out = if (useIndex)
        getParentBlockVariableNameChildren(recursive) { block ->
            val range = block.textRange
            ObjJVariableNameByScopeIndex.instance.getInRangeStrict(file, range, project)
        }
    else
        getParentBlockVariableNameChildren(recursive, defaultSearchFunction)

    // Check if Index search failed to find results
    return if (useIndex && out.isEmpty())
        getParentBlockVariableNameChildren(recursive, defaultSearchFunction)
    else
        out
}

private fun PsiElement.getParentBlockVariableNameChildren(recursive: Boolean, searchFunction:(block:ObjJBlock) -> List<ObjJVariableName>) : List<ObjJVariableName> {
    var block: ObjJBlock? = getParentOfType(ObjJBlock::class.java) ?: return (this.containingFile as? ObjJFile)?.getFileChildrenOfType(ObjJVariableName::class.java, recursive) ?: return listOf()
    val out = ArrayList<ObjJVariableName>()
    do {
        if (block == null)
            break
        out.addAll(searchFunction(block))
        block = block.getParentOfType(ObjJBlock::class.java)
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
    val out = mutableListOf<ObjJBlock>()
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
    val out = mutableListOf<ObjJBlock>()
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
 * Gets the outer scope for a given element
 * Mostly used to determine whether two variable name elements have the same scope
 * @return scope block for element
 */
fun PsiElement?.getScopeBlockRanges(): List<Pair<Int, Int>> {
    if (this == null) {
        return emptyList()
    }
    val blockRanges = mutableListOf<Pair<Int,Int>>()
    var block = getParentOfType(ObjJFoldable::class.java)
    if (block == null) {
        val fileRange = containingFile.textRange
        return listOf(Pair(fileRange.startOffset, fileRange.endOffset))
    }
    while (block != null) {
        val range = block.textRange
        blockRanges.add(Pair(range.startOffset, range.endOffset))
        block = block.getParentOfType(ObjJFoldable::class.java)
    }
    return blockRanges
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