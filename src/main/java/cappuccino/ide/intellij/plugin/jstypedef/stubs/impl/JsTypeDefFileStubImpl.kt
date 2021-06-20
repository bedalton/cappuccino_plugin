package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFileStub
import com.intellij.psi.stubs.PsiFileStubImpl

class JsTypeDefFileStubImpl(file: JsTypeDefFile?, override val fileName: String) : PsiFileStubImpl<JsTypeDefFile>(file), JsTypeDefFileStub {

}