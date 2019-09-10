package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageViewUtil
import javax.swing.Icon

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
        val text = UsageViewUtil.createNodeText(this)
        return object : ItemPresentation {
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
    }

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

}