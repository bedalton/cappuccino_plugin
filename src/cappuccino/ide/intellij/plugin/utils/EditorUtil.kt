@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package cappuccino.ide.intellij.plugin.utils

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

object EditorUtil {

    fun runWriteAction(writeAction: Runnable, project: Project?) {
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT) }
        } else {
            application.invokeLater { application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT) } }
        }
    }
    fun runWriteAction(writeAction: Runnable, project: Project?, document: Document) {
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document) }
        } else {
            application.invokeLater { application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document) } }
        }
    }

    fun deleteText(document:Document, range: TextRange) {
        com.intellij.openapi.application.runWriteAction {
            document.deleteString(range.startOffset, range.endOffset)
        }
    }

    fun isTextAtOffset(context: InsertionContext, text: String): Boolean {
        if (context.selectionEndOffset == context.document.textLength) {
            return false
        }
        val range = TextRange.create(context.selectionEndOffset, context.selectionEndOffset + text.length)
        return isTextAtOffset(context.document, range, text)
    }

    fun isTextAtOffset(document:Document, startOffset:Int, text: String): Boolean {
        return isTextAtOffset(document, TextRange(startOffset, startOffset + text.length), text)
    }

    fun isTextAtOffset(document:Document, range:TextRange, text: String): Boolean {
        val textAtRange = document.getText(range)
        return textAtRange == text
    }

    fun insertText(insertionContext: InsertionContext, text: String, moveCaretToEnd: Boolean) {
        insertText(insertionContext.editor, text, moveCaretToEnd)
    }

    fun insertText(editor: Editor, text: String, moveCaretToEnd: Boolean) {
        insertText(editor, text, editor.selectionModel.selectionEnd, moveCaretToEnd)
    }

    fun insertText(editor: Editor, text: String, offset: Int, moveCaretToEnd: Boolean) {
        runWriteAction(Runnable {
            editor.document.insertString(offset, text)
            if (moveCaretToEnd) {
                offsetCaret(editor, text.length)
            }
        }, editor.project, editor.document)

    }

    fun insertText(project:Project, document: Document, text: String, offset: Int) {
        runWriteAction(Runnable {
            document.insertString(offset, text)
        }, project, document)
    }

    fun insertText(document: Document, text: String, offset: Int) {
        runWriteAction(Runnable {
            document.insertString(offset, text)
        }, null, document)
    }

    fun offsetCaret(insertionContext: InsertionContext, offset: Int) {
        offsetCaret(insertionContext.editor, offset)
    }

    fun offsetCaret(editor: Editor, offset: Int) {
        editor.caretModel.moveToOffset(editor.caretModel.offset + offset)
    }

    fun document(element: PsiElement) : Document? {
        val containingFile = element.containingFile ?: return null
        val psiDocumentManager = PsiDocumentManager.getInstance(element.project)
        return psiDocumentManager.getDocument(containingFile)
    }

    fun editor(element:PsiElement) : Editor?  {
        val document = document(element) ?: return null
        return EditorFactory.getInstance().getEditors(document, element.project)[0]
    }

    fun tabSize(psiElement: PsiElement) : Int? {
        val editor = psiElement.editor ?: return null
        return tabSize(editor)
    }

    fun tabSize(editor:Editor) : Int? {
        var tabSize: Int? = null
        val commonCodeStyleSettings = CommonCodeStyleSettings(ObjJLanguage.instance)
        val indentOptions = commonCodeStyleSettings.indentOptions

        if (indentOptions != null) {
            tabSize = indentOptions.TAB_SIZE
        }

        if (tabSize == null || tabSize == 0) {
            tabSize = editor.settings.getTabSize(editor.project)
        }
        return tabSize
    }

    fun formatRange(file:PsiFile, textRange: TextRange) {
        CodeStyleManager.getInstance(file.project).reformatTextWithContext(file, listOf(textRange))
    }
}

val PsiElement.document : Document? get() {
    return EditorUtil.document(this)
}

val PsiElement.editor : Editor? get() {
    return EditorUtil.editor(this)
}
