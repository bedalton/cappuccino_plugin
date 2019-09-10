package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefTypeMapEntry
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefTypeMapElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefTypeMapStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeMapStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefTypeMapStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefTypeMapStub, JsTypeDefTypeMapElementImpl>(debugName, JsTypeDefTypeMapElementImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefTypeMapStub): JsTypeDefTypeMapElementImpl {
        return JsTypeDefTypeMapElementImpl(stub, this)
    }

    override fun createStub(typeMap:JsTypeDefTypeMapElementImpl, parent: StubElement<*>): JsTypeDefTypeMapStub {
        val fileName = typeMap.containingFile.name
        val mapName = typeMap.mapName ?: ""
        val values = typeMap.keyValuePairList.map {
            JsTypeDefTypeMapEntry(it.key, it.typesList.toInferenceResult())
        }
        return JsTypeDefTypeMapStubImpl(parent, fileName, mapName, values)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefTypeMapStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.mapName)
        stream.writeInt(stub.values.size)
        for (value in stub.values) {
            stream.writeName(value.key)
            stream.writeInferenceResult(value.types)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefTypeMapStub {

        val fileName = stream.readName()?.string ?: ""
        val mapName = stream.readName()?.string ?: "???"
        val numValues = stream.readInt()
        val values:List<JsTypeDefTypeMapEntry> = (0 until numValues).mapNotNull {
            val key = stream.readName()?.string
            val types = stream.readInferenceResult()
            if (key == null || types == null)
                return@mapNotNull null
            return@mapNotNull JsTypeDefTypeMapEntry(key, types)
        }
        return JsTypeDefTypeMapStubImpl(parent, fileName, mapName, values)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node as? JsTypeDefTypeMapElement)?.mapName.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefTypeMapStub, sink: IndexSink) {
        //ServiceManager.getService(StubIndexService::class.java).indexTypeMap(stub, sink)
    }
}
