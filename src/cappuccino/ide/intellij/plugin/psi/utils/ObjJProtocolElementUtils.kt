package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import java.util.*

/**
 * Get all inherited protocols for a given class
 */
fun getInheritedProtocols(classDeclaration: ObjJImplementationDeclaration): List<String> {
    val stubProtocols = classDeclaration.stub?.inheritedProtocols
    if (stubProtocols != null) {
        return stubProtocols
    }
    return getProtocolListAsStrings(classDeclaration.inheritedProtocolList)
}

/**
 * Gets inherited protocols for a given protocol
 */
fun getInheritedProtocols(protocolDeclaration: ObjJProtocolDeclaration): List<String> {
    val stubProtocols = protocolDeclaration.stub?.inheritedProtocols
    if (stubProtocols != null) {
        return stubProtocols
    }
    return getProtocolListAsStrings(protocolDeclaration.inheritedProtocolList)

}

private fun getProtocolListAsStrings(protocolListElement: ObjJInheritedProtocolList?): List<String> {
    if (protocolListElement == null) {
        return emptyList()
    }
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