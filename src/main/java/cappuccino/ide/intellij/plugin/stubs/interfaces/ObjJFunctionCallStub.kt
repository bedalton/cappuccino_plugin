package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionNameImpl
import com.intellij.psi.stubs.StubElement

interface ObjJFunctionCallStub : StubElement<ObjJFunctionNameImpl> {
    val functionName:String?
    val indexInQualifiedReference:Int
}
