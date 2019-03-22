package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImplementationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException
import java.util.ArrayList

class ObjJImplementationStubType internal constructor(
        debugName: String) : ObjJClassDeclarationStubType<ObjJImplementationStub, ObjJImplementationDeclarationImpl>(debugName, ObjJImplementationDeclarationImpl::class.java) {

    override fun createPsi(
            objJImplementationStub: ObjJImplementationStub): ObjJImplementationDeclarationImpl {
        return ObjJImplementationDeclarationImpl(objJImplementationStub, this)
    }

    override fun createStub(
            element: ObjJImplementationDeclarationImpl, parentStub: StubElement<*>): ObjJImplementationStub {
        val className = element.getClassName()?.text ?: ""
        val superClassName = element.superClassName
        val protocols = element.getInheritedProtocols()
        val categoryName = if (element.categoryName != null) element.categoryName!!.text else null
        return ObjJImplementationStubImpl(parentStub, className, superClassName, categoryName, protocols, shouldResolve(element.node))
    }


    override fun getExternalId(): String {
        return "objj." + toString()
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJImplementationStub,
            stream: StubOutputStream) {
        stream.writeName(stub.className)
        stream.writeName(Strings.notNull(stub.superClassName, ObjJClassType.UNDEF_CLASS_NAME))
        stream.writeBoolean(stub.isCategory)
        if (stub.isCategory) {
            stream.writeName(stub.categoryName)
        }
        //protocols
        val protocols = stub.inheritedProtocols
        val numProtocols = protocols.size
        stream.writeInt(numProtocols)
        // Write protocol names
        for (protocol in protocols) {
            stream.writeName(protocol)
        }
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJImplementationStub {
        val className = StringRef.toString(stream.readName()) ?: ""
        val superClassName = StringRef.toString(stream.readName())
        val categoryName = if (stream.readBoolean()) StringRef.toString(stream.readName()) else null
        val numProtocols = stream.readInt()
        val inheritedProtocols = ArrayList<String>()
        for (i in 0 until numProtocols) {
            inheritedProtocols.add(StringRef.toString(stream.readName()))
        }
        val shouldResolve = stream.readBoolean()
        return ObjJImplementationStubImpl(parentStub, className, if (superClassName != ObjJClassType.UNDEF_CLASS_NAME) superClassName else null, categoryName, inheritedProtocols, shouldResolve)
    }

    override fun indexStub(
            stub: ObjJImplementationStub,
            sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexClassDeclaration(stub, sink)
    }
}
