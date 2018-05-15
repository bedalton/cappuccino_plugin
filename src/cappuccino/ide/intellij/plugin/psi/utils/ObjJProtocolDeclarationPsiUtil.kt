package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration

import java.util.ArrayList
import java.util.Collections

object ObjJProtocolDeclarationPsiUtil {

    val EMPTY_PROTOCOL_METHODS_RESULT = ProtocolMethods(emptyList(), emptyList())

    fun getHeaders(declaration: ObjJProtocolDeclaration): ProtocolMethods {
        val required = ArrayList<ObjJMethodHeader>()
        val optional = ArrayList<ObjJMethodHeader>()


        return ProtocolMethods(required, optional)
    }


    class ProtocolMethods(val required: List<ObjJMethodHeader>, val optional: List<ObjJMethodHeader>)

}
