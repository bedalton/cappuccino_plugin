package cappuccino.ide.intellij.plugin.jstypedef.fixes

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNodeIgnoringComments
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNodeIgnoringComments
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

/**
 * Removes an element from a list, removing pipes if necessary
 */
class RemoveElementInPipedListFix(element:PsiElement, private val message:String) : BaseIntentionAction() {

    private val element: SmartPsiElementPointer<PsiElement> = SmartPointerManager.createPointer(element)

    override fun getText(): String {
        return message
    }

    override fun getFamilyName(): String {
        return JsTypeDefBundle.message("jstypedef-quick-fix.family-name")
    }

    override fun isAvailable(p0: Project, p1: Editor?, p2: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {

        val parentNode = element.element?.parent?.node ?: return
        removePipe(parentNode)
        val elementNode = element.element?.node ?: return
        parentNode.removeChild(elementNode)
    }

    private fun removePipe(parentNode:ASTNode) {
        val elementNode = element.element?.node ?: return

        // Check PipeNext
        val pipeNext = elementNode.getNextNonEmptyNodeIgnoringComments()
        if (pipeNext != null && pipeNext.elementType == JsTypeDefTypes.JS_PIPE) {
            parentNode.removeChild(pipeNext)
            return
        }
        // Check Pipe Prev if nextElement was not pipe
        val pipePrev = elementNode.getPreviousNonEmptyNodeIgnoringComments()
        if (pipePrev != null && pipePrev.elementType == JsTypeDefTypes.JS_PIPE) {
            parentNode.removeChild(pipePrev)
        }
    }
}