package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJClassMethodIndex extends ObjJStringStubIndexBase<ObjJMethodHeader> {
    private static final StubIndexKey<String, ObjJMethodHeader> KEY = IndexKeyUtil.createIndexKey(ObjJClassMethodIndex.class);
    private static final ObjJClassMethodIndex INSTANCE = new ObjJClassMethodIndex();
    private static final int VERSION = 3;


    private ObjJClassMethodIndex() {
    }

    public static ObjJClassMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJMethodHeader> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return ObjJIndexService.INDEX_VERSION + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJMethodHeader> getIndexedElementClass() {
        return ObjJMethodHeader.class;
    }


}
