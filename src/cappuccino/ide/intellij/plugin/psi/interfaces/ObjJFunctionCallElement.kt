package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import com.intellij.psi.PsiElement

interface ObjJFunctionCallElement : ObjJCompositeElement {

    val exprList: List<ObjJExpr>

    val closeParen: PsiElement?

    val openParen: PsiElement
}
