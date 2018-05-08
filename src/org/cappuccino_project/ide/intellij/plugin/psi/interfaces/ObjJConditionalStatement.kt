package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr

interface ObjJConditionalStatement : ObjJCompositeElement {
    val conditionalExpression: ObjJExpr
}
