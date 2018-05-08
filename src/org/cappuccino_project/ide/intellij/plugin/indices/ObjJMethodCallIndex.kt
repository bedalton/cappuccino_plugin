package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall

class ObjJMethodCallIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodCall>() {

    protected override val indexedElementClass: Class<ObjJMethodCall>
        get() = ObjJMethodCall::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodCall> {
        return KEY
    }

    override fun getVersion(): Int {
        return ObjJIndexService.INDEX_VERSION + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey(ObjJMethodCallIndex::class.java)
        val instance = ObjJMethodCallIndex()
        private val VERSION = 1
    }
}
