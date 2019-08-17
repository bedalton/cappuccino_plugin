package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefKeyList
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefKeyListImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefKeyListStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefKeysListStub
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefKeysListStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefKeysListStub, JsTypeDefKeyListImpl>(debugName, JsTypeDefKeyListImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefKeysListStub): JsTypeDefKeyListImpl {
        return JsTypeDefKeyListImpl(stub, this)
    }

    override fun createStub(keysList:JsTypeDefKeyListImpl, parent: StubElement<*>): JsTypeDefKeysListStub {
        val fileName = keysList.containingFile.name
        val keyListName = keysList.keyName?.text ?: ""
        return JsTypeDefKeyListStubImpl(parent, fileName, keyListName, keysList.stringLiteralList.map { it.stringValue })
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefKeysListStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.listName)
        val namespaceKeys = stub.values
        stream.writeInt(namespaceKeys.size)
        for (component in namespaceKeys)
            stream.writeName(component)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefKeysListStub {

        val fileName = stream.readName()?.string ?: ""
        val keyListName = stream.readName()?.string ?: ""
        val numValues = stream.readInt()
        val values = (0 until numValues).map {
            stream.readNameString() ?: "???"
        }
        return JsTypeDefKeyListStubImpl(parent, fileName, keyListName, values)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefKeyList)?.keyName?.text?.isNotNullOrBlank().orFalse()
    }

    override fun indexStub(stub: JsTypeDefKeysListStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexKeyList(stub, sink)
    }
}
