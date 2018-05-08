package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

import java.util.ArrayList

/**
 * Meant to find parent classes of a class
 */
class ObjJParentClassesByChildIndex private constructor() : StringStubIndexExtension<ObjJClassDeclarationElement<*>>() {

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    @JvmOverloads
    fun getParentClasses(parentClassName: String, project: Project, scope: GlobalSearchScope? = null): List<ObjJClassDeclarationElement<*>> {
        //ProgressIndicatorProvider.checkCanceled();
        return ArrayList(StubIndex.getElements<String, ObjJClassDeclarationElement>(KEY, parentClassName, project, scope, ObjJClassDeclarationElement<*>::class.java))
    }

    companion object {
        val instance = ObjJParentClassesByChildIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement>(ObjJParentClassesByChildIndex::class.java)
        private val VERSION = 1
    }
}
