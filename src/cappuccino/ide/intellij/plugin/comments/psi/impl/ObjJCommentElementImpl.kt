package cappuccino.ide.intellij.plugin.comments.psi.impl

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJCommentElement
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.Language


open class ObjJCommentElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ObjJCommentElement {

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
}