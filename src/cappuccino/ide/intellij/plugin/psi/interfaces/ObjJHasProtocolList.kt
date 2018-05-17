package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList

import java.util.ArrayList
import java.util.Collections

interface ObjJHasProtocolList {
    val inheritedProtocolList: ObjJInheritedProtocolList?

    fun getInheritedProtocols(): List<String>
}
