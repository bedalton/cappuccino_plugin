package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED

object ObjJInheritanceUtil {

    fun appendAllInheritedClassesStrictToList(className: String, project: Project): MutableSet<String> {
        return getAllInheritedClasses(className, project, false)
    }

    fun getAllInheritedClasses(className: String, project: Project, withProtocols: Boolean = true): MutableSet<String> {
        val inheritedClasses = mutableSetOf<String>()
        appendAllInheritedClassesToSet(inheritedClasses, className, project, withProtocols)
        return inheritedClasses
    }


    fun appendAllInheritedProtocolsToSet(className: String, project: Project): MutableSet<ObjJProtocolDeclaration> {
        val out = mutableSetOf<ObjJProtocolDeclaration>()
        appendAllInheritedProtocolsToSet(out, className, project)
        return out
    }

    private fun appendAllInheritedProtocolsToSet(out: MutableSet<ObjJProtocolDeclaration>, className: String, project: Project) {
        ProgressIndicatorProvider.checkCanceled()
        if (className == UNDETERMINED || className == ObjJClassType.CLASS || ObjJClassType.isPrimitive(className)) {
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
            ProgressIndicatorProvider.checkCanceled()
            appendAllInheritedProtocolsToSet(out, parentProtocolNameElement.text, project)
        }
    }

    private fun isProtocolInArray(protocolDeclarations: Set<ObjJProtocolDeclaration>, className: String): Boolean {
        for (protocolDeclaration in protocolDeclarations) {
            ProgressIndicatorProvider.checkCanceled()
            if (protocolDeclaration.getClassNameString() == className) {
                return true
            }
        }
        return false
    }

    private fun appendAllInheritedClassesToSet(classNames: MutableSet<String>, className: String, project: Project, withProtocols:Boolean = true) {
        if (className == UNDETERMINED || className == ObjJClassType.CLASS || ObjJClassType.isPrimitive(className)) {
            return
        }

        if (DumbService.isDumb(project)) {
            classNames.add(UNDETERMINED)
            return
        }
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
        if (parentClass == ObjJClassType.UNDEF_CLASS_NAME || subclassName == ObjJClassType.UNDEF_CLASS_NAME) {
            throw CannotDetermineException()
        }
        return if (parentClass == UNDETERMINED || parentClass == UNDETERMINED) {
            true
        } else getAllInheritedClasses(subclassName, project).contains(parentClass)
    }


    fun getInheritanceUpAndDown(className: String, project: Project): Set<String> {
        val referencedAncestors = mutableSetOf<String>()
        for (parentClass in ObjJInheritanceUtil.getAllInheritedClasses(className, project)) {
            if (!referencedAncestors.contains(parentClass)) {
                referencedAncestors.add(parentClass)
            }
        }
        for (childClass in ObjJClassInheritanceIndex.instance.getChildClassesAsStrings(className, project)) {
            if (!referencedAncestors.contains(childClass)) {
                referencedAncestors.add(childClass)
            }
        }
        return referencedAncestors
    }

    /**
     * Adds protocol classes to set
     */
    private fun addProtocols(
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

}