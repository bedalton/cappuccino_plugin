package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import java.util.*


/**
 * Gets method headers for a class declaration
 */
fun getAllMethodHeaders(declaration: ObjJImplementationDeclaration): List<ObjJMethodHeader> {
    val headers = ArrayList<ObjJMethodHeader>()
    for (methodHeaderDeclaration in declaration.methodDeclarationList) {
        headers.add(methodHeaderDeclaration.methodHeader)
    }
    return headers
}

/**
 * Gets method headers for a given protocol element
 */
fun getAllMethodHeaders(declaration: ObjJProtocolDeclaration): List<ObjJMethodHeader> {
    val headers = declaration.methodHeaderList.toMutableList()
    for (scopedBlock in declaration.protocolScopedMethodBlockList) {
        headers.addAll(scopedBlock.methodHeaderList)
    }
    return headers
}

/**
 * Determines whether a protocol contains a given method
 */
fun hasMethod(declaration: ObjJProtocolDeclaration, selector: String): Boolean {
    return selector in declaration.getAllSelectors(true)
}

/**
 * Determines whether this implementation class has a given selector
 */
fun hasMethod(implementationDeclaration: ObjJImplementationDeclaration, selector: String): Boolean {
    if (selector in implementationDeclaration.getAllSelectors(true)) {
        return true
    }
    val instanceVariableDeclarationList = implementationDeclaration.instanceVariableList?.instanceVariableDeclarationList
            ?: return false
    for (instanceVariableDeclaration in instanceVariableDeclarationList) {
        if (instanceVariableDeclaration.accessor == null) {
            continue
        }
        if (instanceVariableDeclaration.variableName != null && selector == instanceVariableDeclaration.variableName!!.text) {
            return true
        }
        if (instanceVariableDeclaration.accessorPropertyList.isEmpty()) {
            if (selector.startsWith("set") && selector.substring(2) == instanceVariableDeclaration.variableName!!.text) {
                return true
            }
        }
        for (accessorProperty in instanceVariableDeclaration.accessorPropertyList) {
            if (selector == accessorProperty.selectorString || selector.startsWith("set") && selector.substring(2) == accessorProperty.selectorString) {
                return true
            }
        }
    }
    return false
}

/**
 * Gets all unimplemented protocol getMethods
 */
fun getAllUnimplementedProtocolMethods(@Suppress("UNUSED_PARAMETER") declaration: ObjJImplementationDeclaration): Map<ObjJClassName, ObjJProtocolDeclarationPsiUtil.ProtocolMethods> {
    //todo
    return emptyMap()
}

/**
 * Gets unimplemented protocols for an @implementation declaration for a given protocol name
 */
fun getUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolName: String): ObjJProtocolDeclarationPsiUtil.ProtocolMethods {
    val project = declaration.project
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }

    val thisClassName = declaration.classNameString
    val required = ArrayList<ObjJMethodHeader>()
    val optional = ArrayList<ObjJMethodHeader>()
    val inheritedProtocols = ObjJInheritanceUtil.appendAllInheritedProtocolsToSet(protocolName, project)
    for (protocolDeclaration in inheritedProtocols) {
        addUnimplementedProtocolMethods(project, thisClassName, protocolDeclaration.methodHeaderList, required)
        for (scopedBlock in protocolDeclaration.protocolScopedMethodBlockList) {
            ProgressIndicatorProvider.checkCanceled()
            val headerList = if (scopedBlock.atOptional != null) optional else required
            addUnimplementedProtocolMethods(project, thisClassName, scopedBlock.methodHeaderList, headerList)
        }
    }
    return ObjJProtocolDeclarationPsiUtil.ProtocolMethods(required, optional)
}

/**
 * Adds unimplemented protocol getMethods to a list of headers
 */
private fun addUnimplementedProtocolMethods(project: Project, className: String,
                                            requiredHeaders: List<ObjJMethodHeader>, listOfUnimplemented: MutableList<ObjJMethodHeader>) {
    for (methodHeader in requiredHeaders) {
        ProgressIndicatorProvider.checkCanceled()
        if (!isProtocolMethodImplemented(project, className, methodHeader.selectorString)) {
            listOfUnimplemented.add(methodHeader)
        }
    }
}

/**
 * Checks whether a protocol method has been implemented into any class or extension with a given name
 */
private fun isProtocolMethodImplemented(project: Project, classNameIn: String?, selector: String): Boolean {
    var className: String? = classNameIn ?: return false
    while (className != null) {
        ProgressIndicatorProvider.checkCanceled()
        for (methodHeaderDeclaration in ObjJClassMethodIndex.instance[className, project]) {
            if (methodHeaderDeclaration.selectorString == selector) {
                return true
            }
        }
        className = getSuperClassName(className, project)
    }
    return false
}
