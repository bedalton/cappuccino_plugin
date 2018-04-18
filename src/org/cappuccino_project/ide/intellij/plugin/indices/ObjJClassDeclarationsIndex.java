package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

public class ObjJClassDeclarationsIndex extends ObjJStringStubIndexBase<ObjJClassDeclarationElement> {

    private static final ObjJClassDeclarationsIndex INSTANCE = new ObjJClassDeclarationsIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJClassDeclarationsIndex.class);
    private static final int VERSION = 1;

    private ObjJClassDeclarationsIndex(){}

    public static ObjJClassDeclarationsIndex getInstance() {
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
