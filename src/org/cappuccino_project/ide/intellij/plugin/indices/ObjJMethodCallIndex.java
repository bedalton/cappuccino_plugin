package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall;
import org.jetbrains.annotations.NotNull;

public class ObjJMethodCallIndex extends ObjJStringStubIndexBase<ObjJMethodCall> {
    private static final StubIndexKey<String, ObjJMethodCall> KEY = IndexKeyUtil.createIndexKey(ObjJMethodCallIndex.class);
    private static final ObjJMethodCallIndex INSTANCE = new ObjJMethodCallIndex();
    private static final int VERSION = 0;


    private ObjJMethodCallIndex() {

    }

    public static ObjJMethodCallIndex getInstance()
    {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJMethodCall> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJMethodCall> getIndexedElementClass() {
        return ObjJMethodCall.class;
    }
}
