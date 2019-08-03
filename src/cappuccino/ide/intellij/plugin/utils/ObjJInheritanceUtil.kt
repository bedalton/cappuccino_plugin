package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.ID
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED

object ObjJInheritanceUtil {

    private val STRIP_GENERIC_REGEX = "[^<]+(<[^>]+)".toRegex()

    fun getAllInheritedClasses(className: String, project: Project, withProtocols: Boolean = true): MutableSet<String> {
        val inheritedClasses = mutableSetOf<String>()
        appendAllInheritedClassesToSet(inheritedClasses, className, project, withProtocols)
        return inheritedClasses
    }

    fun getAllSuperClasses(classDeclarationElement: ObjJImplementationDeclaration) : List<ObjJImplementationDeclaration> {
        val superClass = classDeclarationElement.superClassName ?: return emptyList()
        val project = classDeclarationElement.project
        val out = mutableListOf<ObjJImplementationDeclaration>()
        ObjJImplementationDeclarationsIndex.instance[superClass, project].forEach {
            out.add(it)
            out.addAll(getAllSuperClasses(it))
        }
        return out
    }

    fun getAllProtocols(classDeclarationElement: ObjJClassDeclarationElement<*>) : List<ObjJProtocolDeclaration> {
        val names = mutableSetOf<String>()
        val project = classDeclarationElement.project
        addProtocols(classDeclarationElement, names)
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            classDeclarationElement.superClassDeclarations.forEach {
                addProtocols(it, names)
            }
        }
        return names.flatMap {
            ObjJProtocolDeclarationsIndex.instance[it, project]
        }
    }

    fun appendAllInheritedProtocolsToSet(className: String, project: Project): MutableSet<ObjJProtocolDeclaration> {
        val out = mutableSetOf<ObjJProtocolDeclaration>()
        appendAllInheritedProtocolsToSet(out, className, project)
        return out
    }

    private fun appendAllInheritedProtocolsToSet(out: MutableSet<ObjJProtocolDeclaration>, className: String, project: Project) {
        if (className == UNDETERMINED || className == ID || className == ObjJClassType.CLASS || ObjJClassType.isPrimitive(className)) {
            return
        }

        if (isProtocolInArray(out, className)) {
            return
        }

        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(project)) {
            return
        }

        val temp = ObjJProtocolDeclarationsIndex.instance[className, project]
        if (temp.isEmpty()) {
            return
        }
        val thisProtocol = temp[0]
        out.add(thisProtocol)
        val protocolList = thisProtocol.inheritedProtocolList ?: return
        for (parentProtocolNameElement in protocolList.classNameList) {
            appendAllInheritedProtocolsToSet(out, parentProtocolNameElement.text, project)
        }
    }

    private fun isProtocolInArray(protocolDeclarations: Set<ObjJProtocolDeclaration>, className: String): Boolean {
        for (protocolDeclaration in protocolDeclarations) {
            if (protocolDeclaration.classNameString == className) {
                return true
            }
        }
        return false
    }

    private fun appendAllInheritedClassesToSet(classNames: MutableSet<String>, classNameIn: String, project: Project, withProtocols:Boolean = true) {
        if (classNameIn == UNDETERMINED || classNameIn == ID || classNameIn == ObjJClassType.CLASS || ObjJClassType.isPrimitive(classNameIn)) {
            return
        }

        if (DumbService.isDumb(project)) {
            classNames.add(UNDETERMINED)
            return
        }
        val className = classNameIn.replace(STRIP_GENERIC_REGEX, "")
        val classesDeclarations = ObjJClassDeclarationsIndex.instance[className, project]
        if (classesDeclarations.isEmpty()) {
            return
        }
        if (!classNames.contains(className)) {
            classNames.add(className)
        }
        for (classDeclaration in classesDeclarations) {
            if (withProtocols) {
                addProtocols(classDeclaration, classNames)
            }
            if (classDeclaration is ObjJImplementationDeclaration) {
                val superClassName = classDeclaration.superClassName
                if (superClassName == null || classNames.contains(superClassName)) {
                    continue
                }
                appendAllInheritedClassesToSet(classNames, superClassName, project)
            }
        }
    }

    @Throws(CannotDetermineException::class)
    fun isSubclassOrSelf(parentClass: String?, subclassName: String?, project: Project): Boolean {
        if (parentClass == null || subclassName == null) {
            return false
        }
        if (parentClass == subclassName) {
            return true
        }
        if (parentClass == ObjJClassType.UNDEF_CLASS_NAME || subclassName == UNDETERMINED) {
            throw CannotDetermineException()
        }
        return if (parentClass == UNDETERMINED || parentClass == ID || subclassName == UNDETERMINED || subclassName == ID) {
            true
        } else getAllInheritedClasses(subclassName, project).contains(parentClass)
    }

    /**
     * Adds protocol classes to set
     */
    private fun addProtocols(
            classDeclarationElement: ObjJClassDeclarationElement<*>,
            protocols: MutableSet<String>) {
        val project = classDeclarationElement.project
        val stub = classDeclarationElement.stub
        val newProtocols = stub?.inheritedProtocols ?: classDeclarationElement.getInheritedProtocols()
        for (protocol in newProtocols) {
            if (protocols.contains(protocol)) {
                continue
            }
            protocols.add(protocol)
            ObjJProtocolDeclarationsIndex.instance[protocol, project].forEach {
                addProtocols(it, protocols)
            }
        }
    }

}