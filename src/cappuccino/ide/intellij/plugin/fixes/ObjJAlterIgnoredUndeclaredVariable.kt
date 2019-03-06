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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.util.IncorrectOperationException



class ObjJAlterIgnoredUndeclaredVariable(private val keyword:String, val addToIgnored:Boolean) : BaseIntentionAction(), LocalQuickFix {


    override fun getText(): String {
        return if (addToIgnored) "Add variable name \"$keyword\" to globally ignored variables list" else "Remove variable name \"$keyword\" from globally ignored variable names list"
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        invoke(project, FileEditorManager.getInstance(project).selectedTextEditor)
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        invoke(project, editor)
    }

    private fun invoke(project: Project, editor:Editor?) {
        ApplicationManager.getApplication().invokeLater {
            if (addToIgnored) {
                ObjJPluginSettings.ignoreVariableName(keyword)
            } else {
                ObjJPluginSettings.removeIgnoredVariableName(keyword)
            }
            if (editor != null) DaemonCodeAnalyzer.getInstance(project).updateVisibleHighlighters(editor)
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}