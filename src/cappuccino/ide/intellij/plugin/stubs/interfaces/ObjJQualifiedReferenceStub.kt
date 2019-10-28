package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJQualifiedReferenceImpl
import com.intellij.psi.stubs.StubElement

interface ObjJQualifiedReferenceStub : StubElement<ObjJQualifiedReferenceImpl> {
    val components:QualifiedReferenceStubComponents
}

typealias QualifiedReferenceStubComponents = List<ObjJQualifiedReferenceComponentPart>

data class ObjJQualifiedReferenceComponentPart(val name:String?, val type:ObjJQualifiedReferenceComponentPartType)

enum class ObjJQualifiedReferenceComponentPartType(val typeId:Int) {
    VARIABLE_NAME(0),
    FUNCTION_NAME(1),
    METHOD_CALL(2),
    ARRAY_COMPONENT(3),
    PAREN_ENCLOSED_EXPR(4),
    STRING_LITERAL(5);

    companion object {
        fun fromId(id:Int) : ObjJQualifiedReferenceComponentPartType {
            return when (id) {
                0 -> VARIABLE_NAME
                1 -> FUNCTION_NAME
                2 -> METHOD_CALL
                3 -> ARRAY_COMPONENT
                4 -> PAREN_ENCLOSED_EXPR
                5 -> STRING_LITERAL
                else -> throw IndexOutOfBoundsException("Invalid qualified reference component part type id <$id>")
            }
        }
    }
}