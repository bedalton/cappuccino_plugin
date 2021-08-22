package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement


interface ObjJUniversalQualifiedReferenceComponent : ObjJUniversalPsiElement {
    val indexInQualifiedReference:Int
}

interface ObjJQualifiedReferenceComponent : ObjJUniversalQualifiedReferenceComponent, ObjJCompositeElement

val ObjJUniversalQualifiedReferenceComponent.previousSiblings: List<ObjJUniversalQualifiedReferenceComponent> get() {
    val components = (parent as? HasQualifiedName<*>)?.qualifiedNameParts
            ?: return emptyList()
    val index = indexInQualifiedReference
    if (index < components.size && index > 0)
        return components.subList(0, indexInQualifiedReference)
    return emptyList()
}
