package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
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
        val enclosingClass = function.getParentOfType(JsTypeDefClassDeclaration::class.java)?.className
        val functionName = function.functionName.text
        val parameters = function.argumentsList?.arguments?.map { argument ->
            argument.toStubParameter()
        }?.toList() ?: emptyList()
        val returnTypes = function.getReturnTypes(createTag())?.types.orEmpty()
        val returnType = InferenceResult(returnTypes, function.isNullableReturnType)
        val genericsKeys = function.genericsKeys
        val isGlobal:Boolean = function.hasParentOfType(JsTypeDefFunctionDeclaration::class.java)
        val static:Boolean = function.isStatic
        val completionModifier = function.completionModifier
        return JsTypeDefFunctionStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                enclosingClass = enclosingClass,
                functionName = functionName,
                parameters = parameters,
                returnType = returnType,
                genericsKeys = genericsKeys,
                global = isGlobal,
                static = static,
                completionModifier = completionModifier)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefFunctionStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.enclosingClass)
        stream.writeName(stub.functionName)
        stream.writeFunctionArgumentsList(stub.parameters)
        stream.writeInferenceResult(stub.returnType)
        stream.writeTypeList(stub.genericsKeys ?: emptySet())
        stream.writeBoolean(stub.global)
        stream.writeBoolean(stub.static || stub.global)
        stream.writeName(stub.completionModifier.tag)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefFunctionStub {

        val fileName = stream.readName()?.string ?: ""
        val enclosingNamespace = stream.readName()?.string ?: ""
        val enclosingClass = stream.readName()?.string
        val functionName = stream.readName()?.string ?: ""
        val parameters = stream.readFunctionArgumentsList()
        val returnType = stream.readInferenceResult()
        val genericKeys = stream.readTypesList().ifEmpty { null }?.mapNotNull { it as? JsTypeListType.JsTypeListGenericType }?.toSet()
        val global = stream.readBoolean()
        val static = stream.readBoolean()
        val completionModifier = CompletionModifier.fromTag(stream.readName()?.string!!)
        return JsTypeDefFunctionStubImpl(
                parent = parent,
                fileName = fileName,
                enclosingNamespace = enclosingNamespace,
                enclosingClass = enclosingClass,
                functionName = functionName,
                parameters = parameters,
                returnType = returnType!!,
                genericsKeys = genericKeys,
                global = global,
                static = static,
                completionModifier = completionModifier
            )
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefFunction)?.functionName?.text.isNotNullOrBlank()
    }

    override fun indexStub(stub: JsTypeDefFunctionStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexFunction(stub, sink)
    }
}
