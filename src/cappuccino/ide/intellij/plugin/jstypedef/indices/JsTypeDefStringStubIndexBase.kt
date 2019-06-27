package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.ObjJStringStubIndexBase
import cappuccino.ide.intellij.plugin.jstypedef.stubs.JsTypeDefStubVersion
import com.intellij.psi.PsiElement

abstract class JsTypeDefStringStubIndexBase<ObjJElemT : PsiElement> : ObjJStringStubIndexBase<ObjJElemT>() {

    override fun getVersion(): Int {
        return JsTypeDefStubVersion.VERSION + VERSION
    }

    companion object {
        const val VERSION = 1
    }
}