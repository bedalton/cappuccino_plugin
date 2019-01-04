package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.uast.getContainingClass
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class ObjJSmartEnterProcessor : SmartEnterProcessor() {

    override fun process(
            project: Project,
            editor: Editor,
            psiFile: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        var elementAtCaret:PsiElement = psiFile.findElementAt(offset) ?: return false
        if (elementAtCaret.text.trim().isEmpty()) {
            elementAtCaret = elementAtCaret.getPreviousNonEmptySibling(true) ?: return false
        }
        for (fixer in FIXERS) {
            if (fixer.process(editor, elementAtCaret)) {
                return true
            }
        }
        return false
    }

    companion object {
        private val FIXERS:List<ObjJSmartEnterFixer> = Arrays.asList(
                ClassImplementationEnterHandler()
        )
    }
}


private interface ObjJSmartEnterFixer {
    fun process(editor: Editor, psiElement: PsiElement): Boolean
}

private class ClassImplementationEnterHandler:ObjJSmartEnterFixer {
    override fun process(editor: Editor, psiElement: PsiElement): Boolean {
        val classDeclaration:ObjJClassDeclarationElement<*>? = psiElement as? ObjJClassDeclarationElement<*> ?: psiElement.getParentOfType(ObjJClassDeclarationElement::class.java) ?: return false
        //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "In class declaration")
        val hasEnd:Boolean;
        if (classDeclaration is ObjJImplementationDeclaration) {
            hasEnd = classDeclaration.atEnd != null
        } else if (classDeclaration is ObjJProtocolDeclaration) {
            hasEnd = classDeclaration.atEnd != null
        } else {
            //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration not of expected type")
            return false
        }

        if (!hasEnd) {
            //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration does not have end")
            EditorUtil.insertText(editor, "@end", false)
            EditorUtil.insertText(editor, "\n\n", true)
            EditorUtil.insertText(editor, "\n\n", false)

            return true;
        }
        //Logger.getLogger("ClassImplementationSmartEnterFixer").log(Level.INFO, "Class declaration has @end statement")
        return false
    }

}