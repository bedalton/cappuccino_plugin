package cappuccino.ide.intellij.plugin.psi.interfaces

interface ObjJQualifiedReferenceComponent : ObjJCompositeElement {
    val qualifiedNameText: String?
    val indexInQualifiedReference:Int
}
