package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefKeyListImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefKeysListStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefKeyListStubImpl(parent:StubElement<*>, override val fileName:String, override val listName: String, override val values: List<String>) : JsTypeDefStubBaseImpl<JsTypeDefKeyListImpl>(parent = parent, elementType = JsTypeDefStubTypes.JS_KEY_LIST), JsTypeDefKeysListStub