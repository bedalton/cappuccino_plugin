package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException

/**
 * Adds or Removes a function name to a project level list of function names that are always defined as existing
 * Function with this name are ignored though-out the whole project, not just the current file
 * To ignore the function name in a given scope, it should use inspection suppression
 */
class ObjJAlterIgnoredClassNames(private val className:String, val addToIgnored:Boolean) : BaseIntentionAction(), LocalQuickFix {


    override fun getText(): String {
        return if (addToIgnored)
            ObjJBundle.message("objective-j.inspections.class-type-inspection.fix.ignore-class.prompt", className)
         else
            ObjJBundle.message("objective-j.inspections.class-type-inspection.fix.remove-ignored.prompt", className)
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        invoke()
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        invoke()
    }

    private fun invoke() {
        ApplicationManager.getApplication().invokeLater {
            if (addToIgnored) {
                ObjJPluginSettings.ignoreClassName(className)
            } else {
                ObjJPluginSettings.removeIgnoredClassName(className)
            }
            FileContentUtilCore.reparseFiles()
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}