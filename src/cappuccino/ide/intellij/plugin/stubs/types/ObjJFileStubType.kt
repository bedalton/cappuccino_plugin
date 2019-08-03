package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import com.intellij.psi.StubBuilder
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.*

import java.io.IOException

class ObjJFileStubType : IStubFileElementType<ObjJFileStub>(NAME, ObjJLanguage.instance) {

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
        super.serialize(stub, stream)
        stream.writeName(stub.fileName)
        stream.writeName(stub.framework)
        stream.writeInt(stub.imports.size)
        for (importStatement in stub.imports) {
            stream.writeImportInfo(importStatement)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>?): ObjJFileStub {
        super.deserialize(stream, parentStub)
        val fileName = StringRef.toString(stream.readName())
        val frameworkName = stream.readNameString()
        val numImports = stream.readInt()
        val imports = mutableListOf<ObjJImportInfoStub>()
        for (i in 0 until numImports) {
            imports.add(stream.readImportInfo())
        }
        return ObjJFileStubImpl(null, fileName, frameworkName, imports)
    }

    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexFile(stub as? ObjJFileStub, sink)
    }

    companion object {

        private const val NAME = "objj.FILE"
    }
}

internal fun StubInputStream.readImportInfo() : ObjJImportInfoStub {
    val frameworkName = readNameString()
    val fileName = readNameString()
    return ObjJImportInfoStub(frameworkName, fileName)
}

internal fun StubOutputStream.writeImportInfo(stub:ObjJImportInfoStub) {
    val frameworkName = stub.framework
    val fileName = stub.fileName
    writeName(frameworkName)
    writeName(fileName)

}
