package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.lang.SmartEnterProcessorWithFixers
import com.intellij.lang.SmartEnterProcessorWithFixers.FixEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.uast.getContainingClass
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class ObjJSmartEnterProcessor : SmartEnterProcessorWithFixers() {

    init {
        Logger.getLogger(ObjJSmartEnterProcessor::class.java.simpleName).info("Smart Enter Procressing")
        addEnterProcessors(ClassImplementationEnterHandler())
    }
}

private class ClassImplementationEnterHandler: FixEnterProcessor() {
    override fun doEnter(psiElementIn: PsiElement, psiFile: PsiFile?, editor: Editor, afterCompletion: Boolean): Boolean {
        val psiElement = if (psiElementIn.text.trim().isNotEmpty()) psiElementIn else psiElementIn.getPreviousNonEmptySibling(true) ?: return false
        val classDeclaration: ObjJClassDeclarationElement<*>? = psiElement as? ObjJClassDeclarationElement<*>
                ?: psiElement.getParentOfType(ObjJClassDeclarationElement::class.java) ?: return {
                    Logger.getLogger("ClassImplementationSmartEnterFixer").info("Element not in class declaration")
                    false
                }()
        //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "In class declaration")
        val hasEnd: Boolean
        if (classDeclaration is ObjJImplementationDeclaration) {
            hasEnd = classDeclaration.atEnd != null
        } else if (classDeclaration is ObjJProtocolDeclaration) {
            hasEnd = classDeclaration.atEnd != null
        } else {
            Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration not of expected type")
            return false
        }

        if (!hasEnd) {
            Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration does not have end")
            EditorUtil.insertText(editor, "\n", true)
            EditorUtil.insertText(editor, "\n@end\n\n", false)

            return true;
        }
        //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration has @end statement")
        return false
    }
}