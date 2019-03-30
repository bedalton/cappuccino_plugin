package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJClassNameImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJClassNameStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassNameStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef

import java.io.IOException

class ObjJClassNameStubType(
        debugName: String) : ObjJStubElementType<ObjJClassNameStub, ObjJClassNameImpl>(debugName, ObjJClassNameImpl::class.java) {

    override fun createPsi(
            stub: ObjJClassNameStub): ObjJClassNameImpl {
        return ObjJClassNameImpl(stub, ObjJStubTypes.CLASS_NAME)
    }

    override fun createStub(
            className: ObjJClassNameImpl, parent: StubElement<*>): ObjJClassNameStub {
        return ObjJClassNameStubImpl(parent, className.text)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJClassNameStub,
            stream: StubOutputStream) {
        stream.writeName(stub.getClassName())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): ObjJClassNameStub {
        val className:StringRef = stream.readName() ?: StringRef.fromString("")
        return ObjJClassNameStubImpl(parent,className)
    }
}
