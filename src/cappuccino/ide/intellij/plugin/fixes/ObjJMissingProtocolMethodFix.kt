package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import java.util.logging.Logger
import javax.swing.SwingUtilities

class ObjJMissingProtocolMethodFix(private val declaration:ObjJImplementationDeclaration, private val protocolName:String, private val methodHeaders: ProtocolMethods) : BaseIntentionAction() {

    override fun getText(): String {
        return "Implement missing protocol methodHeaders"
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

    override fun isAvailable(
            project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(
            project: Project, editor: Editor, psiFile: PsiFile) {
        try {
            addMethods(project, editor.document)
        } catch (e:Exception) {
            SwingUtilities.invokeLater {
                Messages.showDialog(project, MESSAGE.format(protocolName, "An unexpected error occurred"), TITLE, listOf("OK").toTypedArray(), 0, Messages.getWarningIcon())
            }
            Logger.getLogger(ObjJMissingProtocolMethodFix::class.java.canonicalName).severe(MESSAGE.format(protocolName, "An unexpected error occurred") + "; Error: "+e.message)
        }
        DaemonCodeAnalyzer.getInstance(declaration.project).restart(declaration.containingFile)
    }

    private fun addMethods(project: Project, document:Document) {
        if (methodHeaders.required.isEmpty()) {
            return
        }
        val requiredHeaders = methodHeaders.required;
        val sibling:PsiElement? = when {
            declaration.atEnd != null -> {
                declaration.atEnd!!
            }
            declaration.lastChild != null -> {
                declaration.lastChild
            }
            else -> {
                declaration.node.treeNext?.psi
            }
        }
        if (sibling == null) {
            SwingUtilities.invokeLater {
                Messages.showDialog(project, MESSAGE.format(protocolName, "Implementation declaration is not well formed"), TITLE, listOf("OK").toTypedArray(), 0, Messages.getWarningIcon())
            }
            return
        }
        val numberOfRequiredNewLinesBeforeMethods = numberOfReturnsNeeded(sibling, true, 2)
        var newLinesBefore = ""
        for (i in 0 until numberOfRequiredNewLinesBeforeMethods) {
            newLinesBefore += "\n"
        }
        val methodsText = ObjJElementFactory.createMethodDeclarationsText(requiredHeaders)
        document.insertString(sibling.textRange.startOffset, newLinesBefore + methodsText + "\n\n")
    }

    companion object {
        const val TITLE = "Protocol Implementation Failure"
        const val MESSAGE:String = "Failed to create placeholder methods for protocol %s. %s"

        private fun numberOfReturnsNeeded(element:PsiElement, before:Boolean, required:Int) : Int {
            var remaining = required;
            var prevSibling:PsiElement = element.prevSibling ?: return required
            for (i in 0 until required) {
                if (prevSibling.text?.endsWith("\n") == true) {
                    remaining -= 1;
                }
                prevSibling = prevSibling.prevSibling ?: return remaining
            }
            return remaining
        }
    }
}
