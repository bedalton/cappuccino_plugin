package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJBlock

interface ObjJHasBlockStatements : ObjJCompositeElement {
    val blockList: List<ObjJBlock>
}
