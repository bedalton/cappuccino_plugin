package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJExpr

interface ObjJConditionalStatement : ObjJCompositeElement {
    val conditionalExpression: ObjJExpr
}
