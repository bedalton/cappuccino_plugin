package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import org.apache.velocity.runtime.parser.node.ASTMap
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger

class ObjJTreeUtil : PsiTreeUtil() {
    companion object {

        private val LOGGER = Logger.getLogger(ObjJTreeUtil::class.java.name)
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

        fun getChildrenOfType(element: PsiElement, iElementType: IElementType): List<PsiElement> {
            val out = ArrayList<PsiElement>()
            for (child in element.children) {
                //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">, is of type  <"+child.getNode().getElementType().toString()+">");
                if (child.node.elementType === iElementType) {
                    //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">is of token type: <"+iElementType.toString()+">");
                    out.add(child)
                }
            }
            return out
        }

        fun getPreviousSiblingOfType(element: PsiElement, siblingElementType: IElementType): PsiElement? {
            var element = element
            while (element.prevSibling != null) {
                element = element.prevSibling
                if (hasElementType(element, siblingElementType)) {
                    return element
                }
            }
            return null
        }

        fun getNextSiblingOfType(element: PsiElement, siblingElementType: IElementType): PsiElement? {
            var element = element
            while (element.nextSibling != null) {
                element = element.nextSibling
                if (hasElementType(element, siblingElementType)) {
                    return element
                }
            }
            return null
        }

        private fun hasElementType(element: PsiElement, elementType: IElementType): Boolean {
            return element.node.elementType === elementType
        }

        fun getNextNodeType(compositeElement: PsiElement): IElementType? {
            val astNode = getNextNode(compositeElement)
            return astNode?.elementType
        }

        fun getNextNode(compositeElement: PsiElement): ASTNode? {
            return compositeElement.node.treeNext
        }

        fun getNextNonEmptyNodeType(compositeElement: PsiElement, ignoreLineTerminator: Boolean): IElementType? {
            val next = getNextNonEmptyNode(compositeElement, ignoreLineTerminator)
            return next?.elementType
        }

        fun getPreviousNonEmptySibling(psiElement: PsiElement, ignoreLineTerminator: Boolean): PsiElement? {
            val node = getPreviousNonEmptyNode(psiElement, ignoreLineTerminator)
            return node?.psi
        }

        fun getNextNonEmptySibling(psiElement: PsiElement, ignoreLineTerminator: Boolean): PsiElement? {
            val node = getNextNonEmptyNode(psiElement, ignoreLineTerminator)
            return node?.psi
        }

        fun getPreviousNonEmptyNode(compositeElement: PsiElement?, ignoreLineTerminator: Boolean): ASTNode? {
            var out: ASTNode? = compositeElement?.node?.treePrev
            while (shouldSkipNode(out, ignoreLineTerminator)) {
                if (out!!.treePrev == null) {
                    out = TreeUtil.prevLeaf(out)
                } else {
                    out = out.treePrev
                }
                if (out == null) {
                    return null
                }
                //LOGGER.log(Level.INFO, "<"+compositeElement.getText()+">NextNode "+out.getText()+" ElementType is <"+out.getElementType().toString()+">");
            }
            return out
        }

        fun getNextNonEmptyNode(compositeElement: PsiElement?, ignoreLineTerminator: Boolean): ASTNode? {
            var out: ASTNode? = compositeElement?.node?.treeNext
            while (shouldSkipNode(out, ignoreLineTerminator)) {
                if (out!!.treeNext == null) {
                    out = TreeUtil.nextLeaf(out)
                } else {
                    out = out.treeNext
                }
                if (out == null) {
                    return null
                }
                //LOGGER.log(Level.INFO, "<"+compositeElement.getText()+">NextNode "+out.getText()+" ElementType is <"+out.getElementType().toString()+">");
            }
            return out
        }

        private fun shouldSkipNode(out: ASTNode?, ignoreLineTerminator: Boolean): Boolean {
            return out != null && (ignoreLineTerminator && out.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR || out.elementType === com.intellij.psi.TokenType.WHITE_SPACE || out.psi is PsiErrorElement)
        }


        fun <PsiT : PsiElement> getSharedContextOfType(psiElement1: PsiElement?, psiElement2: PsiElement?, sharedClass: Class<PsiT>): PsiT? {
            if (psiElement1 == null || psiElement2 == null) {
                return null
            }
            val sharedContext = PsiTreeUtil.findCommonContext(psiElement1, psiElement2) ?: return null
            return if (sharedClass.isInstance(sharedContext)) {
                sharedClass.cast(sharedContext)
            } else PsiTreeUtil.getParentOfType(sharedContext, sharedClass)
        }

        fun <PsiT : PsiElement> hasSharedContextOfType(psiElement1: PsiElement?, psiElement2: PsiElement?, sharedClass: Class<PsiT>): Boolean {
            return getSharedContextOfType(psiElement1, psiElement2, sharedClass) != null
        }

        fun <PsiT : PsiElement> siblingOfTypeOccursAtLeastOnceBefore(psiElement: PsiElement?, siblingElementClass: Class<PsiT>): Boolean {
            var psiElement: PsiElement? = psiElement ?: return false
            while (psiElement!!.prevSibling != null) {
                psiElement = psiElement.prevSibling
                if (siblingElementClass.isInstance(psiElement)) {
                    return true
                }
            }
            return false
        }
    }

}
