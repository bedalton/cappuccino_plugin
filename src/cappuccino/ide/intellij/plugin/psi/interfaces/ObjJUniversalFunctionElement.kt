package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.hints.ObjJFunctionDescription
import com.intellij.psi.PsiElement

interface ObjJUniversalFunctionElement : PsiElement {
    val functionNameString:String?
    val description:ObjJFunctionDescription
}