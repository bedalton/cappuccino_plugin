package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType;
import org.jetbrains.annotations.NotNull;

/**
 * Index to find global variables by file name
 */
public class ObjJGlobalVariablesByFileNameIndex extends ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration> {

    private static final ObjJGlobalVariablesByFileNameIndex INSTANCE = new ObjJGlobalVariablesByFileNameIndex();
    private static final StubIndexKey<String, ObjJGlobalVariableDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJGlobalVariablesByFileNameIndex.class);
    private static final int VERSION = ObjJGlobalVariableDeclarationStubType.VERSION;

    @NotNull
    public static ObjJGlobalVariablesByFileNameIndex getInstance() {
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