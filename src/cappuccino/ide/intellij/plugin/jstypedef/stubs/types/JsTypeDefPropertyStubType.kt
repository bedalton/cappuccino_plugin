package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefPropertyImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefPropertyStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefPropertyStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefPropertyStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefPropertyStub, JsTypeDefPropertyImpl>(debugName, JsTypeDefPropertyImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefPropertyStub): JsTypeDefPropertyImpl {
        return JsTypeDefPropertyImpl(stub, this)
    }

    override fun createStub(property:JsTypeDefPropertyImpl, parent: StubElement<*>): JsTypeDefPropertyStub {
        val fileName = property.containingFile.name
        val enclosingNamespace = property.enclosingNamespace
        val propertyName = property.propertyName.text
        val typeList = InferenceResult(property.typeList.toJsTypeDefTypeListTypes(), property.isNullable)
        return JsTypeDefPropertyStubImpl(parent, fileName, enclosingNamespace, property.namespaceComponents, propertyName, typeList)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefPropertyStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.propertyName)
        stream.writeInferenceResult(stub.types)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefPropertyStub {

        val fileName = stream.readNameString() ?: ""
        val enclosingNamespace = stream.readNameString() ?: ""
        val propertyName = stream.readNameString() ?: "???"
        val types = stream.readInferenceResult() ?: INFERRED_ANY_TYPE
        return JsTypeDefPropertyStubImpl(parent, fileName, enclosingNamespace, enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX).plus(propertyName), propertyName, types)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefProperty)?.propertyName?.text.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefPropertyStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexProperty(stub, sink)
    }
}