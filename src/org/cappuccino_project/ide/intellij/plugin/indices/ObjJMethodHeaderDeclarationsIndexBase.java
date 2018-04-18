package org.cappuccino_project.ide.intellij.plugin.indices;

import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;

public abstract class ObjJMethodHeaderDeclarationsIndexBase<MethodHeaderT extends ObjJMethodHeaderDeclaration> extends ObjJStringStubIndexBase<MethodHeaderT> {

    private static final int VERSION = 1;

    @Override
    public int getVersion() {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION;
    }

}
