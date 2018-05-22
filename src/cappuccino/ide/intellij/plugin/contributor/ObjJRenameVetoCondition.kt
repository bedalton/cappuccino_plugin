package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

class ObjJRenameVetoCondition : Condition<PsiElement> {
    override fun value(element: PsiElement): Boolean {
        return element is ObjJCompositeElement && ObjJFileUtil.isFrameworkElement(element)
    }
}
