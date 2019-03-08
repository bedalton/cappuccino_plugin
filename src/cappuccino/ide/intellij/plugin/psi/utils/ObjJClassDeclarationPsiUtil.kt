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
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.ide.presentation.Presentation
import icons.ObjJIcons
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.Icon

private val LOGGER = Logger.getLogger("cappuccino.ide.intellij.plugin.psi.utils.ObjJClassDeclarationPsiUtil")

fun isCategory(declaration:ObjJImplementationDeclaration): Boolean =
    declaration.stub?.isCategory ?: declaration.categoryName != null

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
            val classNameElement: ObjJClassName = dec.getClassName() ?: continue
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
fun getContainingSuperClass(psiElement:ObjJCompositeElement,returnDefault: Boolean): ObjJClassName? {
    val containingClass = ObjJPsiImplUtil.getContainingClass(psiElement)
            ?: //   LOGGER.log(Level.INFO, "Child element of type <"+childElement.getNode().getElementType().toString()+"> has no containing superclass.");
            return null
    val project = psiElement.project
    if (containingClass !is ObjJImplementationDeclaration) {
        return if (returnDefault) containingClass.getClassName() else null
    }

    val superClassName = (if (!containingClass.isCategory()) containingClass.superClassName else containingClass.getClassNameString())
            ?: return if (returnDefault) containingClass.getClassName() else null
    //ProgressIndicatorProvider.checkCanceled();
    if (DumbService.isDumb(project)) {
        return null
    }
    val superClassDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[superClassName, project])
    if (superClassDeclarations.size < 1) {
        //   LOGGER.log(Level.INFO, "Super class references an undefined class <"+superClassName+">");
        return if (returnDefault) containingClass.getClassName() else null
    }
    for (superClassDec in superClassDeclarations) {
        if (superClassDec is ObjJImplementationDeclaration) {
            if (superClassDec.isEquivalentTo(containingClass) || superClassDec.isCategory()) {
                continue
            }
            return superClassDeclarations[0].getClassName()
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
        newProtocols = classDeclarationElement.getInheritedProtocols()
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
    for (declaration in ObjJImplementationDeclarationsIndex.instance[className, project]) {
        if (declaration.superClassName != null) {
            return declaration.superClassName
        }
    }
    return null
}

fun getSuperClassName(declaration: ObjJImplementationDeclaration): String? {
    val stub = declaration.stub
    return if (stub != null) stub.superClassName else declaration.superClass?.text
}

fun getMethodHeaders(declaration:ObjJImplementationDeclaration): List<ObjJMethodHeader> {
    val headers = ArrayList<ObjJMethodHeader>()
    for (methodHeaderDeclaration in declaration.methodDeclarationList) {
        headers.add(methodHeaderDeclaration.methodHeader)
    }
    return headers
}

fun getMethodHeaders(declaration:ObjJProtocolDeclaration): List<ObjJMethodHeader> {
    val headers = declaration.getMethodHeaderList().toMutableList()
    for (scopedBlock in declaration.protocolScopedMethodBlockList) {
        headers.addAll(scopedBlock.methodHeaderList)
    }
    return headers
}

fun hasMethod(declaration:ObjJProtocolDeclaration,selector: String): Boolean {
    for (methodHeader in declaration.getMethodHeaderList()) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }

    return false
}

fun hasMethod(implementationDeclaration:ObjJImplementationDeclaration,selector: String): Boolean {
    for (methodHeader in implementationDeclaration.getMethodHeaders()) {
        if (methodHeader.selectorString == selector) {
            return true
        }
    }
    val instanceVariableDeclarationList = implementationDeclaration.instanceVariableList?.instanceVariableDeclarationList ?: return false;
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


fun getAllUnimplementedProtocolMethods(declaration:ObjJImplementationDeclaration): Map<ObjJClassName, ProtocolMethods> {
    //todo
    return emptyMap()
}


fun getUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolName: String): ProtocolMethods {
    val project = declaration.project
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }

    val thisClassName = declaration.getClassNameString()
    val required = ArrayList<ObjJMethodHeader>()
    val optional = ArrayList<ObjJMethodHeader>()
    val inheritedProtocols = ObjJInheritanceUtil.getAllInheritedProtocols(protocolName, project)
    for (protocolDeclaration in inheritedProtocols) {
        //Logger.getAnonymousLogger().log(Level.INFO, "Checking protocol <"+protocolDeclaration.getClassNameString()+"> for unimplemented methods.");
        addUnimplementedProtocolMethods(project, thisClassName, protocolDeclaration.getMethodHeaderList(), required)
        for (scopedBlock in protocolDeclaration.protocolScopedMethodBlockList) {
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
        for (methodHeaderDeclaration in ObjJClassMethodIndex.instance[className, project]) {
            if (methodHeaderDeclaration.selectorString == selector) {
                //LOGGER.log(Level.INFO, "Class <$className> has required protocol method: <selector> in class: ${methodHeaderDeclaration.containingClassName} in file ${ObjJFileUtil.getContainingFileName(methodHeaderDeclaration)}")
                return true
            }
        }
        //LOGGER.log(Level.INFO, "Class <"+className+"> does not have required protocol method: <"+selector+">");
        className = getSuperClassName(className, project)
    }
    return false
}


fun getPresentation(declaration:ObjJImplementationDeclaration): ItemPresentation {
    //LOGGER.log(Level.INFO, "Get Presentation <Implementation:"+implementationDeclaration.getClassNameString()+">");
    val text = declaration.getClassNameString() + if (declaration.isCategory()) " (${declaration.categoryName?.className?.text})" else ""
    val icon = if (declaration.isCategory()) ObjJIcons.CATEGORY_ICON else ObjJIcons.CLASS_ICON
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

fun getPresentation(declaration:ObjJProtocolDeclaration): ItemPresentation {
    //LOGGER.log(Level.INFO, "Get Presentation <Protocol:"+protocolDeclaration.getClassNameString()+">");
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

fun getInheritedProtocols(classDeclaration:ObjJImplementationDeclaration) : List<String> {
    val stubProtocols = classDeclaration.stub?.inheritedProtocols
    if (stubProtocols != null) {
        return stubProtocols
    }
    return getProtocolListAsStrings(classDeclaration.inheritedProtocolList)
}

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
    return getDummyPresenter(className);
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