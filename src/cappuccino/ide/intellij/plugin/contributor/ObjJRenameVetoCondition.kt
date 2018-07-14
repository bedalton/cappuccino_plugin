package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsHolder
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

class ObjJRenameVetoCondition : Condition<PsiElement> {
    override fun value(element: PsiElement): Boolean {
        return ObjJFileUtil.isFrameworkElement(element) ||
                (element is ObjJSelector && !ObjJPluginSettingsHolder.selectorRenameEnabled())
    }
}
