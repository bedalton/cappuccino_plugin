package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReferencePrime
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import com.intellij.psi.PsiNamedElement

interface ObjJQualifiedReferenceComponent : ObjJUniversalPsiElement {
    val indexInQualifiedReference:Int
}



val ObjJQualifiedReferenceComponent.previousSiblings:List<ObjJQualifiedReferenceComponent> get() {
    val components = (parent as? ObjJHasQualifiedName)?.qualifiedNameParts
            ?: return emptyList()
    val index = indexInQualifiedReference
    if (index < components.size && index > 0)
        return components.subList(0, indexInQualifiedReference)
    return emptyList()
}
