package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.IncorrectOperationException



class ObjJAddIgnoreVariableNameIntention(private val keyword:String) : BaseIntentionAction(), LocalQuickFix {

    override fun getText(): String {
        return "Add '$keyword' to ignored properties list"
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        apply(project, file)
    }

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        apply(project, problemDescriptor.psiElement.containingFile)
    }

    private fun apply(project: Project, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            ObjJPluginSettings.ignoreVariableName(keyword)
            DaemonCodeAnalyzer.getInstance(project).restart(file)
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}