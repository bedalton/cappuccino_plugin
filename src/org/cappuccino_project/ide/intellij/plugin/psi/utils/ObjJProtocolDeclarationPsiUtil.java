package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjJProtocolDeclarationPsiUtil {

    public static ProtocolMethods getHeaders(@NotNull ObjJProtocolDeclaration declaration) {
        final List<ObjJMethodHeader> required;
        final List<ObjJMethodHeader> optional;

    }


    public static class ProtocolMethods {
        public final List<ObjJMethodHeader> required;
        public final List<ObjJMethodHeader> optional;

        public ProtocolMethods(@NotNull final List<ObjJMethodHeader> required, @NotNull final List<ObjJMethodHeader> optional) {
            this.required = required;
            this.optional = optional;
        }
    }

}
