package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassMethodIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.Icon

private val LOGGER = Logger.getLogger("org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJClassDeclarationPsiUtil")

fun ObjJImplementationDeclaration.isCategory(): Boolean {
    val stub = stub
    return if (stub != null)
        stub.categoryName != null
    else
        categoryName != null
}

fun getAllClassNameElements(project: Project): List<ObjJClassName> {
    val classDeclarationsIndex = ObjJClassDeclarationsIndex.instance
    val classNameStrings = classDeclarationsIndex.getAllKeys(project)
    val classNameElements = ArrayList<ObjJClassName>()
    //ProgressIndicatorProvider.checkCanceled();
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }
    for (classNameString in classNameStrings) {
        val decs = classDeclarationsIndex[classNameString, project]
        for (dec in decs) {
            val classNameElement: ObjJClassName = dec.className ?: continue
            if (classNameElements.contains(classNameElement)) {
                continue
            }
            classNameElements.add(classNameElement)
        }
    }
    return classNameElements
}


/**
 * Gets the super class of the containing class declaration
 * @param returnDefault **true** if method should return containing class if super class is not found
 * @return containing super class
 */
fun ObjJCompositeElement.getContainingSuperClass(returnDefault: Boolean): ObjJClassName? {
    val containingClass = ObjJPsiImplUtil.getContainingClass(this)
            ?: //   LOGGER.log(Level.INFO, "Child element of type <"+childElement.getNode().getElementType().toString()+"> has no containing superclass.");
            return null
    val project = project
    if (containingClass !is ObjJImplementationDeclaration) {
        return if (returnDefault) containingClass.className else null
    }

    val superClassName = (if (!containingClass.isCategory()) containingClass.getSuperClassName() else containingClass.classNameString)
            ?: return if (returnDefault) containingClass.className else null
    //ProgressIndicatorProvider.checkCanceled();
    if (DumbService.isDumb(project)) {
        return null
    }
    val superClassDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[superClassName, project])
    if (superClassDeclarations.size < 1) {
        //   LOGGER.log(Level.INFO, "Super class references an undefined class <"+superClassName+">");
        return if (returnDefault) containingClass.className else null
    }
    for (superClassDec in superClassDeclarations) {
        if (superClassDec is ObjJImplementationDeclaration) {
            if (superClassDec.isEquivalentTo(containingClass) || superClassDec.isCategory()) {
                continue
            }
            return superClassDeclarations[0].className
        }
    }
    return null
}

fun addProtocols(
        classDeclarationElement: ObjJClassDeclarationElement<*>,
        protocols: MutableList<String>) {
    val stub = classDeclarationElement.stub
    val newProtocols: List<*>
    if (stub != null) {
        val stubElement = stub as ObjJClassDeclarationStub<*>?
        newProtocols = stubElement!!.inheritedProtocols
    } else {
        newProtocols = classDeclarationElement.inheritedProtocols
    }
    for (protocol in newProtocols) {
        if (protocols.contains(protocol)) {
            continue
        }
        protocols.add(protocol)
    }
}

fun getSuperClassName(className: String, project: Project): String? {
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }
    for (declaration in ObjJImplementationDeclarationsIndex.instance.get(className, project)) {
        if (declaration.getSuperClassName() != null) {
            return declaration.getSuperClassName()
        }
    }
    return null
}

fun ObjJImplementationDeclaration.getSuperClassName(): String? {
    val stub = stub
    return if (stub != null) stub.superClassName else superClass?.text
}

fun ObjJImplementationDeclaration.getMethodHeaders(): List<ObjJMethodHeader> {
    val headers = ArrayList<ObjJMethodHeader>()
    for (methodHeaderDeclaration in methodDeclarationList) {
        headers.add(methodHeaderDeclaration.methodHeader)
    }
    return headers
}

fun ObjJProtocolDeclaration.getMethodHeaders(): List<ObjJMethodHeader> {
    val headers = methodHeaderList
    for (scopedBlock in protocolScopedBlockList) {
        headers.addAll(scopedBlock.methodHeaderList)
    }
    return headers
}

fun ObjJProtocolDeclaration.hasMethod(selector: String): Boolean {
    for (methodHeader in methodHeaders) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }

    return false
}

fun ObjJImplementationDeclaration.hasMethod(selector: String): Boolean {
    for (methodHeader in methodHeaders) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }
    val instanceVariableDeclarationList = instanceVariableList?.instanceVariableDeclarationList ?: return false;
    for (instanceVariableDeclaration in instanceVariableDeclarationList) {
        if (instanceVariableDeclaration.atAccessors == null) {
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


fun ObjJImplementationDeclaration.getAllUnimplementedProtocolMethods(): Map<ObjJClassName, ProtocolMethods> {
    //todo
    return emptyMap()
}


fun ObjJImplementationDeclaration.getUnimplementedProtocolMethods(protocolName: String): ProtocolMethods {
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }

    val thisClassName = classNameString
    val project = project
    val required = ArrayList<ObjJMethodHeader>()
    val optional = ArrayList<ObjJMethodHeader>()
    val inheritedProtocols = ObjJInheritanceUtil.getAllInheritedProtocols(protocolName, project)
    for (protocolDeclaration in inheritedProtocols) {
        //Logger.getAnonymousLogger().log(Level.INFO, "Checking protocol <"+protocolDeclaration.getClassNameString()+"> for unimplemented methods.");
        addUnimplementedProtocolMethods(project, thisClassName, protocolDeclaration.methodHeaderList, required)
        for (scopedBlock in protocolDeclaration.protocolScopedBlockList) {
            ProgressIndicatorProvider.checkCanceled()
            val headerList = if (scopedBlock.atOptional != null) optional else required
            addUnimplementedProtocolMethods(project, thisClassName, scopedBlock.methodHeaderList, headerList)
        }
    }
    return ProtocolMethods(required, optional)
}

private fun addUnimplementedProtocolMethods(project: Project, className: String,
                                            requiredHeaders: List<ObjJMethodHeader>, listOfUnimplemented: MutableList<ObjJMethodHeader>) {
    for (methodHeader in requiredHeaders) {
        ProgressIndicatorProvider.checkCanceled()
        if (!isProtocolMethodImplemented(project, className, methodHeader.selectorString)) {
            listOfUnimplemented.add(methodHeader)
        }
    }
}

private fun isProtocolMethodImplemented(project: Project, classNameIn: String?, selector: String): Boolean {
    var className: String? = classNameIn ?: return false;
    while (className != null) {
        ProgressIndicatorProvider.checkCanceled()
        for (methodHeaderDeclaration in ObjJClassMethodIndex.instance.get(className = className, project = project)) {
            if (methodHeaderDeclaration.selectorString == selector) {
                LOGGER.log(Level.INFO, "Class <$className> has required protocol method: <selector> in class: ${methodHeaderDeclaration.containingClassName} in file ${ObjJFileUtil.getContainingFileName(methodHeaderDeclaration)}")
                return true
            }
        }
        //LOGGER.log(Level.INFO, "Class <"+className+"> does not have required protocol method: <"+selector+">");
        className = getSuperClassName(className, project)
    }
    return false
}


fun ObjJImplementationDeclaration.getPresentation(): ItemPresentation {
    //LOGGER.log(Level.INFO, "Get Presentation <Implementation:"+implementationDeclaration.getClassNameString()+">");
    val text = classNameString + if (isCategory()) " ($categoryName)" else ""
    val icon = if (isCategory()) ObjJIcons.CATEGORY_ICON else ObjJIcons.CLASS_ICON
    val fileName = ObjJFileUtil.getContainingFileName(this)
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

fun ObjJProtocolDeclaration.getPresentation(): ItemPresentation {
    //LOGGER.log(Level.INFO, "Get Presentation <Protocol:"+protocolDeclaration.getClassNameString()+">");
    val fileName = ObjJFileUtil.getContainingFileName(this)
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return classNameString
        }

        override fun getLocationString(): String {
            return fileName ?: ""
        }

        override fun getIcon(b: Boolean): Icon {
            return ObjJIcons.PROTOCOL_ICON
        }
    }
}
