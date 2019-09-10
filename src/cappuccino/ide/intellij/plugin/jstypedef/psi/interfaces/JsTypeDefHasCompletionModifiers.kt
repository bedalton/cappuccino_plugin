package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
import com.intellij.psi.PsiElement

interface JsTypeDefHasCompletionModifiers : JsTypeDefElement {
    val atQuiet: PsiElement?
    val atSilent: PsiElement?
    val atSuggest: PsiElement?
    val completionModifier:CompletionModifier
}