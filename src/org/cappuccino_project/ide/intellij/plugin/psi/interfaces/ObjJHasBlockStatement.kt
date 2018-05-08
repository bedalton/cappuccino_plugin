package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock

import java.util.Collections

interface ObjJHasBlockStatement : ObjJHasBlockStatements {
    val block: ObjJBlock?

    override val blockList: List<ObjJBlock>
        get() = if (block != null) listOf<ObjJBlock>(block) else emptyList()
}
