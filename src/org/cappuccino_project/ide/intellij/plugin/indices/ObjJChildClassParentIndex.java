package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

public class ObjJChildClassParentIndex extends ObjJStringStubIndexBase<ObjJClassDeclarationElement> {
    private static final ObjJChildClassParentIndex INSTANCE = new ObjJChildClassParentIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJChildClassParentIndex.class);
    private static final int VERSION = 0;
    private ObjJChildClassParentIndex() {}

    public static ObjJChildClassParentIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJClassDeclarationElement> getIndexedElementClass() {
        return ObjJClassDeclarationElement.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJClassDeclarationElement> getKey() {
        return KEY;
    }

}
