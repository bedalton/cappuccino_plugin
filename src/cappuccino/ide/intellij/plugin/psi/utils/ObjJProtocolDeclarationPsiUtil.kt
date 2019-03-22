package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration

import java.util.ArrayList
import java.util.Collections

object ObjJProtocolDeclarationPsiUtil {

    class ProtocolMethods(val required: List<ObjJMethodHeader>, val optional: List<ObjJMethodHeader>)

}
