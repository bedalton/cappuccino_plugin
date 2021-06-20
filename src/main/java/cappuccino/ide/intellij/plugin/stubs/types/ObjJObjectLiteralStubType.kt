package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListClass
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readJsFunctionList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readPropertiesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeJsFunctionList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writePropertiesList
import cappuccino.ide.intellij.plugin.psi.impl.ObjJObjectLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJObjectLiteralStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJObjectLiteralStub
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class ObjJObjectLiteralStubType(debugName:String) : ObjJStubElementType<ObjJObjectLiteralStub, ObjJObjectLiteralImpl>(debugName, ObjJObjectLiteralImpl::class.java) {

    override fun createPsi(stub: ObjJObjectLiteralStub): ObjJObjectLiteralImpl {
        return ObjJObjectLiteralImpl(stub, this)
    }

    override fun serialize(stub: ObjJObjectLiteralStub, stream: StubOutputStream) {
        stream.writeObject(stub.objectWithoutInference)
    }

    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>): ObjJObjectLiteralStub {
        val objectDefinition = stream.readObject()
        return ObjJObjectLiteralStubImpl(parentStub, objectDefinition)
    }

    override fun createStub(element: ObjJObjectLiteralImpl, parent: StubElement<*>): ObjJObjectLiteralStub {
        val objectLiteral = element.toJsObjectTypeSimple()
        return ObjJObjectLiteralStubImpl(parent, objectLiteral)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return true
    }
}

fun StubOutputStream.writeObject(ob: JsTypeListClass?) {
    writeBoolean(ob != null)
    if (ob == null)
        return
    writePropertiesList(ob.allProperties)
    writeJsFunctionList(ob.allFunctions)
}


fun StubInputStream.readObject() : JsTypeListClass? {
    if (!readBoolean())
        return null
    val properties = readPropertiesList().toSet()
    val functions = readJsFunctionList() ?: emptySet()
    return JsTypeListClass(
            allProperties = properties,
            allFunctions = functions
    )
}