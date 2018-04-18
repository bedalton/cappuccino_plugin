package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJCompletionContributor extends CompletionContributor {

    private static final Logger LOGGER = Logger.getLogger(ObjJCompletionContributor.class.getName());
    public static final String CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;
    public static final double TARGETTED_METHOD_SUGGESTION_PRIORITY = 50;
    public static final double TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY = 10;
    public static final double FUNCTIONS_IN_FILE_PRIORITY = 5;
    public static final double GENERIC_METHOD_SUGGESTION_PRIORITY = -10;
    public static final double GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY = -50;
    public static final double FUNCTIONS_NOT_IN_FILE_PRIORITY = -70;

    public ObjJCompletionContributor() {
        extend (CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withLanguage(ObjJLanguage.INSTANCE),
                new BlanketCompletionProvider());
        LOGGER.log(Level.INFO, "Creating completion contributor");
    }
    /**
     * Allow autoPopup to appear after custom symbol
     */
    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return position.getText().replace(CARET_INDICATOR, "").length() > 1 &&
                !ObjJVariablePsiUtil.isNewVarDec(position);
    }

    @Override
    @Nullable
    public AutoCompletionDecision handleAutoCompletionPossibility(@NotNull AutoCompletionContext context) {
        PsiElement element = context.getParameters().getPosition();
        final Editor editor = context.getLookup().getEditor();
        if (element instanceof ObjJVariableName || element.getParent() instanceof ObjJVariableName) {
            if (ObjJVariableInsertHandler.getInstance().isFunctionCompletion(element)) {
                EditorUtil.insertText(editor,"()", false);
                EditorUtil.offsetCaret(editor, 1);
            }
            if (ObjJVariableInsertHandler.getInstance().shouldAppendFunctionParamComma(element)) {
                //EditorUtil.insertText(editor, ", ", true);
            }
            if (ObjJVariableInsertHandler.getInstance().shouldAppendClosingBracket(element)) {
                //EditorUtil.insertText(editor, "]", false);
            }
        }
        return null;
    }

}
