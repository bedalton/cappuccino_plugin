package cappuccino.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJExpr

interface ObjJFunctionCallElement : ObjJCompositeElement {

    val exprList: List<ObjJExpr>

    val closeParen: PsiElement?

    val openParen: PsiElement
}
