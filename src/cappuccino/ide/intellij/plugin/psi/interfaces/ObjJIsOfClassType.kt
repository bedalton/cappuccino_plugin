package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType


interface ObjJIsOfClassType {

    val classType: ObjJClassType

    companion object {
        val UNDEF: ObjJIsOfClassType = object : ObjJIsOfClassType {
            override val classType: ObjJClassType
                get() = ObjJClassType.UNDEF
        }
    }
}
