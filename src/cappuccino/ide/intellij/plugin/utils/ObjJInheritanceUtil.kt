package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.addProtocols

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

object ObjJInheritanceUtil {

    /**
     * Meant to take an array of inheritance, and reduce it to the deepest descendant in the tree
     * @param classList list of classes
     * @param project project
     * @return list of deepest descendants
     * @throws IndexNotReadyInterruptingException thrown if index is not ready
     */
    @Throws(IndexNotReadyInterruptingException::class)
    fun reduceToDeepestInheritance(classList: List<String>, project: Project): List<String> {
        val superClasses = ArrayList<String>()
        val out = ArrayList(classList)
        if (DumbService.isDumb(project)) {
            throw IndexNotReadyInterruptingException()
        }
        for (className in classList) {
            if (superClasses.contains(className)) {
                out.remove(className)
                continue
            }
            for (parentClassName in getAllInheritedClasses(className, project)) {
                if (out.contains(parentClassName)) {
                    out.remove(parentClassName)
                }
                superClasses.add(parentClassName)
            }
            out.add(className)
        }
        return superClasses
    }

    fun getAllInheritedClassesStrict(className: String, project: Project): MutableList<String> {
        return getAllInheritedClasses(className, project, false)
    }

    fun getAllInheritedClasses(className: String, project: Project, withProtocols: Boolean = true): MutableList<String> {
        val inheritedClasses = ArrayList<String>()
        getAllInheritedClasses(inheritedClasses, className, project)
        return inheritedClasses
    }

    fun isInstanceVariableInClasses(variableName:String, className:String, project:Project) : Boolean {
        if (isInstanceVariableInClass(variableName, className, project)) {
            return true
        }
        for (classNameInLoop in getAllInheritedClasses(className, project)) {
            if (isInstanceVariableInClass(variableName, classNameInLoop, project)) {
                return true
            }
        }
        return false
    }

    fun isInstanceVariableInClass(variableName:String, className:String, project:Project) : Boolean {
        for (variable in ObjJInstanceVariablesByClassIndex.instance[className, project]) {
            //Logger.getAnonymousLogger().log(Level.INFO, "Does Variable ${variable.text} == $variableName?")
            if (variable.text == variableName) {
                return true
            }
        }
        return false
    }


    fun getAllInheritedProtocols(className: String, project: Project): MutableList<ObjJProtocolDeclaration> {
        val out = ArrayList<ObjJProtocolDeclaration>()
        getAllInheritedProtocols(out, className, project)
        return out
    }

    private fun getAllInheritedProtocols(out: MutableList<ObjJProtocolDeclaration>, className: String, project: Project) {
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

        val temp = ObjJProtocolDeclarationsIndex.instance.get(className, project)
        if (temp.isEmpty()) {
            return
        }
        val thisProtocol = temp.get(0)
        out.add(thisProtocol)
        val protocolList = thisProtocol.inheritedProtocolList ?: return
        for (parentProtocolNameElement in protocolList.classNameList) {
            ProgressIndicatorProvider.checkCanceled()
            getAllInheritedProtocols(out, parentProtocolNameElement.getText(), project)
            /*
            for (ObjJProtocolDeclaration currentProtocolInLoop: ObjJProtocolDeclarationsIndex.getInstance().get(parentProtocolNameElement.getText(), project)) {
                ProgressIndicatorProvider.checkCanceled();
                inheritedProtocolList = currentProtocolInLoop != null ? currentProtocolInLoop.getInheritedProtocolList() : null;
                if (inheritedProtocolList == null) {
                    continue;
                }
                for (ObjJClassName currentLoopClassName : inheritedProtocolList.getClassNameList()) {
                    getAllInheritedProtocols(foldingDescriptors, currentLoopClassName.getText(), project);
                }
            }
            */
        }
    }

    private fun isProtocolInArray(protocolDeclarations: List<ObjJProtocolDeclaration>, className: String): Boolean {
        for (protocolDeclaration in protocolDeclarations) {
            ProgressIndicatorProvider.checkCanceled()
            if (protocolDeclaration.getClassNameString() == className) {
                return true
            }
        }
        return false
    }

    fun getAllInheritedClassesStrict(classNames: MutableList<String>, className: String, project: Project) {
        return getAllInheritedClasses(classNames, className, project, false)
    }

    fun getAllInheritedClasses(classNames: MutableList<String>, className: String, project: Project, withProtocols:Boolean = true) {
        if (className == UNDETERMINED || className == ObjJClassType.CLASS || ObjJClassType.isPrimitive(className)) {
            return
        }

        //ProgressIndicatorProvider.checkCanceled();
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
                getAllInheritedClasses(classNames, superClassName, project)
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


    fun getAllInheritedClassesForAllClassTypesInArray(
            result: MutableList<String>,
            baseClassNames: List<String>,
            project: Project) {
        for (baseClassName in baseClassNames) {
            getInheritedClasses(result, baseClassName, project)
        }
    }

    fun getInheritedClasses(
            result: MutableList<String>,
            baseClassName: String,
            project: Project) {
        if (baseClassName == ObjJClassType.CLASS) {
            result.add(UNDETERMINED)
        }
        if (result.contains(baseClassName)) {
            return
        }
        if (baseClassName == ObjJClassType.JSOBJECT && !result.contains(ObjJClassType.CPOBJECT)) {
            result.add(ObjJClassType.CPOBJECT)
        }
        for (inheritedClassName in ObjJPsiImplUtil.getAllInheritedClasses(baseClassName, project)) {
            if (/*ObjJClassType.isPrimitive(inheritedClassName) || */result.contains(inheritedClassName)) {
                continue
            }
            result.add(inheritedClassName)
        }
    }

    fun getInheritanceUpAndDown(className: String, project: Project): List<String> {
        val referencedAncestors = ArrayList<String>()
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

}