package org.cappuccino_project.ide.intellij.plugin.utils;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EditorUtil {
    private static final Logger LOGGER = Logger.getLogger(EditorUtil.class.getName());

    public static void runWriteAction(@NotNull final Runnable writeAction, Project project, Document document) {
        final Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            application.runWriteAction(() -> CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document));
        }
        else {
            application.invokeLater(() -> application.runWriteAction(() -> CommandProcessor.getInstance().executeCommand(project, writeAction, null, null, UndoConfirmationPolicy.DEFAULT, document)));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isTextAtOffset(InsertionContext context, @NotNull
            String text) {
        TextRange range = TextRange.create(context.getSelectionEndOffset(), context.getSelectionEndOffset()+text.length());
        String textAtRange = context.getDocument().getText(range);
        //LOGGER.log(Level.INFO, "Text at range: <"+textAtRange+">");
        return textAtRange.equals(text);
    }

    public static void insertText(InsertionContext insertionContext, @NotNull String text, boolean moveCaretToEnd) {
        insertText(insertionContext.getEditor(), text, moveCaretToEnd);
    }

    public static void insertText(Editor editor, @NotNull String text, boolean moveCaretToEnd) {
        insertText(editor, text, editor.getSelectionModel().getSelectionEnd(), moveCaretToEnd);
    }

    public static void insertText(Editor editor, String text, int offset, boolean moveCaretToEnd) {
        runWriteAction( () -> {
            editor.getDocument().insertString(offset, text);
            if (moveCaretToEnd) {
                offsetCaret(editor, text.length());
            }
        }, editor.getProject(), editor.getDocument());

    }

    public static void offsetCaret(InsertionContext insertionContext, int offset) {
        offsetCaret(insertionContext.getEditor(), offset);
    }

    public static void offsetCaret(Editor editor, int offset) {
        editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset()+offset);
    }

}
