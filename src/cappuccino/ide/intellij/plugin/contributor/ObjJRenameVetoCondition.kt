package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.ObjJPropertyName
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

class ObjJRenameVetoCondition : Condition<PsiElement> {
    override fun value(element: PsiElement): Boolean {
        return ObjJPsiFileUtil.isFrameworkElement(element) ||
                element is ObjJSelector || // Prevents renaming of selectors
                element is ObjJPropertyName
    }
}
