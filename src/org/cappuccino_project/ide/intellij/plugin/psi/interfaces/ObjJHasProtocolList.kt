package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInheritedProtocolList

import java.util.ArrayList
import java.util.Collections

interface ObjJHasProtocolList {
    val inheritedProtocolList: ObjJInheritedProtocolList?

    val inheritedProtocols: List<String>
        get() {
            val protocolListElement = inheritedProtocolList ?: return emptyList()
            val inheritedProtocols = ArrayList<String>()
            val protocolClassNameElements = protocolListElement.classNameList
            if (protocolClassNameElements.isEmpty()) {
                return emptyList()
            }
            for (classNameElement in protocolClassNameElements) {
                inheritedProtocols.add(classNameElement.text)
            }
            return inheritedProtocols
        }
}
