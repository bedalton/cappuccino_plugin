package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readNameAsString
import cappuccino.ide.intellij.plugin.psi.impl.ObjJPropertyNameImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPropertyNameStub
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPropertyNameStubImpl
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class ObjJPropertyNameStubType(debugName:String) : ObjJStubElementType<ObjJPropertyNameStub, ObjJPropertyNameImpl>(debugName, ObjJPropertyNameImpl::class.java) {

    override fun createPsi(element: ObjJPropertyNameStub): ObjJPropertyNameImpl {
        return ObjJPropertyNameImpl(element, this)
    }

    override fun serialize(stub: ObjJPropertyNameStub, stream: StubOutputStream) {
        stream.writeName(stub.key)
        stream.writeName(stub.namespacedName)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJPropertyNameStub {
        val key = stream.readNameAsString() ?: "???"
        val namespacedName = stream.readNameAsString() ?: key
        return ObjJPropertyNameStubImpl(parent, key, namespacedName)
    }

    override fun createStub(element: ObjJPropertyNameImpl, parent: StubElement<*>): ObjJPropertyNameStub {
        val key = element.key
        val namespacedName = element.namespacedName
        return ObjJPropertyNameStubImpl(parent, key, namespacedName)
    }

    override fun indexStub(stub: ObjJPropertyNameStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexPropertyName(stub, sink)
    }

}