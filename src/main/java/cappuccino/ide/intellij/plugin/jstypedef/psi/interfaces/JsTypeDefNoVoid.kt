package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import com.intellij.psi.PsiElement

interface JsTypeDefNoVoid : JsTypeDefElement

interface JsTypeDefHasVoid : JsTypeDefElement{
    val void:PsiElement?
}