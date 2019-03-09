package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJTypeDefImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJTypeDefStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJTypeDefStub

import java.io.IOException

class ObjJTypeDefStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJTypeDefStub, ObjJTypeDefImpl>(debugName, ObjJTypeDefImpl::class.java) {

    override fun createPsi(
            objJTypeDefStub: ObjJTypeDefStub): ObjJTypeDefImpl {
        return ObjJTypeDefImpl(objJTypeDefStub, ObjJStubTypes.TYPE_DEF)
    }

    override fun createStub(
            element: ObjJTypeDefImpl, parentStub: StubElement<*>): ObjJTypeDefStub {
        val className = element.classNameString
        return ObjJTypeDefStubImpl(parentStub, className)
    }


    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJTypeDefStub,
            stream: StubOutputStream) {
        val className = stub.className
        stream.writeName(className)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJTypeDefStub {
        val className = StringRef.toString(stream.readName())
        return ObjJTypeDefStubImpl(parentStub, className)
    }

    override fun indexStub(stub: ObjJTypeDefStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexTypeDef(stub, sink)
    }
}
