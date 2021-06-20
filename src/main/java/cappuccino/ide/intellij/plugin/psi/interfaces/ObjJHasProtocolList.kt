package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList

interface ObjJHasProtocolList {
    val inheritedProtocolList: ObjJInheritedProtocolList?

    fun getInheritedProtocols(): List<String>
}
