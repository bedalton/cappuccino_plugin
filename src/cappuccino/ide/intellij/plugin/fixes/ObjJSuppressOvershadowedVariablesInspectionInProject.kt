package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import org.jetbrains.annotations.Nls

/**
 * Fix to disable the inspection of overshadowed variables. Perhaps I should change this to a scoped inspection settings
 */
class ObjJSuppressOvershadowedVariablesInspectionInProject : BaseIntentionAction(), LocalQuickFix {
    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        apply()
    }

    override fun getText(): String {
        return ObjJBundle.message("objective-j.intentions.suppress-overshadowed-variable-inspection.text")
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
            project: Project, editor: Editor, psiFile: PsiFile) {
        apply()
    }

    private fun apply() {
        ObjJPluginSettings.ignoreOvershadowedVariables(true)
    }
}
