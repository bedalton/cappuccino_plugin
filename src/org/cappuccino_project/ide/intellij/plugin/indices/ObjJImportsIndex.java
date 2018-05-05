package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.jetbrains.annotations.NotNull;

public class ObjJImportsIndex extends ObjJStringStubIndexBase<ObjJImportStatement> {

    private static final ObjJImportsIndex INSTANCE = new ObjJImportsIndex();
    private static final StubIndexKey<String, ObjJImportStatement> KEY = IndexKeyUtil.createIndexKey(ObjJImportsIndex.class);
    private static final int VERSION = 0;
    @NotNull
    public static ObjJImportsIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJImportStatement> getIndexedElementClass() {
        return ObjJImportStatement.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJImportStatement> getKey() {
        return KEY;
    }
}
