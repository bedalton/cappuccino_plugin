package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement

interface ObjJHasFunctionName: ObjJUniversalPsiElement {
    val functionNameString: String

    val functionNameNode: ObjJNamedElement?
}
