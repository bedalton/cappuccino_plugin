package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions

import java.util.ArrayList

class ObjJInstanceVariablesByClassIndex : ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration>() {

    protected override val indexedElementClass: Class<ObjJInstanceVariableDeclaration>
        get() = ObjJInstanceVariableDeclaration::class.java

    override fun getKey(): StubIndexKey<String, ObjJInstanceVariableDeclaration> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    fun getInstanceVariableNames(className: String, project: Project): List<String> {
        val out = ArrayList<String>()
        for (variableDeclaration in get(className, project)) {
            val variableName = if (variableDeclaration.stub != null) variableDeclaration.stub.variableName else if (variableDeclaration.variableName != null) variableDeclaration.variableName!!.text else null
            if (variableName != null) {
                out.add(variableName)
            }
        }
        return out
    }

    companion object {

        val instance = ObjJInstanceVariablesByClassIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJInstanceVariablesByClassIndex::class.java)
        private val VERSION = 1
    }
}
