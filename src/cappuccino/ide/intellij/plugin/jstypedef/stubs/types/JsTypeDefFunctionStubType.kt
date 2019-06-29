package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.INFERRED_VOID_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefFunctionStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.toStubParameter
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefFunctionStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefFunctionStub, JsTypeDefFunctionImpl>(debugName, JsTypeDefFunctionImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefFunctionStub): JsTypeDefFunctionImpl {
        return JsTypeDefFunctionImpl(stub, this)
    }

    override fun createStub(function:JsTypeDefFunctionImpl, parent: StubElement<*>): JsTypeDefFunctionStub {
        val fileName = function.containingFile.name
        val enclosingNamespace = function.enclosingNamespace
        val functionName = function.functionName.text
        val parameters = function.propertiesList?.properties?.map { property ->
            property.toStubParameter()
        }?.toList() ?: emptyList()
        val returnTypes = function.functionReturnType?.typeList?.toJsTypeDefTypeListTypes() ?: emptySet()
        val returnType = InferenceResult(returnTypes, function.isNullableReturnType)
        val isGlobal:Boolean = function.hasParentOfType(JsTypeDefFunctionDeclaration::class.java)
        val static:Boolean = !function.isStatic
        return JsTypeDefFunctionStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                functionName = functionName,
                parameters = parameters,
                returnType = returnType,
                global = isGlobal,
                static = static)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefFunctionStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.functionName)
        stream.writePropertiesList(stub.parameters)
        stream.writeInferenceResult(stub.returnType)
        stream.writeBoolean(stub.returnType.nullable)
        stream.writeBoolean(stub.global)
        stream.writeBoolean(stub.static || stub.global)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefFunctionStub {

        val fileName = stream.readName()?.string ?: ""
        val enclosingNamespaece = stream.readNameString() ?: ""
        val functionName = stream.readNameString() ?: ""
        val parameters = stream.readPropertiesList()
        val returnType = stream.readInferenceResult()
        val global = stream.readBoolean()
        val static = stream.readBoolean()
        return JsTypeDefFunctionStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespaece,
                functionName = functionName,
                parameters = parameters,
                returnType = returnType ?: INFERRED_VOID_TYPE,
                global = global,
                static = static || global
            )
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefFunction)?.functionName?.text.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefFunctionStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexFunction(stub, sink)
    }
}
