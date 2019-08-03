package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.contributor.textWithoutCaret
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement

class JsTypeDefCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withLanguage(JsTypeDefLanguage.instance),
                JsTypeDefCompletionProvider)
    }

    /**
     * Allow autoPopup to appear after custom symbol
     */
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        return position.textWithoutCaret.length > 1
    }

    override fun handleAutoCompletionPossibility(context: AutoCompletionContext): AutoCompletionDecision? {
        return null
    }

    companion object {
        val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        const val JS_CLASS_NAME_COMPLETIONS = 100.0
        const val JS_KEYSET_NAME_COMPLETIONS = 50.0
        const val ObjJ_CLASS_NAME_COMPLETIONS = 10.0
        const val ObjJ_AT_TYPEDEF_NAME_COMPLETIONS = -100.0
    }

}
