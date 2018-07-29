package cappuccino.ide.intellij.plugin.contributor

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.isType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.psi.TokenType
import java.util.logging.Level

import java.util.logging.Logger

class ObjJCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withLanguage(ObjJLanguage.instance),
                BlanketCompletionProvider())
    }

    /**
     * Allow autoPopup to appear after custom symbol
     */
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        return shouldComplete(position.originalElement) && !ObjJVariablePsiUtil.isNewVariableDeclaration(position)
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
                //EditorUtil.insertText(editor, ", ", true)
            }
            if (ObjJVariableInsertHandler.instance.shouldAppendClosingBracket(element)) {
                //EditorUtil.insertText(editor, "]", false)
            }
        }
        return null
    }

    companion object {

        @Suppress("unused")
        private val LOGGER = Logger.getLogger(ObjJCompletionContributor::class.java.name)
        const val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        const val TARGETTED_METHOD_SUGGESTION_PRIORITY = 50.0
        const val TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY = 10.0
        const val FUNCTIONS_IN_FILE_PRIORITY = 5.0
        const val GENERIC_METHOD_SUGGESTION_PRIORITY = -10.0
        const val GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY = -50.0
        const val FUNCTIONS_NOT_IN_FILE_PRIORITY = -70.0



        fun shouldComplete(element: PsiElement?) : Boolean {
            if (element != null && element.text.replace(CARET_INDICATOR, "").trim().isEmpty()) {
                val prevSibling:PsiElement? = when {
                    element.isType(ObjJTypes.ObjJ_SEMI_COLON) -> return false
                    element.prevSibling?.isType(TokenType.WHITE_SPACE) == true -> element.prevSibling.prevSibling
                    element.prevSibling != null -> element.prevSibling
                    else -> return true
                }
                if (prevSibling.isType(ObjJTypes.ObjJ_SEMI_COLON) || prevSibling?.lastChild.isType(ObjJTypes.ObjJ_SEMI_COLON)) {
                    return false
                }
            }
            return true
        }
    }

}
