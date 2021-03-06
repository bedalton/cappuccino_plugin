package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException

/**
 * Adds or Removes a selector from a static list in the settings.
 * Selectors in this list are ignored throughout the project
 */
class ObjJAlterIgnoredSelector(private val keyword:String, private val addToIgnored:Boolean) : BaseIntentionAction() {

    override fun getText(): String {
        return if (addToIgnored)
            ObjJBundle.message("objective-j.intentions.alter-ignored-selector.add-ignore-selector.prompt", keyword)
        else
            ObjJBundle.message("objective-j.intentions.alter-ignored-selector.remove-ignored-selector.prompt", keyword)
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            if (addToIgnored) {
                ObjJPluginSettings.ignoreSelector(keyword)
            } else {
                ObjJPluginSettings.doNotIgnoreSelector(keyword)
            }

            FileContentUtilCore.reparseFiles(file.virtualFile)
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}