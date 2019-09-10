package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.INFERRED_VOID_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefVariableDeclarationStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefVariableDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefVariableDeclarationStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefVariableDeclarationStub, JsTypeDefVariableDeclarationImpl>(debugName, JsTypeDefVariableDeclarationImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefVariableDeclarationStub): JsTypeDefVariableDeclarationImpl {
        return JsTypeDefVariableDeclarationImpl(stub, this)
    }

    override fun createStub(declaration:JsTypeDefVariableDeclarationImpl, parent: StubElement<*>): JsTypeDefVariableDeclarationStub {
        val fileName = declaration.containingFile.name
        val enclosingNamespace = declaration.enclosingNamespace
        val enclosingNamespaceComponents = declaration.enclosingNamespaceComponents
        val variableName = declaration.property?.propertyNameString.orEmpty()
        val types = InferenceResult(declaration.property?.propertyTypes.orEmpty().toJsTypeDefTypeListTypes(), declaration.property?.isNullable.orTrue())
        val readOnly = declaration.readonly != null
        val comment = null
        val default = null
        val completionModifier = declaration.completionModifier
        return JsTypeDefVariableDeclarationStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                enclosingNamespaceComponents = enclosingNamespaceComponents,
                variableName = variableName,
                types = types,
                readonly = readOnly,
                comment = comment,
                default = default,
                completionModifier = completionModifier
        )
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefVariableDeclarationStub,
            stream: StubOutputStream) {
        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.variableName)
        stream.writeInferenceResult(stub.types)
        stream.writeBoolean(stub.readonly)
        stream.writeName(stub.comment)
        stream.writeName(stub.default)
        stream.writeName(stub.completionModifier.tag)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefVariableDeclarationStub {
        val fileName = stream.readName()?.string ?: ""
        val enclosingNamespace = stream.readName()?.string ?: ""
        val enclosingNamespaceComponents = enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX)
        val variableName:String = stream.readName()?.string ?: ""
        val types: InferenceResult = stream.readInferenceResult() ?: INFERRED_VOID_TYPE
        val readonly: Boolean = stream.readBoolean()
        val comment: String? = stream.readName()?.string
        val default: String? = stream.readName()?.string
        val completionModifier = CompletionModifier.fromTag(stream.readName()?.string!!)
        return JsTypeDefVariableDeclarationStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                enclosingNamespaceComponents = enclosingNamespaceComponents,
                variableName = variableName,
                readonly = readonly,
                types = types,
                comment = comment,
                default = default,
                completionModifier = completionModifier
        )
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefVariableDeclaration)?.property?.propertyName?.text.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefVariableDeclarationStub, sink: IndexSink) {
        //ServiceManager.getService(StubIndexService::class.java).indexVariableDeclaration(stub, sink)
    }
}
