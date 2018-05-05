package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.jetbrains.annotations.NotNull;

public class ObjJImplementationCategoryDeclarationsIndex extends ObjJStringStubIndexBase<ObjJImplementationDeclaration> {

    private static final ObjJImplementationCategoryDeclarationsIndex INSTANCE = new ObjJImplementationCategoryDeclarationsIndex();
    private static final StubIndexKey<String, ObjJImplementationDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJImplementationCategoryDeclarationsIndex.class);
    private static final int VERSION = 0;

    private ObjJImplementationCategoryDeclarationsIndex(){}

    public static ObjJImplementationCategoryDeclarationsIndex getInstance() {
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
