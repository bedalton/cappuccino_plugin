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
import com.intellij.util.FileContentUtil
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException

/**
 * Adds or Removes a function name to a project level list of function names that are always defined as existing
 * Function with this name are ignored though-out the whole project, not just the current file
 * To ignore the function name in a given scope, it should use inspection suppression
 */
class ObjJAlterIgnoredFunctionNames(private val keyword:String, val addToIgnored:Boolean) : BaseIntentionAction(), LocalQuickFix {


    override fun getText(): String {
        return if (addToIgnored)
            ObjJBundle.message("objective-j.intentions.alter-ignored-function-name.add-ignore.prompt", keyword)
         else
            ObjJBundle.message("objective-j.intentions.alter-ignored-function-name.remove-ignored.prompt", keyword)
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        invoke(descriptor.psiElement.containingFile)
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        invoke(file)
    }

    private fun invoke(file:PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            if (addToIgnored) {
                ObjJPluginSettings.ignoreFunctionName(keyword)
            } else {
                ObjJPluginSettings.removeIgnoredFunctionName(keyword)
            }
            FileContentUtil.reparseFiles(file.virtualFile)
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}