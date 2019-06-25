package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.psi.StubBuilder
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub
import com.intellij.psi.stubs.*

import java.io.IOException
import java.util.ArrayList

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
        stream.writeInt(stub.imports.size)
        for (importStatement in stub.imports) {
            stream.writeImportInfo(importStatement)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>?): ObjJFileStub {
        super.deserialize(stream, parentStub)
        val fileName = StringRef.toString(stream.readName())
        val numImports = stream.readInt()
        val imports = ArrayList<ObjJImportInfoStub>()
        for (i in 0 until numImports) {
            imports.add(stream.readImportInfo())
        }
        return ObjJFileStubImpl(null, fileName, imports)
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
