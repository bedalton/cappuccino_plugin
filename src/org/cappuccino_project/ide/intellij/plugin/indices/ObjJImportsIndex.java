package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.jetbrains.annotations.NotNull;

public class ObjJImportsIndex extends ObjJStringStubIndexBase<ObjJImportStatement> {

    private static final ObjJImportsIndex INSTANCE = new ObjJImportsIndex();
    private static final StubIndexKey<String, ObjJImportStatement> KEY = IndexKeyUtil.createIndexKey(ObjJImportsIndex.class);

    @NotNull
    public static ObjJImportsIndex getInstance() {
        return INSTANCE;
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
