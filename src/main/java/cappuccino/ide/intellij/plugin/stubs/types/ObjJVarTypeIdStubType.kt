package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableTypeIdImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJVariableTypeIdStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableTypeIdStub
import cappuccino.ide.intellij.plugin.utils.Strings
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException

class ObjJVariableTypeIdStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJVariableTypeIdStub, ObjJVariableTypeIdImpl>(debugName, ObjJVariableTypeIdImpl::class.java) {


    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJVariableTypeIdStub,
            stream: StubOutputStream) {
        stream.writeName(stub.idType)
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJVariableTypeIdStub {
        val idType = Strings.notNull(StringRef.toString(stream.readName()), "id")
        val shouldResolve = stream.readBoolean()
        return ObjJVariableTypeIdStubImpl(stubParent, idType, shouldResolve)
    }

    override fun indexStub(stub: ObjJVariableTypeIdStub, sink: IndexSink) {
        //ServiceManager.getService(StubIndexService.class).indexVariableTypeId(stub, indexSink);
    }

    override fun createPsi(
            stub: ObjJVariableTypeIdStub): ObjJVariableTypeIdImpl {
        return ObjJVariableTypeIdImpl(stub, this)
    }

    override fun createStub(
            variableTypeId: ObjJVariableTypeIdImpl, stubParent: StubElement<*>): ObjJVariableTypeIdStub {
        return ObjJVariableTypeIdStubImpl(stubParent, variableTypeId.idType, shouldResolve(variableTypeId.node))
    }
}
