package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr

interface ObjJFunctionCallElement : ObjJCompositeElement {

    val exprList: List<ObjJExpr>

    val closeParen: PsiElement?

    val openParen: PsiElement
}
