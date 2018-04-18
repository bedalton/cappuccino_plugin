package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType;
import org.jetbrains.annotations.NotNull;

/**
 * Index to find global variables by their name.
 */
public class ObjJGlobalVariableNamesIndex extends ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration> {

    private static final ObjJGlobalVariableNamesIndex INSTANCE = new ObjJGlobalVariableNamesIndex();
    private static final StubIndexKey<String, ObjJGlobalVariableDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJGlobalVariableNamesIndex.class);
    private static final int VERSION = ObjJGlobalVariableDeclarationStubType.VERSION;

    @NotNull
    public static ObjJGlobalVariableNamesIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJGlobalVariableDeclaration> getKey() {
        return KEY;
    }

    @NotNull
    @Override
    protected Class<ObjJGlobalVariableDeclaration> getIndexedElementClass() {
        return ObjJGlobalVariableDeclaration.class;
    }
}
