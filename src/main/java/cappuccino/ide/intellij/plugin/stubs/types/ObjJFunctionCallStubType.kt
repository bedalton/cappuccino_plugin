package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.stubs.readNameAsString
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionCallImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFunctionCallStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionCallStub
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef

import java.io.IOException

class ObjJFunctionCallStubType(
        debugName: String) : ObjJStubElementType<ObjJFunctionCallStub, ObjJFunctionCallImpl>(debugName, ObjJFunctionCallImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionCallStub): ObjJFunctionCallImpl {
        return ObjJFunctionCallImpl(stub, this)
    }

    override fun createStub(
            functionCall: ObjJFunctionCallImpl, parent: StubElement<*>): ObjJFunctionCallStub {
        return ObjJFunctionCallStubImpl(parent, functionCall.functionNameString, functionCall.indexInQualifiedReference)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJFunctionCallStub,
            stream: StubOutputStream) {
        stream.writeName(stub.functionName)
        stream.writeInt(stub.indexInQualifiedReference)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): ObjJFunctionCallStub {
        val functionName:String? = stream.readNameAsString()
        val indexInQualifiedName = stream.readInt()
        return ObjJFunctionCallStubImpl(parent, functionName, indexInQualifiedName)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        val functionCall = (node?.psi as? ObjJFunctionCall)
                ?: return false
        return functionCall.functionNameString.isNotNullOrBlank()
    }
}
