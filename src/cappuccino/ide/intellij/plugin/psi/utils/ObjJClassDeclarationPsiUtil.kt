package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import icons.ObjJIcons
import java.util.*
import javax.swing.Icon


/**
 * Determines whether an @implementation class is a category class
 */
fun isCategory(declaration:ObjJImplementationDeclaration): Boolean =
    declaration.stub?.isCategory ?: declaration.categoryName != null

/**
 * Gets the containing class's super class
 */
fun getContainingSuperClassName(psiElement: ObjJCompositeElement, returnDefault: Boolean) : String? {
    val containingClass = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return null
    val project = psiElement.project
    if (containingClass !is ObjJImplementationDeclaration) {
        return if (returnDefault) containingClass.getClassNameString() else null
    }
    if (!containingClass.isCategory) {
        return containingClass.superClass?.text
    }
    return getCategoryClassBaseDeclaration(containingClass.getClassNameString(), project)?.superClassName ?: if (returnDefault) containingClass.getClassNameString() else null

}

private fun getCategoryClassBaseDeclaration(classNameString:String, project: Project) : ObjJImplementationDeclaration? {
    val classDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[classNameString, project])
    for (classDeclaration in classDeclarations) {
        if (classDeclaration is ObjJImplementationDeclaration) {
            if (classDeclaration.isCategory) {
                continue
            }
            return classDeclaration
        }
    }
    return null
}

/**
 * Gets the super class of the containing class declaration
 * @param returnDefault **true** if method should return containing class if super class is not found
 * @return containing super class
 */
fun getContainingSuperClass(psiElement:ObjJCompositeElement, returnDefault: Boolean, filter: ((ObjJClassDeclarationElement<*>) -> Boolean)? = null): ObjJClassName? {
    val project = psiElement.project
    if (DumbService.isDumb(project)) {
        return null
    }
    val containingClass = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return null
    val superClassName = getContainingSuperClassName(psiElement, returnDefault) ?: return if (returnDefault) containingClass.getClassName() else null

    val superClassDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[superClassName, project])
    if (superClassDeclarations.size < 1) {
        return if (returnDefault) containingClass.getClassName() else null
    }
    var className:ObjJClassName? = null
    for (superClassDec in superClassDeclarations) {
        if (superClassDec is ObjJImplementationDeclaration) {
            className = superClassDec.getClassName() ?: continue
            if (filter != null && filter(superClassDec)) {
                return className
            }
            if (superClassDec.isEquivalentTo(containingClass) || superClassDec.isCategory) {
                continue
            }
            if (filter == null) {
                return className
            }
        }
    }
    return className
}

fun getContainingClassWithSelector(containingClassName:String, selector:String, defaultName:ObjJClassName) : ObjJClassName {
    val project:Project = defaultName.project
    if (DumbService.isDumb(project)) {
        return defaultName
    }
    val classDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[containingClassName, project])
    if (classDeclarations.size < 1) {
        return defaultName
    }
    var potential:ObjJClassName? = null
    var className:ObjJClassName? = null
    for (classDeclaration in classDeclarations) {
        if (classDeclaration is ObjJImplementationDeclaration) {
            className = classDeclaration.getClassName() ?: continue
            if (classDeclaration.hasMethod(selector)) {
                if (classDeclaration.isCategory) {
                    potential = className
                } else {
                    return className
                }
            } else if (potential == null) {
                potential = className
            }
        } else if (potential == null) {
            val protocol = classDeclaration as? ObjJProtocolDeclaration ?: continue
            if (protocol.hasMethod(selector)) {
                potential = className
            }
        }
    }
    return potential ?: className ?: defaultName
}


/**
 * Adds protocol classes to set
 */
fun addProtocols(
        classDeclarationElement: ObjJClassDeclarationElement<*>,
        protocols: MutableSet<String>) {
    val stub = classDeclarationElement.stub
    val newProtocols = stub?.inheritedProtocols ?: classDeclarationElement.getInheritedProtocols()
    for (protocol in newProtocols) {
        if (protocols.contains(protocol)) {
            continue
        }
        protocols.add(protocol)
    }
}

/**
 * Gets the super class name for a class with a given name
 */
fun getSuperClassName(className: String, project: Project): String? {
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }
    for (declaration in ObjJImplementationDeclarationsIndex.instance[className, project]) {
        if (declaration.superClassName != null) {
            return declaration.superClassName
        }
    }
    return null
}

/**
 * Gets the super class given an implementation class
 */
fun getSuperClassName(declaration: ObjJImplementationDeclaration): String? {
    val stub = declaration.stub
    return if (stub != null) stub.superClassName else declaration.superClass?.text
}

/**
 * Gets method headers for a class declaration
 */
fun getMethodHeaders(declaration:ObjJImplementationDeclaration): List<ObjJMethodHeader> {
    val headers = ArrayList<ObjJMethodHeader>()
    for (methodHeaderDeclaration in declaration.methodDeclarationList) {
        headers.add(methodHeaderDeclaration.methodHeader)
    }
    return headers
}

/**
 * Gets method headers for a given protocol element
 */
fun getMethodHeaders(declaration:ObjJProtocolDeclaration): List<ObjJMethodHeader> {
    val headers = declaration.methodHeaderList.toMutableList()
    for (scopedBlock in declaration.protocolScopedMethodBlockList) {
        headers.addAll(scopedBlock.methodHeaderList)
    }
    return headers
}

/**
 * Determines whether a protocol contains a given method
 */
fun hasMethod(declaration:ObjJProtocolDeclaration, selector: String): Boolean {
    for (methodHeader in getMethodHeaders(declaration)) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }

    return false
}

/**
 * Determines whether this implementation class has a given selector
 */
fun hasMethod(implementationDeclaration:ObjJImplementationDeclaration,selector: String): Boolean {
    for (methodHeader in implementationDeclaration.getMethodHeaders()) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }
    val instanceVariableDeclarationList = implementationDeclaration.instanceVariableList?.instanceVariableDeclarationList ?: return false
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
 * Gets all unimplemented protocol methods
 */
fun getAllUnimplementedProtocolMethods(@Suppress("UNUSED_PARAMETER") declaration:ObjJImplementationDeclaration): Map<ObjJClassName, ProtocolMethods> {
    //todo
    return emptyMap()
}

/**
 * Gets unimplemented protocols for an @implementation declaration for a given protocol name
 */
fun getUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolName: String): ProtocolMethods {
    val project = declaration.project
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }

    val thisClassName = declaration.getClassNameString()
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
    return ProtocolMethods(required, optional)
}

/**
 * Adds unimplemented protocol methods to a list of headers
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

/**
 * Gets the presentation object for an @implementation declaration
 */
fun getPresentation(declaration:ObjJImplementationDeclaration): ItemPresentation {
    val text = declaration.getClassNameString() + if (declaration.isCategory) " (${declaration.categoryName?.className?.text})" else ""
    val icon = if (declaration.isCategory) ObjJIcons.CATEGORY_ICON else ObjJIcons.CLASS_ICON
    val fileName = ObjJFileUtil.getContainingFileName(declaration)
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return text
        }

        override fun getLocationString(): String {
            return fileName ?: ""
        }

        override fun getIcon(b: Boolean): Icon {
            return icon
        }
    }
}

/**
 * Gets presentation for a protocol declaration
 */
fun getPresentation(declaration:ObjJProtocolDeclaration): ItemPresentation {
    val fileName = ObjJFileUtil.getContainingFileName(declaration)
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return declaration.getClassNameString()
        }

        override fun getLocationString(): String {
            return fileName ?: ""
        }

        override fun getIcon(b: Boolean): Icon {
            return ObjJIcons.PROTOCOL_ICON
        }
    }
}

/**
 * Get all inherited protocols for a given class
 */
fun getInheritedProtocols(classDeclaration:ObjJImplementationDeclaration) : List<String> {
    val stubProtocols = classDeclaration.stub?.inheritedProtocols
    if (stubProtocols != null) {
        return stubProtocols
    }
    return getProtocolListAsStrings(classDeclaration.inheritedProtocolList)
}

/**
 * Gets inherited protocols for a given protocol
 */
fun getInheritedProtocols(protocolDeclaration:ObjJProtocolDeclaration) : List<String> {
    val stubProtocols = protocolDeclaration.stub?.inheritedProtocols
    if (stubProtocols != null) {
        return stubProtocols
    }
    return getProtocolListAsStrings(protocolDeclaration.inheritedProtocolList)

}

private fun getProtocolListAsStrings(protocolListElement:ObjJInheritedProtocolList?) : List<String> {
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

fun getPresentation(className:ObjJClassName) : ItemPresentation {
    val parent = className.parent as? ObjJClassDeclarationElement<*> ?: return getDummyPresenter(className)
    if (parent is ObjJImplementationDeclaration) {
        return getPresentation(parent)
    } else if (parent is ObjJProtocolDeclaration) {
        return getPresentation(parent)
    }
    return getDummyPresenter(className)
}

fun getDummyPresenter(psiElement: ObjJCompositeElement) : ItemPresentation {
    val fileName = ObjJFileUtil.getContainingFileName(psiElement)
    return object : ItemPresentation {
        override fun getIcon(p0: Boolean): Icon? {
            return null
        }

        override fun getLocationString(): String? {
            return fileName ?: ""
        }

        override fun getPresentableText(): String? {
            return psiElement.text
        }
    }
}