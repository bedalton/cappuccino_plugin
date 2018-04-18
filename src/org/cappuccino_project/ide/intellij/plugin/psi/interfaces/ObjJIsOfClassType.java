package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.jetbrains.annotations.NotNull;


public interface ObjJIsOfClassType {
    ObjJIsOfClassType UNDEF = new ObjJIsOfClassType() {
        @NotNull
        @Override
        public ObjJClassType getClassType() {
            return ObjJClassType.UNDEF;
        }
    };

    @NotNull
    ObjJClassType getClassType();
}
