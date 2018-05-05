package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.jetbrains.annotations.NotNull;

public class ObjJMethodIndex extends ObjJStringStubIndexBase<ObjJMethodHeader>{
    @NotNull
    public static final StubIndexKey<String, ObjJMethodHeader> KEY = IndexKeyUtil.createIndexKey(ObjJMethodIndex.class);
    @NotNull
    private static final ObjJMethodIndex INSTANCE = new ObjJMethodIndex();
    private static final int VERSION = 0;

    private ObjJMethodIndex() {
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJMethodHeader> getIndexedElementClass() {
        return ObjJMethodHeader.class;
    }

    @NotNull
    public static ObjJMethodIndex getInstance()
    {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJMethodHeader> getKey() {
        return KEY;
    }
}
