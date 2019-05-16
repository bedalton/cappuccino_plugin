package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefModuleImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefModuleStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefModuleStubImpl(parent:StubElement<*>, override val fileName:String, override val enclosingNamespaceComponents: List<String>, override val moduleName: String) : JsTypeDefStubBaseImpl<JsTypeDefModuleImpl>(parent, JsTypeDefStubTypes.JS_MODULE), JsTypeDefModuleStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}