package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJClassInstanceVariableAccessorMethodIndex extends ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration> {

    private static final ObjJClassInstanceVariableAccessorMethodIndex INSTANCE = new ObjJClassInstanceVariableAccessorMethodIndex();
    private static final StubIndexKey<String, ObjJInstanceVariableDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJClassInstanceVariableAccessorMethodIndex.class);
    private static final int VERSION = 0;

    public static ObjJClassInstanceVariableAccessorMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJInstanceVariableDeclaration> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJInstanceVariableDeclaration> getIndexedElementClass() {
        return ObjJInstanceVariableDeclaration.class;
    }
}
