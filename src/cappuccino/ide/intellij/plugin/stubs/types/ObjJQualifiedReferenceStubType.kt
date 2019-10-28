package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.impl.ObjJQualifiedReferenceImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJQualifiedReferenceStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPartType
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.QualifiedReferenceStubComponents
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class ObjJQualifiedReferenceStubType(debugName:String) : ObjJStubElementType<ObjJQualifiedReferenceStub, ObjJQualifiedReferenceImpl>(debugName, ObjJQualifiedReferenceImpl::class.java) {
    override fun createPsi(stub: ObjJQualifiedReferenceStub): ObjJQualifiedReferenceImpl {
        return ObjJQualifiedReferenceImpl(stub, this)
    }

    override fun serialize(stub: ObjJQualifiedReferenceStub, stream: StubOutputStream) {
        stream.writeQNComponents(stub.components)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJQualifiedReferenceStub {
        return ObjJQualifiedReferenceStubImpl(parent, stream.readQNComponents())
    }

    override fun createStub(element: ObjJQualifiedReferenceImpl, parent: StubElement<*>): ObjJQualifiedReferenceStub {
        val parts = element.toStubParts()
        return ObjJQualifiedReferenceStubImpl(parent, parts)
    }

    override fun indexStub(stub: ObjJQualifiedReferenceStub, sink: IndexSink) {
    }

}

fun ObjJQualifiedReference.toStubParts() : List<ObjJQualifiedReferenceComponentPart> {
    return qualifiedNameParts.map { part ->
        when (part) {
            is ObjJArrayIndexSelector -> ObjJQualifiedReferenceComponentPart("[]", ObjJQualifiedReferenceComponentPartType.ARRAY_COMPONENT)
            is ObjJMethodCall -> ObjJQualifiedReferenceComponentPart(part.selectorString, ObjJQualifiedReferenceComponentPartType.METHOD_CALL)
            is ObjJFunctionCall -> ObjJQualifiedReferenceComponentPart(part.functionName?.text, ObjJQualifiedReferenceComponentPartType.FUNCTION_NAME)
            is ObjJVariableName -> ObjJQualifiedReferenceComponentPart(part.text, ObjJQualifiedReferenceComponentPartType.VARIABLE_NAME)
            is ObjJParenEnclosedExpr -> ObjJQualifiedReferenceComponentPart("(...)", ObjJQualifiedReferenceComponentPartType.PAREN_ENCLOSED_EXPR)
            is ObjJStringLiteral -> ObjJQualifiedReferenceComponentPart(part.text, ObjJQualifiedReferenceComponentPartType.STRING_LITERAL)
            else -> throw Exception("Qualified name part was an unexpected type")
        }
    }
}

internal fun StubOutputStream.writeQNComponents(components:QualifiedReferenceStubComponents) {
    writeInt(components.size)
    components.forEach {
        writeQNComponentPart(it)
    }
}

internal fun StubInputStream.readQNComponents() : QualifiedReferenceStubComponents {
    val parts = mutableListOf<ObjJQualifiedReferenceComponentPart>()
    val numParts = readInt()
    for (i in 0 until numParts)
        parts.add(readQNComponentPart())
    return parts
}

internal fun StubOutputStream.writeQNComponentPart(part:ObjJQualifiedReferenceComponentPart) {
    writeName(part.name)
    writeInt(part.type.typeId)
}

internal fun StubInputStream.readQNComponentPart() : ObjJQualifiedReferenceComponentPart {
    val name = readName()?.string
    val type = ObjJQualifiedReferenceComponentPartType.fromId(readInt())
    return ObjJQualifiedReferenceComponentPart(name, type)
}