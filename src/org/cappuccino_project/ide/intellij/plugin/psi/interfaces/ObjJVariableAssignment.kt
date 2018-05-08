package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJQualifiedReference

interface ObjJVariableAssignment : ObjJCompositeElement {
    val qualifiedReferenceList: List<ObjJQualifiedReference>
    val assignedValue: ObjJExpr
}
