package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference

interface ObjJVariableAssignment : ObjJCompositeElement {
    val qualifiedReferenceList: List<ObjJQualifiedReference>
    val assignedValue: ObjJExpr
}
