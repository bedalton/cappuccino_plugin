@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.document
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger


internal val LOGGER:Logger by lazy {
    Logger.getLogger("#ObjJTreeUtil")
}


fun PsiElement?.getChildrenOfType(iElementType: IElementType): List<PsiElement> {
    val out:MutableList<PsiElement> = mutableListOf()
    if (this == null) {
        return out
    }
    for (child in children) {
        ////LOGGER.info("Child element <"+child.getText()+">, is of type  <"+child.getNode().getElementType().toString()+">");
        if (child.node.elementType === iElementType) {
            ////LOGGER.info("Child element <"+child.getText()+">is of token type: <"+iElementType.toString()+">");
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

fun ASTNode.getNextNonEmptyNodeIgnoringComments(): ASTNode? {
    var node = this.getNextNonEmptyNode(true)
    while (node != null && (node.text.isBlank() || node.elementType in ObjJTokenSets.COMMENTS)) {
        node = node.getNextNonEmptyNode(true)
    }
    return node
}


fun PsiElement.getPreviousNonEmptySibling(ignoreLineTerminator: Boolean): PsiElement? {
    val node = getPreviousNonEmptyNode(ignoreLineTerminator)
    return node?.psi
}

fun ASTNode.getPreviousNonEmptyNodeIgnoringComments(): ASTNode? {
    var node = this.getPreviousNonEmptyNode(true)
    while (node != null && (node.text.trim().isEmpty() || node.elementType in ObjJTokenSets.COMMENTS)) {
        node = node.getPreviousNonEmptyNode(true)
    }
    return node
}

fun ASTNode?.getPreviousPossiblyEmptySibling(): ASTNode? {
    return this?.treePrev ?: getPrevInTreeParent(this)
}

fun ASTNode?.getPreviousNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.treePrev ?: getPrevInTreeParent(this) ?: return null
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treePrev == null) {
            getPrevInTreeParent(out)
        } else {
            out.treePrev
        }
        if (out == null) {
            return null
        }
        ////LOGGER.info("<"+compositeElement.getText()+">NextNode "+foldingDescriptors.getText()+" ElementType is <"+foldingDescriptors.getElementType().toString()+">");
    }
    return out
}


val PsiElement.previous: PsiElement? get(){
    val out: ASTNode? = this.node.treePrev ?: getPrevInTreeParent(this.node) ?: return null
    return out?.psi
}

val PsiElement.next: PsiElement? get() {
    val out: ASTNode? = this.node.treeNext ?: getNextInTreeParent(this.node) ?: return null
    return out?.psi
}

private fun getPrevInTreeParent(out:ASTNode?): ASTNode? {
    var temp:ASTNode? = out?.treeParent ?: return null
    while (temp != null && temp.treePrev == null && temp.treeParent != null) {
        temp = temp.treeParent
    }
    return temp?.treePrev
}



fun ASTNode?.getNextPossiblyEmptySibling(): ASTNode? {
    return this?.treeNext ?: getNextInTreeParent(this)
}

fun ASTNode.getNextNonEmptySiblingIgnoringComments(): ASTNode? {
    var node = this.getNextNonEmptyNode(true)
    while (node != null && (node.text.trim().isEmpty() || node.elementType in ObjJTokenSets.COMMENTS)) {
        node = node.getNextNonEmptyNode(true)
    }
    return node
}

fun ASTNode?.getNextNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.treeNext ?: getNextInTreeParent(this) ?: return null
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treeNext == null) {
            getNextInTreeParent(out)
        } else {
            out.treeNext
        }
        if (out == null) {
            return null
        }
    }
    return out
}

private fun getNextInTreeParent(out:ASTNode?): ASTNode? {
    var temp:ASTNode? = out?.treeParent ?: return null
    while (temp != null && temp.treeNext == null && temp.treeParent != null) {
        temp = temp.treeParent
    }
    return temp?.treeNext
}

fun PsiElement.getNextNonEmptySibling(ignoreLineTerminator: Boolean): PsiElement? {
    val node = getNextNonEmptyNode(ignoreLineTerminator)
    return node?.psi
}

fun PsiElement.getNextNonEmptySiblingIgnoringComments(): PsiElement? {
    val node = node.getNextNonEmptyNodeIgnoringComments()
    return node?.psi
}

fun PsiElement?.getPreviousNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    return this?.node?.getPreviousNonEmptyNode(ignoreLineTerminator)
}

fun PsiElement?.getNextNonEmptyNode(ignoreLineTerminator: Boolean): ASTNode? {
    var out: ASTNode? = this?.node?.treeNext
    while (out != null && shouldSkipNode(out, ignoreLineTerminator)) {
        out = if (out.treeNext == null) {
            out.treeParent.treeNext
        } else {
            out.treeNext
        }
    }
    return out
}

fun PsiElement.distanceFromStartOfLine() : Int? {
    val document = this.document?: return null
    return distanceFromStartOfLine(document)
}

fun PsiElement.distanceFromStartOfLine(editor:Editor) : Int {
    return this.distanceFromStartOfLine(editor.document)
}

fun PsiElement.distanceFromStartOfLine(document:Document) : Int {
    val elementStartOffset = this.textRange.startOffset
    val elementLineNumber = document.getLineNumber(elementStartOffset)
    val elementLineStartOffset = document.getLineStartOffset(elementLineNumber)
    //val elementLineStartOffset = StringUtil.lastIndexOf(document.text, '\n', 0, elementStartOffset)
    return elementStartOffset - elementLineStartOffset
}

val PsiElement.lineNumber:Int? get() {
    val elementStartOffset = this.textRange.startOffset
    return document?.getLineNumber(elementStartOffset)
}


fun ASTNode.isDirectlyPrecededByNewline(): Boolean {
    var node: ASTNode? = this.treePrev ?: getPrevInTreeParent(this) ?: return false
    while (node != null) {
        if (node.elementType == ObjJTypes.ObjJ_LINE_TERMINATOR)
            return true
        if (node.elementType == TokenType.WHITE_SPACE) {
            if (node.text.contains("\n"))
                return true
            node = node.treePrev ?: getPrevInTreeParent(node)
            continue
        }
        break
    }
    return false
}

fun ASTNode.getPrevSiblingOnTheSameLineSkipCommentsAndWhitespace(): ASTNode? {
    var node: ASTNode? = this.treePrev ?: getPrevInTreeParent(this)
    while (node != null) {
        return if (node.elementType == TokenType.WHITE_SPACE || ObjJTokenSets.COMMENTS.contains(node.elementType)) {
            if (node.text.contains("\n")) {
                null
            } else {
                node = if (node.treePrev != null) node.treePrev else getPrevInTreeParent(node)
                continue
            }
        } else node
    }

    return null
}

fun <PsiT: PsiElement> PsiElement?.thisOrParentAs(psiClass:Class<PsiT>) : PsiT? {
    return if (psiClass.isInstance(this)) {
        psiClass.cast(this)
    } else {
        this.getParentOfType(psiClass)
    }
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

fun <StubT : StubElement<*>> filterStubChildren(parent: StubElement<PsiElement>?, stubClass: Class<StubT>): List<StubT> {
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
    if (out == null) {
        return false
    }
    return if (ignoreLineTerminator && out.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR)
        true
    else if (!ignoreLineTerminator && out.text.contains("\n")) {
        false
    } else {
        out.elementType === TokenType.WHITE_SPACE || out.psi is PsiErrorElement
    }
}
