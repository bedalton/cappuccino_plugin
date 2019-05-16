package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.TYPE_SPLIT_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefFunctionStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStubParameter
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStubReturnType
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
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
        val moduleName = function.enclosingNamespace
        val functionName = function.functionName.text
        val parameters = function.propertiesList?.propertyList?.map { property ->
            JsTypeDefFunctionStubParameter(
                    name = property.propertyName.text,
                    types = property.propertyTypes.map { type -> type.text },
                    nullable = property.isNullable
            )
        } ?: listOf()
        val returnTypes = function.functionReturnType?.typeList?.map { it.text } ?: listOf()
        val returnType = JsTypeDefFunctionStubReturnType(returnTypes, function.isNullableReturnType)
        val isGlobal:Boolean = function.hasParentOfType(JsTypeDefFunctionDeclaration::class.java)
        return JsTypeDefFunctionStubImpl(parent, fileName, moduleName, functionName, parameters, returnType, isGlobal)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefFunctionStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.moduleName)
        stream.writeName(stub.functionName)
        val numProperties = stub.parameters.size
        stream.writeInt(numProperties)
        for(i in 0 until numProperties) {
            val parameter = stub.parameters[i]
            stream.writeName(parameter.name)
            stream.writeName(parameter.types.joinToString("|"))
            stream.writeBoolean(parameter.nullable)
        }
        stream.writeName(stub.returnType.types.joinToString("|"))
        stream.writeBoolean(stub.returnType.nullable)
        stream.writeBoolean(stub.global)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefFunctionStub {

        val fileName = stream.readName()?.string ?: ""
        val moduleName = stream.readNameString() ?: ""
        val functionName = stream.readNameString() ?: ""
        val parameters = mutableListOf<JsTypeDefFunctionStubParameter>()
        val numParameters = stream.readInt()
        for(i in 0 until numParameters) {
            val parameterName = stream.readNameString() ?: ""
            val types = (stream.readNameString() ?: "").split(TYPE_SPLIT_REGEX)
            val nullable = stream.readBoolean()
            parameters.add(JsTypeDefFunctionStubParameter(parameterName, types, nullable))
        }
        val returnTypeTypes = (stream.readNameString() ?: "").split(TYPE_SPLIT_REGEX)
        val returnTypeIsNullable = stream.readBoolean()
        val returnType = JsTypeDefFunctionStubReturnType(returnTypeTypes, returnTypeIsNullable)
        val global = stream.readBoolean()
        return JsTypeDefFunctionStubImpl(parent, fileName, moduleName, functionName, parameters, returnType, global)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefFunction)?.functionName?.text.isNotNullOrBlank()
    }
}
