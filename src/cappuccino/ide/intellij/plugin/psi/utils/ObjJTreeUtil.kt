@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList


fun PsiElement?.getChildrenOfType(iElementType: IElementType): List<PsiElement> {
    val out = ArrayList<PsiElement>()
    if (this == null) {
        return out
    }
    for (child in children) {
        //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">, is of type  <"+child.getNode().getElementType().toString()+">");
        if (child.node.elementType === iElementType) {
            //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">is of token type: <"+iElementType.toString()+">");
            out.add(child)
        }
    }
    return out
}

fun PsiElement?.getPreviousSiblingOfType(siblingElementType: IElementType): PsiElement? {
    var element:PsiElement? = this ?: return null
    while (element?.prevSibling != null) {
        element = element.prevSibling
        if (element.hasElementType(siblingElementType)) {
            return element
        }
    }
    return null
}

fun PsiElement?.getNextSiblingOfType(siblingElementType: IElementType): PsiElement? {
    var element:PsiElement? = this ?: return null
    while (element?.nextSibling != null) {
        element = element.nextSibling
        if (element.hasElementType(siblingElementType)) {
            return element
        }
    }
    return null
}

fun <PsiT:PsiElement> PsiElement?.getNextSiblingOfType(siblingClass: Class<PsiT>): PsiElement? {
    var element:PsiElement? = this ?: return null
    while (element?.nextSibling != null) {
        element = element.nextSibling
        if (siblingClass.isInstance(element)) {
            return element
        }
    }
    return null
}

private fun PsiElement?.hasElementType(elementType: IElementType): Boolean {
    return this?.node?.elementType === elementType
}

fun PsiElement.getNextNodeType(): IElementType? {
    return getNextNode()?.elementType
}

fun PsiElement.getNextNode(): ASTNode? {
    return node.treeNext
}

fun PsiElement.getNextNonEmptyNodeType(ignoreLineTerminator: Boolean): IElementType? {
    val next = getNextNonEmptyNode(ignoreLineTerminator)
    return next?.elementType
}

fun PsiElement.getPreviousNonEmptySibling(ignoreLineTerminator: Boolean): PsiElement? {
    val node = getPreviousNonEmptyNode(ignoreLineTerminator)
    return node?.psi
}

fun ASTNode.getPreviousNonEmptySiblingIgnoringComments(): ASTNode? {
    var node = this.getPreviousNonEmptyNode(true)
    while (node != null && (node.text.trim().isEmpty() || node.elementType in ObjJTokenSets.COMMENTS)) {
        node = node.getPreviousNonEmptyNode(true)
    }
    return node
}
fun ASTNode?.getPreviousNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.treePrev ?: return null
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treePrev == null) {
            TreeUtil.prevLeaf(out)
        } else {
            out.treePrev
        }
        if (out == null) {
            return null
        }
        //LOGGER.log(Level.INFO, "<"+compositeElement.getText()+">NextNode "+foldingDescriptors.getText()+" ElementType is <"+foldingDescriptors.getElementType().toString()+">");
    }
    return out
}

fun PsiElement.getNextNonEmptySibling(ignoreLineTerminator: Boolean): PsiElement? {
    val node = getNextNonEmptyNode(ignoreLineTerminator)
    return node?.psi
}

fun PsiElement?.getPreviousNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.node?.treePrev ?: return null
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treePrev == null) {
            TreeUtil.prevLeaf(out)
        } else {
            out.treePrev
        }
    }
    return out
}

fun PsiElement?.getNextNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.node?.treeNext
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treeNext == null) {
            TreeUtil.nextLeaf(out)
        } else {
            out.treeNext
        }
    }
    return out
}



fun <PsiT : PsiElement> PsiElement?.hasSharedContextOfTypeStrict(psiElement2: PsiElement?, sharedClass: Class<PsiT>): Boolean {
    return this?.getSharedContextOfType(psiElement2, sharedClass) != null
}

fun <PsiT : PsiElement> PsiElement?.getSharedContextOfType(psiElement2: PsiElement?, sharedClass: Class<PsiT>): PsiT? {
    if (this == null || psiElement2 == null) {
        return null
    }
    val sharedContext = PsiTreeUtil.findCommonContext(this, psiElement2) ?: return null
    return if (sharedClass.isInstance(sharedContext)) {
        sharedClass.cast(sharedContext)
    } else PsiTreeUtil.getParentOfType(sharedContext, sharedClass)
}

fun <PsiT : PsiElement> PsiElement?.siblingOfTypeOccursAtLeastOnceBefore(siblingElementClass: Class<PsiT>): Boolean {
    var psiElement: PsiElement? = this?.prevSibling ?: return false
    while (psiElement != null) {
        if (siblingElementClass.isInstance(psiElement)) {
            return true
        }
        psiElement = psiElement.prevSibling
    }
    return false
}

fun <StubT : StubElement<*>> filterStubChildren(parent: StubElement<com.intellij.psi.PsiElement>?, stubClass: Class<StubT>): List<StubT> {
    return if (parent == null) {
        emptyList()
    } else filterStubChildren(parent.childrenStubs, stubClass)
}

fun <StubT : StubElement<*>> filterStubChildren(children: List<StubElement<*>>?, stubClass: Class<StubT>): List<StubT> {
    return if (children == null) {
        emptyList()
    } else ArrayUtils.filter(children, stubClass)
}


internal fun shouldSkipNode(out: ASTNode?, ignoreLineTerminator: Boolean): Boolean {
    return out != null && (ignoreLineTerminator && out.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR || out.elementType === com.intellij.psi.TokenType.WHITE_SPACE || out.psi is PsiErrorElement)
}
