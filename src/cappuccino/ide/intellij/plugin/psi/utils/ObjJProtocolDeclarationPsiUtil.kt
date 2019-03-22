package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader

object ObjJProtocolDeclarationPsiUtil {

    class ProtocolMethods(val required: List<ObjJMethodHeader>, val optional: List<ObjJMethodHeader>)

}
