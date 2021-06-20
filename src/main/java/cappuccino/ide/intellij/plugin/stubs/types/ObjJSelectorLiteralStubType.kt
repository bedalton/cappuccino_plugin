package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJSelectorLiteralStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub
import cappuccino.ide.intellij.plugin.stubs.stucts.readSelectorStructList
import cappuccino.ide.intellij.plugin.stubs.stucts.writeSelectorStructList
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.*

class ObjJSelectorLiteralStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJSelectorLiteralStub, ObjJSelectorLiteralImpl>(debugName, ObjJSelectorLiteralImpl::class.java) {

    override fun createPsi(
            objJSelectorLiteralStub: ObjJSelectorLiteralStub): ObjJSelectorLiteralImpl {
        return ObjJSelectorLiteralImpl(objJSelectorLiteralStub, this)
    }

    override fun createStub(
            selectorLiteral: ObjJSelectorLiteralImpl, stubParent: StubElement<*>): ObjJSelectorLiteralStub {
        return ObjJSelectorLiteralStubImpl(
                parent = stubParent,
                containingClassName = selectorLiteral.containingClassName,
                selectorStrings = selectorLiteral.selectorStrings,
                selectorStructs = selectorLiteral.selectorStructs,
                shouldResolve = selectorLiteral.shouldResolve()
        )
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJSelectorLiteralStub,
            stream: StubOutputStream) {
        stream.writeName(stub.containingClassName)
        stream.writeInt(stub.selectorStrings.size)
        for (selector in stub.selectorStrings) {
            stream.writeName(selector)
        }
        stream.writeSelectorStructList(stub.selectorStructs)
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJSelectorLiteralStub {
        val containingClassName = StringRef.toString(stream.readName())
        val numSelectorStrings = stream.readInt()
        val selectorStrings = ArrayList<String>()
        for (i in 0 until numSelectorStrings) {
            selectorStrings.add(StringRef.toString(stream.readName()))
        }
        val selectorStructs = stream.readSelectorStructList()
        val shouldResolve = stream.readBoolean()
        return ObjJSelectorLiteralStubImpl(
                parent = stubParent,
                containingClassName = containingClassName,
                selectorStrings = selectorStrings,
                selectorStructs = selectorStructs,
                shouldResolve = shouldResolve
        )
    }

    override fun indexStub(stub: ObjJSelectorLiteralStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexSelectorLiteral(stub, sink)
    }
}
