package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.lang.SmartEnterProcessorWithFixers
import com.intellij.lang.SmartEnterProcessorWithFixers.FixEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Smart enter processor
 * @todo fix spacing issue that occurs when used in a class declaration
 */
class ObjJSmartEnterProcessor : SmartEnterProcessorWithFixers() {

    init {
        Logger.getLogger(ObjJSmartEnterProcessor::class.java.simpleName).info("Smart Enter Processing")
        addEnterProcessors(ClassImplementationEnterHandler())
    }
}

private class ClassImplementationEnterHandler: FixEnterProcessor() {
    override fun doEnter(psiElementIn: PsiElement, psiFile: PsiFile?, editor: Editor, afterCompletion: Boolean): Boolean {
        val psiElement = if (psiElementIn.text.trim().isNotEmpty()) psiElementIn else psiElementIn.getPreviousNonEmptySibling(true) ?: return false
        val classDeclaration: ObjJClassDeclarationElement<*>? = psiElement as? ObjJClassDeclarationElement<*>
                ?: psiElement.getParentOfType(ObjJClassDeclarationElement::class.java) ?: return false
        val hasEnd: Boolean
        hasEnd = when (classDeclaration) {
            is ObjJImplementationDeclaration -> classDeclaration.atEnd != null
            is ObjJProtocolDeclaration -> classDeclaration.atEnd != null
            else -> {
                Logger.getLogger("ClassImplementationSmartEnterFixer").severe("Class declaration not of expected type")
                return false
            }
        }

        if (!hasEnd) {
            EditorUtil.insertText(editor, "\n", true)
            EditorUtil.insertText(editor, "\n@end\n\n", false)

            return true
        }
        return false
    }
}