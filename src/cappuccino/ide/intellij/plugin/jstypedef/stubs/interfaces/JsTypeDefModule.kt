package cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsModuleName
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsModuleImpl
import com.intellij.psi.stubs.StubElement

interface JsTypeDefModule : StubElement<JsModuleImpl> {
    val namespaceComponents:List<String>
    val moduleName:String
}