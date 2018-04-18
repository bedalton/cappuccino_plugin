package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJProtocolDeclarationsIndex extends ObjJStringStubIndexBase<ObjJProtocolDeclaration> {

    private static final ObjJProtocolDeclarationsIndex INSTANCE = new ObjJProtocolDeclarationsIndex();
    private static final StubIndexKey<String, ObjJProtocolDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJProtocolDeclarationsIndex.class);
    private static final int VERSION = 1;

    private ObjJProtocolDeclarationsIndex(){}

    public static ObjJProtocolDeclarationsIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJProtocolDeclaration> getIndexedElementClass() {
        return ObjJProtocolDeclaration.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJProtocolDeclaration> getKey() {
        return KEY;
    }

}
