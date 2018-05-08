package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJImplementationStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import org.cappuccino_project.ide.intellij.plugin.utils.Strings

import java.io.IOException
import java.util.ArrayList

class ObjJImplementationStubType internal constructor(
        debugName: String) : ObjJClassDeclarationStubType<ObjJImplementationStub, ObjJImplementationDeclarationImpl>(debugName, ObjJImplementationDeclarationImpl::class.java, ObjJImplementationStub::class.java) {

    override fun createPsi(
            objJImplementationStub: ObjJImplementationStub): ObjJImplementationDeclarationImpl {
        return ObjJImplementationDeclarationImpl(objJImplementationStub, this)
    }

    override fun createStub(
            element: ObjJImplementationDeclarationImpl, parentStub: StubElement<*>): ObjJImplementationStub {
        val className = if (element.className != null) element.className!!.text else ""
        val superClassName = if (element.superClass != null) element.superClass!!.text else null
        val protocols = element.inheritedProtocols
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
        val className = StringRef.toString(stream.readName())
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
            objJImplementationStub: ObjJImplementationStub,
            indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexClassDeclaration(objJImplementationStub, indexSink)
    }
}
