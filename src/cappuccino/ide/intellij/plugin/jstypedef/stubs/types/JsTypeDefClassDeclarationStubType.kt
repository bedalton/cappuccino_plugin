package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefClassElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefInterfaceElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefInterfaceStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeTypeList
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import java.io.IOException

abstract class JsTypeDefClassDeclarationStubType<PsiT:JsTypeDefClassDeclaration<*>, StubT:JsTypeDefClassDeclarationStub<PsiT>> internal constructor(
        debugName: String, psiClass:Class<PsiT>) : JsTypeDefStubElementType<StubT, PsiT>(debugName, psiClass) {

    override fun createStub(declaration:PsiT, parent: StubElement<*>): StubT {
        val fileName = declaration.containingFile.name
        val namespaceComponents = declaration.namespaceComponents.toMutableList()
        val className = namespaceComponents.removeAt(namespaceComponents.lastIndex)
        val superClasses = declaration.extendsStatement.typeList.toJsTypeDefTypeListTypes()
        return createStub(parent, fileName, namespaceComponents, className, superClasses)
    }

    protected abstract fun createStub(parent: StubElement<*>, fileName:String, namespaceComponents:List<String>, className:String, superClasses:Set<JsTypeListType>) : StubT

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
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): StubT {

        val fileName = stream.readName()?.string ?: ""
        val numComponents = stream.readInt()
        val namespaceComponents = mutableListOf<String>()
        for (i in 0 until numComponents){
            namespaceComponents.add(stream.readNameString() ?: "???")
        }
        val className = stream.readNameString() ?: ""
        val superTypes = stream.readTypesList().toSet()
        return createStub(parent, fileName, namespaceComponents, className, superTypes)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return true
    }
}


class JsTypeDefInterfaceStubType:JsTypeDefClassDeclarationStubType<JsTypeDefInterfaceElementImpl, JsTypeDefInterfaceStub>("JS_INTERFACE_ELEMENT", JsTypeDefInterfaceElementImpl::class.java) {
    override fun createStub(parent: StubElement<*>, fileName: String, namespaceComponents: List<String>, className: String, superClasses: Set<JsTypeListType>): JsTypeDefInterfaceStub {
        return JsTypeDefInterfaceStubImpl(parent, fileName, namespaceComponents, className, superClasses)
    }

    override fun createPsi(stub: JsTypeDefInterfaceStub): JsTypeDefInterfaceElementImpl {
        return JsTypeDefInterfaceElementImpl(stub, this)
    }
}

class JsTypeDefClassStubType:JsTypeDefClassDeclarationStubType<JsTypeDefClassElementImpl, JsTypeDefClassStub>("JS_INTERFACE_ELEMENT", JsTypeDefClassElementImpl::class.java) {
    override fun createStub(parent: StubElement<*>, fileName: String, namespaceComponents: List<String>, className: String, superClasses: Set<JsTypeListType>): JsTypeDefClassStub {
        return JsTypeDefClassStubImpl(parent, fileName, namespaceComponents, className, superClasses)
    }

    override fun createPsi(stub: JsTypeDefClassStub): JsTypeDefClassElementImpl {
        return JsTypeDefClassElementImpl(stub, this)
    }
}