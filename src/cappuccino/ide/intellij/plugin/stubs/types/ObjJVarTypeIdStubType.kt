package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJVarTypeIdStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJVarTypeIdStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJVarTypeIdStub, ObjJVarTypeIdImpl>(debugName, ObjJVarTypeIdImpl::class.java) {


    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJVarTypeIdStub,
            stream: StubOutputStream) {
        stream.writeName(stub.idType)
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJVarTypeIdStub {
        val idType = Strings.notNull(StringRef.toString(stream.readName()), "id")
        val shouldResolve = stream.readBoolean()
        return ObjJVarTypeIdStubImpl(stubParent, idType, shouldResolve)
    }

    override fun indexStub(stub: ObjJVarTypeIdStub, sink: IndexSink) {
        //ServiceManager.getService(StubIndexService.class).indexVarTypeId(stub, indexSink);
    }

    override fun createPsi(
            stub: ObjJVarTypeIdStub): ObjJVarTypeIdImpl {
        return ObjJVarTypeIdImpl(stub, this)
    }

    override fun createStub(
            varTypeId: ObjJVarTypeIdImpl, stubParent: StubElement<*>): ObjJVarTypeIdStub {
        return ObjJVarTypeIdStubImpl(stubParent, varTypeId.idType, shouldResolve(varTypeId.node))
    }
}
