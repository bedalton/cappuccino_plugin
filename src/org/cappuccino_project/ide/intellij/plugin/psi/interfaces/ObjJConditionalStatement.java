package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr;

public interface ObjJConditionalStatement extends ObjJCompositeElement {
    ObjJExpr getConditionalExpression();
}
