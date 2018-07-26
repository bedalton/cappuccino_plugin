package cappuccino.ide.intellij.plugin.fixes

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import org.jetbrains.annotations.Nls

class ObjJIgnoreOvershadowedVariablesInProject() : BaseIntentionAction(), LocalQuickFix {
    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        apply()
    }

    override fun getText(): String {
        return "Disable overshadowed variable inspection in project"
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
