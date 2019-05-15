package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import com.intellij.psi.PsiElement

interface JsTypeDefNoNull : JsTypeDefElement

interface JsTypeDefHasNull : JsTypeDefElement {
    val `null` : PsiElement?
}