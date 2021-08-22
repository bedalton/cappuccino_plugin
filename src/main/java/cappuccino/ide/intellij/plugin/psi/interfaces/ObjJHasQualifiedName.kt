package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement

interface HasQualifiedName<T:ObjJUniversalQualifiedReferenceComponent> : ObjJUniversalPsiElement {
    val qualifiedNameParts:List<T>
    val qualifiedNamePath:List<ObjJQualifiedReferenceComponentPart>
}

interface ObjJHasQualifiedName: HasQualifiedName<ObjJUniversalQualifiedReferenceComponent>