package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions

import java.util.ArrayList

class ObjJSelectorInferredMethodIndex : ObjJMethodHeaderDeclarationsIndexBase<ObjJSelectorLiteral>() {

    protected override val indexedElementClass: Class<ObjJSelectorLiteral>
        get() = ObjJSelectorLiteral::class.java

    override fun getKey(): StubIndexKey<String, ObjJSelectorLiteral> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {
        val instance = ObjJSelectorInferredMethodIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJSelectorInferredMethodIndex::class.java)
        private val VERSION = 1
    }
}
