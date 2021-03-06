package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefClassElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefInterfaceElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefClassStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefInterfaceStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefInterfaceStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeTypeList
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

abstract class JsTypeDefClassDeclarationStubType<PsiT:JsTypeDefClassDeclaration<PsiT,StubT>, StubT:JsTypeDefClassDeclarationStub<PsiT>> internal constructor(
        debugName: String, psiClass:Class<PsiT>) : JsTypeDefStubElementType<StubT, PsiT>(debugName, psiClass) {

    override fun createStub(declaration:PsiT, parent: StubElement<*>): StubT {
        val fileName = declaration.containingFile.name
        val namespaceComponents = declaration.namespaceComponents.toMutableList()
        val className = namespaceComponents.removeAt(namespaceComponents.lastIndex)
        val superClasses = declaration.extendsStatement?.typeList.toJsTypeDefTypeListTypes()
        val completionModifier = declaration.completionModifier
        return createStub(parent, fileName, namespaceComponents, className, superClasses, completionModifier)
    }

    protected abstract fun createStub(parent: StubElement<*>, fileName:String, namespaceComponents:List<String>, className:String, superClasses:Set<JsTypeListType>, completionModifier: CompletionModifier) : StubT

    @Throws(IOException::class)
    override fun serialize(
            stub: StubT,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        val namespaceComponents = stub.namespaceComponents
        stream.writeInt(namespaceComponents.size)
        for (component in namespaceComponents)
            stream.writeName(component)
        stream.writeName(stub.className)
        stream.writeTypeList(stub.superTypes)
        stream.writeName(stub.completionModifier.tag)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): StubT {

        val fileName = stream.readName()?.string ?: ""
        val numComponents = stream.readInt()
        val namespaceComponents = (0 until numComponents).map {
            stream.readName()?.string ?: "???"
        }
        val className = stream.readName()?.string ?: ""
        val superTypes = stream.readTypesList().toSet()
        val completionModifier = CompletionModifier.fromTag(stream.readName()?.string!!)
        return createStub(parent, fileName, namespaceComponents, className, superTypes, completionModifier)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node?.psi as? JsTypeDefClassDeclaration<*,*>)?.className != null
    }
}


class JsTypeDefInterfaceStubType(debugName:String):JsTypeDefClassDeclarationStubType<JsTypeDefInterfaceElementImpl, JsTypeDefInterfaceStub>(debugName, JsTypeDefInterfaceElementImpl::class.java) {
    override fun createStub(parent: StubElement<*>, fileName: String, namespaceComponents: List<String>, className: String, superClasses: Set<JsTypeListType>, completionModifier: CompletionModifier): JsTypeDefInterfaceStub {
        return JsTypeDefInterfaceStubImpl(parent, fileName, namespaceComponents, className, superClasses, completionModifier)
    }

    override fun createPsi(stub: JsTypeDefInterfaceStub): JsTypeDefInterfaceElementImpl {
        return JsTypeDefInterfaceElementImpl(stub, this)
    }

    override fun indexStub(stub: JsTypeDefInterfaceStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexInterface(stub, sink)
    }
}

class JsTypeDefClassStubType(debugName: String):JsTypeDefClassDeclarationStubType<JsTypeDefClassElementImpl, JsTypeDefClassStub>(debugName, JsTypeDefClassElementImpl::class.java) {
    override fun createStub(parent: StubElement<*>, fileName: String, namespaceComponents: List<String>, className: String, superClasses: Set<JsTypeListType>, completionModifier: CompletionModifier): JsTypeDefClassStub {
        return JsTypeDefClassStubImpl(parent, fileName, namespaceComponents, className, superClasses, completionModifier)
    }

    override fun createPsi(stub: JsTypeDefClassStub): JsTypeDefClassElementImpl {
        return JsTypeDefClassElementImpl(stub, this)
    }

    override fun indexStub(stub: JsTypeDefClassStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexClass(stub, sink)
    }
}