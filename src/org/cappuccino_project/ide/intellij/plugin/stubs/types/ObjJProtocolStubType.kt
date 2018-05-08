package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJProtocolDeclarationStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub

import java.io.IOException
import java.util.ArrayList

class ObjJProtocolStubType internal constructor(
        debugName: String) : ObjJClassDeclarationStubType<ObjJProtocolDeclarationStub, ObjJProtocolDeclarationImpl>(debugName, ObjJProtocolDeclarationImpl::class.java, ObjJProtocolDeclarationStub::class.java) {

    override fun createPsi(
            objJProtocolDeclarationStub: ObjJProtocolDeclarationStub): ObjJProtocolDeclarationImpl {
        return ObjJProtocolDeclarationImpl(objJProtocolDeclarationStub)
    }

    override fun createStub(
            element: ObjJProtocolDeclarationImpl, parentStub: StubElement<*>): ObjJProtocolDeclarationStub {
        val className = if (element.className != null) element.className!!.text else ""
        val protocols = element.inheritedProtocols
        val shouldResolve = shouldResolve(element.node)
        return ObjJProtocolDeclarationStubImpl(parentStub, className, protocols, shouldResolve)
    }


    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJProtocolDeclarationStub,
            stream: StubOutputStream) {
        val className = stub.className
        stream.writeName(className)

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
            stream: StubInputStream, parentStub: StubElement<*>): ObjJProtocolDeclarationStub {
        val className = StringRef.toString(stream.readName())
        val numProtocols = stream.readInt()
        val inheritedProtocols = ArrayList<String>()
        for (i in 0 until numProtocols) {
            inheritedProtocols.add(StringRef.toString(stream.readName()))
        }
        val shouldResolve = stream.readBoolean()
        return ObjJProtocolDeclarationStubImpl(parentStub, className, inheritedProtocols, shouldResolve)
    }

    override fun indexStub(stub: ObjJProtocolDeclarationStub, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexClassDeclaration(stub, indexSink)
    }
}
