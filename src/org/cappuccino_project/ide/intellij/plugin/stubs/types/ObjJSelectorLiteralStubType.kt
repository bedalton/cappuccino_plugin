package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJSelectorLiteralStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub

import java.io.IOException
import java.util.ArrayList

class ObjJSelectorLiteralStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJSelectorLiteralStub, ObjJSelectorLiteralImpl>(debugName, ObjJSelectorLiteralImpl::class.java, ObjJSelectorLiteralStub::class.java) {

    override fun createPsi(
            objJSelectorLiteralStub: ObjJSelectorLiteralStub): ObjJSelectorLiteralImpl {
        return ObjJSelectorLiteralImpl(objJSelectorLiteralStub, this)
    }

    override fun createStub(
            selectorLiteral: ObjJSelectorLiteralImpl, stubParent: StubElement<*>): ObjJSelectorLiteralStub {
        return ObjJSelectorLiteralStubImpl(stubParent, selectorLiteral.containingClassName, selectorLiteral.selectorStrings, selectorLiteral.shouldResolve())
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
        val shouldResolve = stream.readBoolean()
        return ObjJSelectorLiteralStubImpl(stubParent, containingClassName, selectorStrings, shouldResolve)
    }

    override fun indexStub(stub: ObjJSelectorLiteralStub, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexSelectorLiteral(stub, indexSink)
    }
}
