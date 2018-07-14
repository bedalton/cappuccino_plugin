package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsHolder
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.IncorrectOperationException



class ObjJAddIgnoreVariableNameIntention(private val keyword:String) : BaseIntentionAction() {

    override fun getText(): String {
        return "Add '${keyword}' to ignored properties list"
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true;
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            ObjJPluginSettingsHolder.addIgnoredVariableNameToList(keyword);
            DaemonCodeAnalyzer.getInstance(project).updateVisibleHighlighters(editor)
        }
    }

    override fun getFamilyName(): String {
       return "Objective-J"
    }

}