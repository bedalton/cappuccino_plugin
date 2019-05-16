package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefModuleNameImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefModuleNameStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefModuleNameStubImpl(parent:StubElement<*>, override val fileName:String, override val enclosingNamespaceComponents: List<String>, override val moduleName: String) : JsTypeDefStubBaseImpl<JsTypeDefModuleNameImpl>(parent, JsTypeDefStubTypes.JS_MODULE_NAME), JsTypeDefModuleNameStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}