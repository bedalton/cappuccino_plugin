package cappuccino.ide.intellij.plugin.contributor

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil
import cappuccino.ide.intellij.plugin.utils.*

import java.util.logging.Logger

class ObjJCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withLanguage(ObjJLanguage.instance),
                ObjJBlanketCompletionProvider)
    }

    /**
     * Allow autoPopup to appear after custom symbol
     */
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        return position.text.replace(CARET_INDICATOR, "").trim().length > 2 &&
                position !is ObjJBlock &&
                !ObjJVariablePsiUtil.isNewVarDec(position) &&
                !(position.text.contains("{") || position.text.contains("}")) &&
                !position.text.matches("^[0-9]".toRegex()) &&
                !position.text.contains(";") && position.prevSibling?.text?.endsWith(";")  == false
    }

    override fun handleAutoCompletionPossibility(context: AutoCompletionContext): AutoCompletionDecision? {
        val element = context.parameters.position
        val editor = context.lookup.editor
        if (element is ObjJVariableName || element.parent is ObjJVariableName) {
            if (ObjJVariableInsertHandler.isFunctionCompletion(element)) {
                EditorUtil.insertText(editor, "()", false)
                EditorUtil.offsetCaret(editor, 1)
            }
            if (ObjJVariableInsertHandler.shouldAppendFunctionParamComma(element)) {
                //EditorUtil.insertText(editor, ", ", true);
            }
            if (ObjJVariableInsertHandler.shouldAppendClosingBracket(element)) {
                //EditorUtil.insertText(editor, "]", false);
            }
        }
        return null
    }

    companion object {

        private val LOGGER = Logger.getLogger(ObjJCompletionContributor::class.java.name)
        val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        val TARGETTED_METHOD_SUGGESTION_PRIORITY = 50.0
        val TARGETTED_SUPERCLASS_METHOD_SUGGESTION_PRIORITY = 30.0
        val TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY = 10.0
        val TARGETTED_VARIABLE_SUGGESTION_PRIORITY = 15.0
        val FUNCTIONS_IN_FILE_PRIORITY = 5.0
        val GENERIC_METHOD_SUGGESTION_PRIORITY = -10.0
        val GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY = -50.0
        val GENERIC_VARIABLE_SUGGESTION_PRIORITY = -40.0
        val FUNCTIONS_NOT_IN_FILE_PRIORITY = -70.0
        val TYPEDEF_PRIORITY = -300.0
        val FUNCTION_SHOULD_SKIP = -120.0
    }

}
