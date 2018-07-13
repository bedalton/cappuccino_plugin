package cappuccino.ide.intellij.plugin.fixes

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import org.jetbrains.annotations.Nls

class ObjJIgnoreOvershadowedVariablesInProject() : BaseIntentionAction() {

    override fun getText(): String {
        return "ignore overshadowed variables in project"
    }

    @Nls
    override fun getFamilyName(): String {
        return "Objective-J Annotator Settings"
    }

    override fun isAvailable(
            project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(
            project: Project, editor: Editor, psiFile: PsiFile) {
        ObjJPluginSettings.ignoreOvershadowedVariables(true)
    }
}