package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJQualifiedReference;

import java.util.List;

public interface ObjJVariableAssignment extends ObjJCompositeElement {
    List<ObjJQualifiedReference> getQualifiedReferenceList();
    ObjJExpr getAssignedValue();
}
