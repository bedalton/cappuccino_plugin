package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

object ObjJClassTypePsiUtil {

    /**
     * Determines whether this class name references a defined class
     * Also takes into account @typedef statements
     */
    fun isValidClass(className:ObjJClassName) : Boolean? {
        val classNameString:String = className.text ?: return false
        // Is primitive type, do not continue check
        if (classNameString in ObjJClassType.ADDITIONAL_PREDEFINED_CLASSES || classNameString.contains("signed"))
            return true
        if (ObjJCommentEvaluatorUtil.isIgnored(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS))
            return true
        val project:Project = className.project
        if (DumbService.isDumb(project))
            return null
        return ObjJClassDeclarationsIndex.instance.containsKey(classNameString, project) ||
                ObjJTypeDefIndex.instance.containsKey(classNameString, project) ||
                JsTypeDefClassesByNamespaceIndex.instance.containsKey(classNameString, project)
    }

    /**
     * Determines whether this class name string references a defined class
     * Also takes into account @typedef statements
     */
    fun isValidClass(classNameString:String, project: Project) : Boolean? {
        // Is primitive type, do not continue check
        if (classNameString in ObjJClassType.ADDITIONAL_PREDEFINED_CLASSES || classNameString.contains("signed"))
            return true
        if (DumbService.isDumb(project))
            return null
        return classNameString in ObjJClassDeclarationsIndex.instance.getAllKeys(project) ||
                classNameString in ObjJTypeDefIndex.instance.getAllKeys(project) ||
                JsTypeDefClassesByNamespaceIndex.instance.containsKey(classNameString, project)
    }
}