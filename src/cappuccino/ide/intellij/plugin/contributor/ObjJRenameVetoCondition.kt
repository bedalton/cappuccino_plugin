package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkFileName
import cappuccino.ide.intellij.plugin.psi.ObjJPropertyName
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

class ObjJRenameVetoCondition : Condition<PsiElement> {
    override fun value(element: PsiElement): Boolean {
        return ObjJPsiFileUtil.isDefinitionElement(element) ||
                //(element is ObjJSelector && !ObjJPluginSettings.experimental_didAskAboutAllowSelectorRename && ObjJPluginSettings.experimental_didAskAboutAllowSelectorRename) || // Prevents renaming of selectors
                element is ObjJPropertyName ||
                (element is ObjJFrameworkFileName && element.reference.multiResolve(false).size < 2)
    }
}
