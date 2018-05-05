package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;


public class ObjJInheritsProtocolIndex extends ObjJStringStubIndexBase<ObjJClassDeclarationElement> {
    private static final ObjJInheritsProtocolIndex INSTANCE = new ObjJInheritsProtocolIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJInheritsProtocolIndex.class);
    private static final int VERSION = 0;
    private ObjJInheritsProtocolIndex() {}

    public static ObjJInheritsProtocolIndex getInstance() {
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
