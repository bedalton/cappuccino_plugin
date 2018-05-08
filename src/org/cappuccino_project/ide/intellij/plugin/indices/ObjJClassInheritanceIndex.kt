package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

import java.util.ArrayList


class ObjJClassInheritanceIndex private constructor() : ObjJStringStubIndexBase<ObjJClassDeclarationElement<*>>() {

    protected override val indexedElementClass: Class<ObjJClassDeclarationElement<*>>
        get() = ObjJClassDeclarationElement<*>::class.java

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    fun getChildClassesAsStrings(parentClassName: String, project: Project): List<String> {
        return getChildClassesRecursive(ArrayList(), parentClassName, project)
    }

    private fun getChildClassesRecursive(descendants: MutableList<String>, className: String, project: Project): List<String> {
        if (DumbService.isDumb(project)) {
            throw IndexNotReadyRuntimeException()
        }
        for (classDeclarationElement in get(className, project)) {
            val currentClassName = classDeclarationElement.classNameString
            if (descendants.contains(currentClassName)) {
                continue
            }
            descendants.add(currentClassName)
            getChildClassesRecursive(descendants, currentClassName, project)
        }
        return descendants
    }

    companion object {
        val instance = ObjJClassInheritanceIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement>(ObjJClassInheritanceIndex::class.java)
        private val VERSION = 1
    }
}
