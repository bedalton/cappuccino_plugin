package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions
import java.util.Collections

class ObjJMethodIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeader>() {

    protected override val indexedElementClass: Class<ObjJMethodHeader>
        get() = ObjJMethodHeader::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJMethodHeader> {
        return KEY
    }

    companion object {
        val KEY = IndexKeyUtil.createIndexKey(ObjJMethodIndex::class.java)
        val instance = ObjJMethodIndex()
        private val VERSION = 1
    }
}
