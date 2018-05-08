package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock

interface ObjJHasBlockStatements : ObjJCompositeElement {
    val blockList: List<ObjJBlock>
}
