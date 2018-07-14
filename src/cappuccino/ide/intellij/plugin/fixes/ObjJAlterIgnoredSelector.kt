package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsHolder
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.IncorrectOperationException



class ObjJAlterIgnoredSelector(private val keyword:String, val addToIgnored:Boolean) : BaseIntentionAction() {

    override fun getText(): String {
        return if (addToIgnored) "Add selector <$keyword> to ignored selectors list" else "Remove selector <${keyword}> from ignored selectors list"
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true;
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            if (addToIgnored) {
                ObjJPluginSettingsHolder.addIgnoredSelectorToList(keyword)
            } else {
                ObjJPluginSettingsHolder.removeIgnoredSelectorFromList(keyword)
            }
            DaemonCodeAnalyzer.getInstance(project).updateVisibleHighlighters(editor)
        }
    }

    override fun getFamilyName(): String {
       return "Objective-J"
    }

}