package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionNameImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionCallStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ObjJFunctionCallStubImpl(
        parent:StubElement<*>,
        override val functionName:String?,
        override val indexInQualifiedReference:Int
) : StubBase<ObjJFunctionNameImpl>(parent, ObjJStubTypes.FUNCTION_CALL), ObjJFunctionCallStub {
}