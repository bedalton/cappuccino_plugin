package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefPropertyImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
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
        val enclosingClass = property.getParentOfType(JsTypeDefClassDeclaration::class.java)?.className
        val propertyName = property.propertyNameString
        val typeList = InferenceResult(property.typeListTypes, property.isNullable)
        val static = property.parent is JsTypeDefVariableDeclaration
        return JsTypeDefPropertyStubImpl(parent, fileName, enclosingNamespace, enclosingClass, property.namespaceComponents, propertyName, typeList, static, property.completionModifier)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefPropertyStub,
            stream: StubOutputStream) {
        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.enclosingClass)
        stream.writeName(stub.propertyName)
        stream.writeInferenceResult(stub.types)
        stream.writeBoolean(stub.static)
        stream.writeName(stub.completionModifier.tag)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefPropertyStub {

        val fileName = stream.readName()?.string ?: ""
        val enclosingNamespace = stream.readName()?.string ?: ""
        val enclosingClass = stream.readName()?.string
        val propertyName = stream.readName()?.string ?: "???"
        val types = stream.readInferenceResult() ?: INFERRED_ANY_TYPE
        val static = stream.readBoolean()
        val completionModifier = CompletionModifier.fromTag(stream.readName()?.string!!)
        return JsTypeDefPropertyStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                enclosingClass = enclosingClass,
                namespaceComponents = enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX).plus(propertyName),
                propertyName = propertyName,
                types = types,
                static = static,
                completionModifier = completionModifier
        )
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefProperty)?.propertyName?.text.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefPropertyStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexProperty(stub, sink)
    }
}