package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock

import java.util.Collections

interface ObjJHasBlockStatement : ObjJHasBlockStatements {
    val block: ObjJBlock?
}
