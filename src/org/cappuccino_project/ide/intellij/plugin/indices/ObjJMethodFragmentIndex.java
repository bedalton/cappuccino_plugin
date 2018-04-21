package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJMethodFragmentIndex extends ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration> {
    private static final ObjJMethodFragmentIndex INSTANCE = new ObjJMethodFragmentIndex();
    public static final StubIndexKey<String, ObjJMethodHeaderDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJMethodFragmentIndex.class);
    private static final int VERSION = 0;


    private ObjJMethodFragmentIndex() {}

    @NotNull
    public static ObjJMethodFragmentIndex getInstance() {
        return INSTANCE;
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
    @Override
    public StubIndexKey<String, ObjJMethodHeaderDeclaration> getKey() {
        return KEY;
    }
}
