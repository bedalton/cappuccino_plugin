package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInheritedProtocolList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface ObjJHasProtocolList {
    @Nullable
    ObjJInheritedProtocolList getInheritedProtocolList();

    @NotNull
    default List<String> getInheritedProtocols() {
        final ObjJInheritedProtocolList protocolListElement = getInheritedProtocolList();
        if (protocolListElement == null) {
            return Collections.emptyList();
        }
        final List<String> inheritedProtocols = new ArrayList<>();
        final List<ObjJClassName> protocolClassNameElements = protocolListElement.getClassNameList();
        if (protocolClassNameElements.isEmpty()) {
            return Collections.emptyList();
        }
        for (ObjJClassName classNameElement : protocolClassNameElements) {
            inheritedProtocols.add(classNameElement.getText());
        }
        return inheritedProtocols;
    }
}
