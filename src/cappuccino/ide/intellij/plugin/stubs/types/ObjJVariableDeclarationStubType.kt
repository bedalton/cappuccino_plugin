package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.ObjJDerefExpression
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJVariableDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class ObjJVariableDeclarationStubType internal constructor(debugName:String):
        ObjJStubElementType<ObjJVariableDeclarationStub, ObjJVariableDeclarationImpl>(debugName, ObjJVariableDeclarationImpl::class.java) {
    override fun createPsi(stub: ObjJVariableDeclarationStub): ObjJVariableDeclarationImpl {
        return ObjJVariableDeclarationImpl(stub, this)
    }

    override fun serialize(stub: ObjJVariableDeclarationStub, stream: StubOutputStream) {
        stream.writeInt(stub.qualifiedNameParts.size)
        stub.qualifiedNameParts.forEach {
            stream.writeQNComponents(it)
        }
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJVariableDeclarationStub {
        val numParts = stream.readInt()
        val out = mutableListOf<QualifiedReferenceStubComponents>()
        (0 until numParts).forEach { _ ->
            out.add(stream.readQNComponents())
        }
        return ObjJVariableDeclarationStubImpl(parent, out)
    }

    override fun createStub(element: ObjJVariableDeclarationImpl, parent: StubElement<*>): ObjJVariableDeclarationStub {
        return ObjJVariableDeclarationStubImpl(parent, element.toQualifiedNamePaths())
    }

}

fun ObjJVariableDeclaration.toQualifiedNamePaths() : List<QualifiedReferenceStubComponents> {
    return this.qualifiedReferenceList.map {
        it.toStubParts()
    } + this.derefExpressionList.map {
        it.toStubPart()
    }
}

fun ObjJDerefExpression.toStubPart() : QualifiedReferenceStubComponents {
    return listOf(ObjJQualifiedReferenceComponentPart(this.variableName.text, ObjJQualifiedReferenceComponentPartType.VARIABLE_NAME))
}