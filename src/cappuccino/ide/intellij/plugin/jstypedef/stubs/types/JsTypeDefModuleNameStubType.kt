package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModuleName
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefModuleNameImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefModuleNameStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefModuleNameStub
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefModuleNameStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefModuleNameStub, JsTypeDefModuleNameImpl>(debugName, JsTypeDefModuleNameImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefModuleNameStub): JsTypeDefModuleNameImpl {
        return JsTypeDefModuleNameImpl(stub, this)
    }

    override fun createStub(module:JsTypeDefModuleNameImpl, parent: StubElement<*>): JsTypeDefModuleNameStub {
        val fileName = module.containingFile.name
        val namespaceComponents = module.namespaceComponents.toMutableList()
        val moduleName = namespaceComponents.removeAt(namespaceComponents.lastIndex)
        return JsTypeDefModuleNameStubImpl(parent, fileName, namespaceComponents, moduleName)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefModuleNameStub,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        val namespaceComponents = stub.namespaceComponents
        stream.writeInt(namespaceComponents.size)
        for (component in namespaceComponents)
            stream.writeName(component)
        stream.writeName(stub.moduleName)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefModuleNameStub {

        val fileName = stream.readName()?.string ?: ""
        val numComponents = stream.readInt()
        val namespaceComponents = mutableListOf<String>()
        for (i in 0 until numComponents){
            namespaceComponents.add(stream.readNameString() ?: "???")
        }
        val moduleName = stream.readNameString() ?: ""
        return JsTypeDefModuleNameStubImpl(parent, fileName, namespaceComponents, moduleName)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefModuleName)?.text.isNotNullOrBlank()
    }
}
