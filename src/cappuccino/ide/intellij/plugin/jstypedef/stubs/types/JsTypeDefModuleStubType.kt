package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefModuleImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefModuleStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefModuleStub
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

class JsTypeDefModuleStubType internal constructor(
        debugName: String) : JsTypeDefStubElementType<JsTypeDefModuleStub, JsTypeDefModuleImpl>(debugName, JsTypeDefModuleImpl::class.java) {

    override fun createPsi(
            stub: JsTypeDefModuleStub): JsTypeDefModuleImpl {
        return JsTypeDefModuleImpl(stub, this)
    }

    override fun createStub(module:JsTypeDefModuleImpl, parent: StubElement<*>): JsTypeDefModuleStub {
        val fileName = module.containingFile.name
        val namespaceComponents = module.namespaceComponents.toMutableList()
        val moduleName = namespaceComponents.removeAt(namespaceComponents.lastIndex)
        return JsTypeDefModuleStubImpl(parent, fileName, namespaceComponents, moduleName)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: JsTypeDefModuleStub,
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
            stream: StubInputStream, parent: StubElement<*>): JsTypeDefModuleStub {

        val fileName = stream.readName()?.string ?: ""
        val numComponents = stream.readInt()
        val namespaceComponents = (0 until numComponents).map {
            stream.readNameString() ?: "???"
        }
        val moduleName = stream.readNameString() ?: ""
        return JsTypeDefModuleStubImpl(parent, fileName, namespaceComponents, moduleName)
    }

    override fun indexStub(stub: JsTypeDefModuleStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexModule(stub, sink)
    }
}
