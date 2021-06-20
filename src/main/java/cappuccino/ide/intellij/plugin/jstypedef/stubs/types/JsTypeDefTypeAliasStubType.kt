package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.INFERRED_VOID_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeAlias
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefTypeAliasImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefTypeAliasStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeAliasStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefTypeAliasStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefTypeAliasStub, JsTypeDefTypeAliasImpl>(debugName, JsTypeDefTypeAliasImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefTypeAliasStub): JsTypeDefTypeAliasImpl {
        return JsTypeDefTypeAliasImpl(stub, this)
    }

    override fun createStub(declaration:JsTypeDefTypeAliasImpl, parent: StubElement<*>): JsTypeDefTypeAliasStub {
        val fileName = declaration.containingFile.name
        val typeName = declaration.typeNameString
        val types = declaration.typesList
        val comment = null
        return JsTypeDefTypeAliasStubImpl(
                parent = parent,
                fileName = fileName,
                typeName = typeName,
                types = types,
                comment = comment
        )
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefTypeAliasStub,
            stream: StubOutputStream) {
        stream.writeName(stub.fileName)
        stream.writeName(stub.typeName)
        stream.writeInferenceResult(stub.types)
        stream.writeName(stub.comment)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefTypeAliasStub {
        val fileName = stream.readName()?.string ?: ""
        val typeName = stream.readName()?.string ?: ""
        val types: InferenceResult = stream.readInferenceResult() ?: INFERRED_VOID_TYPE
        val comment: String? = stream.readName()?.string
        return JsTypeDefTypeAliasStubImpl(
                parent = parent,
                fileName = fileName,
                typeName = typeName,
                types = types,
                comment = comment
        )
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefTypeAlias)?.typeNameString.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefTypeAliasStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexTypeAlias(stub, sink)
    }
}
