package org.cappuccino_project.ide.intellij.plugin.contributor

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.utils.isNewVarDec
import org.cappuccino_project.ide.intellij.plugin.utils.*

import java.util.logging.Logger

class ObjJCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withLanguage(ObjJLanguage.INSTANCE),
                BlanketCompletionProvider())
        //LOGGER.log(Level.INFO, "Creating completion contributor");
    }

    /**
     * Allow autoPopup to appear after custom symbol
     */
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        return position.text.replace(CARET_INDICATOR, "").length > 1 && !isNewVarDec(position)
    }

    override fun handleAutoCompletionPossibility(context: AutoCompletionContext): AutoCompletionDecision? {
        val element = context.parameters.position
        val editor = context.lookup.editor
        if (element is ObjJVariableName || element.parent is ObjJVariableName) {
            if (ObjJVariableInsertHandler.instance.isFunctionCompletion(element)) {
                EditorUtil.insertText(editor, "()", false)
                EditorUtil.offsetCaret(editor, 1)
            }
            if (ObjJVariableInsertHandler.instance.shouldAppendFunctionParamComma(element)) {
                //EditorUtil.insertText(editor, ", ", true);
            }
            if (ObjJVariableInsertHandler.instance.shouldAppendClosingBracket(element)) {
                //EditorUtil.insertText(editor, "]", false);
            }
        }
        return null
    }

    companion object {

        private val LOGGER = Logger.getLogger(ObjJCompletionContributor::class.java.name)
        val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        val TARGETTED_METHOD_SUGGESTION_PRIORITY = 50.0
        val TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY = 10.0
        val FUNCTIONS_IN_FILE_PRIORITY = 5.0
        val GENERIC_METHOD_SUGGESTION_PRIORITY = -10.0
        val GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY = -50.0
        val FUNCTIONS_NOT_IN_FILE_PRIORITY = -70.0
    }

}
