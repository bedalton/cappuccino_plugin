package cappuccino.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope
import cappuccino.ide.intellij.plugin.psi.utils.getScope
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Level
import java.util.logging.Logger

interface ObjJCompositeElement : PsiElement {

    val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            if (file == null) {
                LOGGER.log(Level.SEVERE, "Cannot get ObjJFile, as containing file is null.")
                return null
            }
            if (file is ObjJFile) {
                return file
            }
            LOGGER.log(Level.SEVERE, "ObjJFile is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
    companion object {
        val LOGGER: Logger by lazy {Logger.getLogger(ObjJCompositeElement::class.java.name)}
    }

    fun <PsiT : PsiElement> getParentOfType(parentClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildOfType(childClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildrenOfType(childClass:Class<PsiT>) : List<PsiT>
    fun commonContext(otherElement:PsiElement) : PsiElement? {
        return PsiTreeUtil.findCommonContext(this, otherElement)
    }

    fun commonScope(otherElement: PsiElement) : ReferencedInScope {
        return getScope(commonContext(otherElement))
    }

}
