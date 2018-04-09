package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJClassMethodIndex extends ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration> {
    private static final StubIndexKey<String, ObjJMethodHeaderDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJClassMethodIndex.class);
    private static final ObjJClassMethodIndex INSTANCE = new ObjJClassMethodIndex();
    private static final int VERSION = 2;


    private ObjJClassMethodIndex() {
    }

    public static ObjJClassMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJMethodHeaderDeclaration> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return ObjJIndexService.INDEX_VERSION + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJMethodHeaderDeclaration> getIndexedElementClass() {
        return ObjJMethodHeaderDeclaration.class;
    }


}
