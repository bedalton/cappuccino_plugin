package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefPropertyImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefPropertyStub
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
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
        val typeList = property.typeList
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefPropertyStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.enclosingNamespace)
        stream.writeName(stub.propertyName)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefPropertyStub {

        val fileName = stream.readNameString() ?: ""
        val enclosingNamespace =stream.readNameString() ?: ""
        val propertyName = stream.readNameString() ?: "???"
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefProperty)?.propertyName?.text.isNotNullOrBlank()
    }
}


val List<JsTypeDefTypeName>.arrayTypes() : List<JsTypeDefClassName>