package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJUnifiedMethodIndex extends ObjJMethodHeaderDeclarationsIndexBase<ObjJMethodHeaderDeclaration>{
    @NotNull
    public static final StubIndexKey<String, ObjJMethodHeaderDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJUnifiedMethodIndex.class);
    @NotNull
    private static final ObjJUnifiedMethodIndex INSTANCE = new ObjJUnifiedMethodIndex();
    private static final int VERSION = 1;

    private ObjJUnifiedMethodIndex() {
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJMethodHeaderDeclaration> getIndexedElementClass() {
        return ObjJMethodHeaderDeclaration.class;
    }

    @NotNull
    public static ObjJUnifiedMethodIndex getInstance()
    {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJMethodHeaderDeclaration> getKey() {
        return KEY;
    }
}
