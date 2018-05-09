package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

import java.util.ArrayList

class ObjJParentClassChildrenIndex private constructor() : StringStubIndexExtension<ObjJClassDeclarationElement<*>>() {

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    fun getChildClassesAsStrings(parentClassName: String, project: Project): List<String> {
        //ProgressIndicatorProvider.checkCanceled();
        val childClasses = ArrayList<String>()
        val childClassElements = getChildClasses(parentClassName, project)
        for (childClass in childClassElements) {
            //ProgressIndicatorProvider.checkCanceled();
            val childClassName = if (childClass.stub != null) childClass.stub!!.className else childClass.classNameString
            if (childClassName.isEmpty()) {
                continue
            }
            childClasses.add(childClassName)
        }
        return childClasses
    }

    @JvmOverloads
    fun getChildClasses(parentClassName: String, project: Project, scope: GlobalSearchScope? = null): List<ObjJClassDeclarationElement<*>> {
        ProgressIndicatorProvider.checkCanceled()
        return ArrayList(StubIndex.getElements<String, ObjJClassDeclarationElement<*>>(KEY, parentClassName, project, scope, ObjJClassDeclarationElement::class.java))
    }

    companion object {
        val instance = ObjJParentClassChildrenIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement<*>>(ObjJParentClassChildrenIndex::class.java)
        private val VERSION = 1
    }
}
