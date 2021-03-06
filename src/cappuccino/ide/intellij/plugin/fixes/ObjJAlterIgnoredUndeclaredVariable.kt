package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException

/**
 * Adds or Removes a variable name to a project level list of variable names that are always defined as existing
 * Variables with this name are ignored thoughout the whole project, not just the current file
 * To ignore the variable name in a given scope, it should use inspection suppression
 */
class ObjJAlterIgnoredUndeclaredVariable(private val keyword:String, val addToIgnored:Boolean, private val message:String? = null) : BaseIntentionAction(), LocalQuickFix {


    override fun getText(): String {
        return message ?: if (addToIgnored)
            ObjJBundle.message("objective-j.intentions.alter-ignored-variable.add-ignore-var.prompt", keyword)
         else
            ObjJBundle.message("objective-j.intentions.alter-ignored-variable.remove-ignored-var.text", keyword)
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
                ObjJPluginSettings.ignoreVariableName(keyword)
            } else {
                ObjJPluginSettings.removeIgnoredVariableName(keyword)
            }
            FileContentUtilCore.reparseFiles(file.virtualFile)
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}