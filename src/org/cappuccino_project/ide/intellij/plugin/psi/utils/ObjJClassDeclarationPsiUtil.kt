package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubElement
import com.intellij.usageView.UsageViewUtil
import javafx.scene.control.ProgressIndicator
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassMethodIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJCompositeElementImpl
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil
import sun.rmi.runtime.Log

import javax.swing.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

object ObjJClassDeclarationPsiUtil {

    private val LOGGER = Logger.getLogger(ObjJClassDeclarationPsiUtil::class.java.canonicalName)

    fun isCategory(implementationDeclaration: ObjJImplementationDeclaration): Boolean {
        return implementationDeclaration.categoryName != null
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
            val decs = classDeclarationsIndex.get(classNameString, project)
            for (dec in decs) {
                val classNameElement = dec.className
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
     * @param childElement child element of a class declaration with a super class
     * @param returnDefault **true** if method should return containing class if super class is not found
     * @return containing super class
     */
    fun getContainingSuperClass(childElement: ObjJCompositeElement, returnDefault: Boolean): ObjJClassName? {
        val containingClass = ObjJPsiImplUtil.getContainingClass(childElement)
                ?: //   LOGGER.log(Level.INFO, "Child element of type <"+childElement.getNode().getElementType().toString()+"> has no containing superclass.");
                return null

        if (containingClass !is ObjJImplementationDeclaration) {
            return if (returnDefault) containingClass.className else null
        }

        val superClassName = (if (!containingClass.isCategory) containingClass.superClassName else containingClass.classNameString)
                ?: //   LOGGER.log(Level.INFO, "Cannot get super declaration as class has no super class.");
                return if (returnDefault) containingClass.className else null
//ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(containingClass.project)) {
            return null
        }
        val superClassDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance.get(superClassName, childElement.project))
        if (superClassDeclarations.size < 1) {
            //   LOGGER.log(Level.INFO, "Super class references an undefined class <"+superClassName+">");
            return if (returnDefault) containingClass.className else null
        }
        for (superClassDec in superClassDeclarations) {
            if (superClassDec is ObjJImplementationDeclaration) {
                val superClassImplementationDeclaration = superClassDec as ObjJImplementationDeclaration
                if (superClassImplementationDeclaration.isEquivalentTo(containingClass) || superClassImplementationDeclaration.isCategory) {
                    continue
                }
                return superClassDeclarations.get(0).className
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
        for (proto in newProtocols) {
            val protocol = proto as String
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
            if (declaration.superClassName != null) {
                return declaration.superClassName
            }
        }
        return null
    }

    fun getSuperClassName(implementationDeclaration: ObjJImplementationDeclaration): String? {
        val stub = implementationDeclaration.stub
        return if (stub != null) stub.superClassName else if (implementationDeclaration.superClass != null) implementationDeclaration.superClass!!.text else null
    }

    fun getMethodHeaders(declaration: ObjJImplementationDeclaration): List<ObjJMethodHeader> {
        val headers = ArrayList<ObjJMethodHeader>()
        for (methodHeaderDeclaration in declaration.methodDeclarationList) {
            headers.add(methodHeaderDeclaration.methodHeader)
        }
        return headers
    }

    fun getMethodHeaders(protocolDeclaration: ObjJProtocolDeclaration): List<ObjJMethodHeader> {
        val headers = protocolDeclaration.methodHeaderList
        for (scopedBlock in protocolDeclaration.protocolScopedBlockList) {
            headers.addAll(scopedBlock.methodHeaderList)
        }
        return headers
    }

    fun hasMethod(classDeclarationElement: ObjJProtocolDeclaration, selector: String): Boolean {
        for (methodHeader in classDeclarationElement.methodHeaders) {
            if (methodHeader.selectorString == selector) {
                return true
            }
        }

        return false
    }

    fun hasMethod(classDeclaration: ObjJImplementationDeclaration, selector: String): Boolean {
        for (methodHeader in classDeclaration.methodHeaders) {
            if (methodHeader.selectorString == selector) {
                return true
            }
        }
        if (classDeclaration.instanceVariableList != null) {
            for (instanceVariableDeclaration in classDeclaration.instanceVariableList!!.instanceVariableDeclarationList) {
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
        }
        return false
    }


    fun getAllUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration): Map<ObjJClassName, ProtocolMethods> {
        //todo
        return emptyMap()
    }


    fun getUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolName: String): ProtocolMethods {
        val protocolListElement = declaration.inheritedProtocolList
                ?: return ObjJProtocolDeclarationPsiUtil.EMPTY_PROTOCOL_METHODS_RESULT
        if (DumbService.isDumb(declaration.project)) {
            throw IndexNotReadyRuntimeException()
        }

        val thisClassName = declaration.classNameString
        val project = declaration.project
        val required = ArrayList<ObjJMethodHeader>()
        val optional = ArrayList<ObjJMethodHeader>()
        val inheritedProtocols = ObjJInheritanceUtil.getAllInheritedProtocols(protocolName, declaration.project)
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

    private fun isProtocolMethodImplemented(project: Project, className: String, selector: String): Boolean {
        var className = className
        while (className != null) {
            ProgressIndicatorProvider.checkCanceled()
            for (methodHeaderDeclaration in ObjJClassMethodIndex.instance.get(className, project)) {
                if (methodHeaderDeclaration.selectorString == selector) {
                    LOGGER.log(Level.INFO, "Class <" + className + "> has required protocol method: <" + selector + "> in class: " + methodHeaderDeclaration.containingClassName + " in file " + ObjJFileUtil.getContainingFileName(methodHeaderDeclaration))
                    return true
                }
            }
            //LOGGER.log(Level.INFO, "Class <"+className+"> does not have required protocol method: <"+selector+">");
            className = ObjJClassDeclarationPsiUtil.getSuperClassName(className, project)
        }
        return false
    }


    fun getPresentation(implementationDeclaration: ObjJImplementationDeclaration): ItemPresentation {
        //LOGGER.log(Level.INFO, "Get Presentation <Implementation:"+implementationDeclaration.getClassNameString()+">");
        val text = implementationDeclaration.classNameString + if (implementationDeclaration.isCategory) " (" + implementationDeclaration.categoryName + ")" else ""
        val icon = if (implementationDeclaration.isCategory) ObjJIcons.CATEGORY_ICON else ObjJIcons.CLASS_ICON
        val fileName = ObjJFileUtil.getContainingFileName(implementationDeclaration)
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

    fun getPresentation(protocolDeclaration: ObjJProtocolDeclaration): ItemPresentation {
        //LOGGER.log(Level.INFO, "Get Presentation <Protocol:"+protocolDeclaration.getClassNameString()+">");
        val fileName = ObjJFileUtil.getContainingFileName(protocolDeclaration)
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return protocolDeclaration.classNameString
            }

            override fun getLocationString(): String {
                return fileName ?: ""
            }

            override fun getIcon(b: Boolean): Icon {
                return ObjJIcons.PROTOCOL_ICON
            }
        }
    }

}
