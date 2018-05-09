package org.cappuccino_project.ide.intellij.plugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.usageView.UsageViewUtil
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement

import javax.swing.*

open class ObjJCompositeElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ObjJCompositeElement {

    override val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            return file as? ObjJFile
        }

    override fun toString(): String {
        return node.elementType.toString()
    }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        return processDeclarations(this, processor, state, lastParent, place)
    }

    private fun processDeclarations(
            element: PsiElement,
            processor: PsiScopeProcessor,
            state: ResolveState, lastParent: PsiElement?,
            place: PsiElement): Boolean {
        return processor.execute(element, state) && ObjJResolveUtil.processChildren(element, processor, state, lastParent, place)
    }

    override fun getPresentation(): ItemPresentation? {
        //LOGGER.log(Level.INFO, "Get Presentation <"+this.getNode().getElementType().toString()+">");
        val text = UsageViewUtil.createNodeText(this)
        return if (text != null) {
            object : ItemPresentation {
                override fun getPresentableText(): String {
                    return text
                }

                override fun getLocationString(): String {
                    return containingFile.name
                }

                override fun getIcon(b: Boolean): Icon? {
                    return this@ObjJCompositeElementImpl.getIcon(0)
                }
            }
        } else super.getPresentation()
    }

}