package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle.message
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.utils.EditorUtil.insertText
import cappuccino.ide.intellij.plugin.utils.editor
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

class ObjJAddSemiColonIntention(element: ObjJNeedsSemiColon) : IntentionAction, LocalQuickFix {
    private val element: SmartPsiElementPointer<ObjJNeedsSemiColon>
    override fun startInWriteAction(): Boolean = true
    override fun getText(): String = message("objective-j.intentions.add-semi-colon.prompt")
    override fun getName(): String = message("objective-j.intentions.add-semi-colon.prompt")
    override fun getFamilyName(): String = ObjJInspectionProvider.GROUP_DISPLAY_NAME

    override fun isAvailable(
        project: Project, editor: Editor?, psiFile: PsiFile?
    ): Boolean {
        val element = element.element ?: return false
        return element.lastChild.text != ";"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
            ?: return
        val editor = element.editor
            ?: return
        invoke(editor, element)
    }

    override fun invoke(
        project: Project, editor: Editor, psiFile: PsiFile?
    ) {
        val element = element.element ?: return
        invoke(editor, element)
    }

    private fun invoke(editor: Editor, element: PsiElement) {
        insertText(editor, ";", element.textRange.endOffset, true)
    }

    init {
        this.element = SmartPointerManager.createPointer(element)
    }
}