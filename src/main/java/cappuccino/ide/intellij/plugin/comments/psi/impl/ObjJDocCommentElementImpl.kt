package cappuccino.ide.intellij.plugin.comments.psi.impl

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentElement
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil


open class ObjJDocCommentElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ObjJDocCommentElement {

    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            return file as? ObjJFile
        }

    override fun toString(): String {
        return node.elementType.toString()
    }

    override fun <T: PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T: PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T: PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)
}