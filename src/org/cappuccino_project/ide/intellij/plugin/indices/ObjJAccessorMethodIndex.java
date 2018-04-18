package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJAccessorProperty;
import org.jetbrains.annotations.NotNull;

public class ObjJAccessorMethodIndex extends ObjJMethodHeaderDeclarationsIndexBase<ObjJAccessorProperty> {

    private static final ObjJAccessorMethodIndex INSTANCE = new ObjJAccessorMethodIndex();
    private static final StubIndexKey<String, ObjJAccessorProperty> KEY = IndexKeyUtil.createIndexKey(ObjJAccessorMethodIndex.class);
    private static final int VERSION = 1;
    private ObjJAccessorMethodIndex () {}

    public static ObjJAccessorMethodIndex getInstance() {
        return INSTANCE;
    }

    public int getVersion() {
        return super.getVersion()+ ObjJIndexService.INDEX_VERSION + VERSION;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJAccessorProperty> getKey() {
        return KEY;
    }


    @NotNull
    @Override
    protected Class<ObjJAccessorProperty> getIndexedElementClass() {
        return ObjJAccessorProperty.class;
    }
}
