package org.cappuccino_project.ide.intellij.plugin.utils

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

import java.util.logging.Level
import java.util.logging.Logger

object EditorUtil {
    private val LOGGER = Logger.getLogger(EditorUtil::class.java.name)

    fun runWriteAction(writeAction: Runnable, project: Project?, document: Document) {
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document) }
        } else {
            application.invokeLater { application.runWriteAction { CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document) } }
        }
    }

    fun isTextAtOffset(context: InsertionContext, text: String): Boolean {
        val range = TextRange.create(context.selectionEndOffset, context.selectionEndOffset + text.length)
        val textAtRange = context.document.getText(range)
        //LOGGER.log(Level.INFO, "Text at range: <"+textAtRange+">");
        return textAtRange == text
    }

    fun insertText(insertionContext: InsertionContext, text: String, moveCaretToEnd: Boolean) {
        insertText(insertionContext.editor, text, moveCaretToEnd)
    }

    fun insertText(editor: Editor, text: String, moveCaretToEnd: Boolean) {
        insertText(editor, text, editor.selectionModel.selectionEnd, moveCaretToEnd)
    }

    fun insertText(editor: Editor, text: String, offset: Int, moveCaretToEnd: Boolean) {
        runWriteAction({
            editor.document.insertString(offset, text)
            if (moveCaretToEnd) {
                offsetCaret(editor, text.length)
            }
        }, editor.project, editor.document)

    }

    fun offsetCaret(insertionContext: InsertionContext, offset: Int) {
        offsetCaret(insertionContext.editor, offset)
    }

    fun offsetCaret(editor: Editor, offset: Int) {
        editor.caretModel.moveToOffset(editor.caretModel.offset + offset)
    }

}
