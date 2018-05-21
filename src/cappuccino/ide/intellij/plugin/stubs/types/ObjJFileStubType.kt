package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.psi.StubBuilder
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub
import com.intellij.psi.stubs.*

import java.io.IOException
import java.util.ArrayList

class ObjJFileStubType() : IStubFileElementType<ObjJFileStub>(NAME, ObjJLanguage.instance) {

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

    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        StubIndexService.instance.indexFile(stub, sink)
    }

    companion object {

        private val NAME = "objj.FILE"
    }
}
