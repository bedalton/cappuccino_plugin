package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import com.intellij.psi.PsiElement

interface JsTypeDefNotInInterface : JsTypeDefElement

interface JsTypeDefInInterface : JsTypeDefElement {
    val nullType : PsiElement?
}