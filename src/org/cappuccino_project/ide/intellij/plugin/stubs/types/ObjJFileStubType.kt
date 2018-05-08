package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFileStub

import java.io.IOException
import java.util.ArrayList

class ObjJFileStubType : IStubFileElementType<ObjJFileStub> {
    constructor() : super(NAME, ObjJLanguage.INSTANCE) {}

    constructor(
            debugName: String) : super(debugName, ObjJLanguage.INSTANCE) {
    }

    override fun getBuilder(): StubBuilder {
        return ObjJFileStubBuilder()
    }

    override fun getStubVersion(): Int {
        return ObjJStubVersions.SOURCE_STUB_VERSION
    }

    override fun getExternalId(): String {
        return NAME
    }

    @Throws(IOException::class)
    override fun serialize(stub: ObjJFileStub, stream: StubOutputStream) {
        stream.writeName(stub.fileName)
        stream.writeInt(stub.imports.size)
        for (importStatement in stub.imports) {
            stream.writeName(importStatement)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>?): ObjJFileStub {
        val fileName = StringRef.toString(stream.readName())
        val numImports = stream.readInt()
        val imports = ArrayList<String>()
        for (i in 0 until numImports) {
            imports.add(StringRef.toString(stream.readName()))
        }
        return ObjJFileStubImpl(null, fileName, imports)
    }

    override fun indexStub(stub: ObjJFileStub, sink: IndexSink) {
        StubIndexService.instance.indexFile(stub, sink)
    }

    companion object {

        private val NAME = "objj.FILE"
    }
}
