package cappuccino.ide.intellij.plugin.jstypedef.fixes

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNodeIgnoringComments
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNodeIgnoringComments
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

class RemoveElementFix(element:PsiElement, private val message:String) : BaseIntentionAction() {

    private val element: SmartPsiElementPointer<PsiElement> = SmartPointerManager.createPointer(element)

    override fun getText(): String {
        return message
    }

    override fun getFamilyName(): String {
        return JsTypeDefBundle.message("JsTypeDef-quick-fix.family-name")
    }

    override fun isAvailable(p0: Project, p1: Editor?, p2: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val elementNode = element.element?.node ?: return
        val parentNode = elementNode.treeParent ?: return
        parentNode.removeChild(elementNode)
    }
}