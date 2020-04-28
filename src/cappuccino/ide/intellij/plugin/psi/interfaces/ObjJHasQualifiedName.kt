package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement

interface ObjJHasQualifiedName : ObjJUniversalPsiElement {
    val qualifiedNameParts:List<ObjJQualifiedReferenceComponent>
    val qualifiedNamePath:List<ObjJQualifiedReferenceComponentPart>
}