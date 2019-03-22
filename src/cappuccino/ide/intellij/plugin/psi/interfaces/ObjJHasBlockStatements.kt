package cappuccino.ide.intellij.plugin.psi.interfaces

interface ObjJHasBlockStatements : ObjJCompositeElement {
    val blockList: List<ObjJBlock>
}
