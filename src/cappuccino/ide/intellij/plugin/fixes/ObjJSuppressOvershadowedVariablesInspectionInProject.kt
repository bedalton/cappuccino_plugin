package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.FileContentUtil
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nls

/**
 * Fix to disable the inspection of overshadowed variables. Perhaps I should change this to a scoped inspection settings
 */
class ObjJSuppressOvershadowedVariablesInspectionInProject : BaseIntentionAction(), LocalQuickFix {

    override fun getText(): String {
        return ObjJBundle.message("objective-j.intentions.suppress-overshadowed-variable-inspection.prompt")
    }

    @Nls
    override fun getFamilyName(): String {
        return "Objective-J Annotator Settings"
    }

    override fun isAvailable(
            project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        return !ObjJPluginSettings.ignoreOvershadowedVariables()
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(
            project: Project, editor: Editor, file: PsiFile) {
        apply(file)
    }
    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        apply(problemDescriptor.psiElement.containingFile)
    }


    private fun apply(file:PsiFile) {
        ObjJPluginSettings.ignoreOvershadowedVariables(true)
        FileContentUtil.reparseFiles(file.virtualFile)
    }
}
