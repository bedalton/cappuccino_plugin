package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReferencePrime

interface ObjJQualifiedReferenceComponent : ObjJCompositeElement {
    val indexInQualifiedReference:Int
}

val ObjJQualifiedReferenceComponent.previousSiblings:List<ObjJQualifiedReferenceComponent> get() {
    val components = (parent as? ObjJQualifiedReferencePrime)?.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java)
            ?: getParentOfType(ObjJQualifiedReference::class.java)?.qualifiedNameParts
            ?: return emptyList()
    val index = indexInQualifiedReference
    if (index < components.size && index > 0)
        return components.subList(0, indexInQualifiedReference)
    return emptyList()
}
