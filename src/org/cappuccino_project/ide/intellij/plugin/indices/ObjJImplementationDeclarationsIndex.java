package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJImplementationDeclarationsIndex extends ObjJStringStubIndexBase<ObjJImplementationDeclaration> {

    private static final ObjJImplementationDeclarationsIndex INSTANCE = new ObjJImplementationDeclarationsIndex();
    private static final StubIndexKey<String, ObjJImplementationDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJImplementationDeclarationsIndex.class);
    private static final int VERSION = 0;

    private ObjJImplementationDeclarationsIndex(){}

    public static ObjJImplementationDeclarationsIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJImplementationDeclaration> getIndexedElementClass() {
        return ObjJImplementationDeclaration.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJImplementationDeclaration> getKey() {
        return KEY;
    }

}
