package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.sun.deploy.util.Property.createProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.FileTypeIndex
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.IncorrectOperationException



class ObjJRemoveIgnoredKeywordIntention(private val keyword:String) : BaseIntentionAction() {

    override fun getText(): String {
        return "Remove '${keyword}' from ignored properties list"
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true;
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        ApplicationManager.getApplication().invokeLater {
            ObjJPluginSettings.removeIgnoredKeyword(keyword)
            DaemonCodeAnalyzer.getInstance(project).updateVisibleHighlighters(editor)
        }
    }

    override fun getFamilyName(): String {
       return "Objective-J"
    }

}