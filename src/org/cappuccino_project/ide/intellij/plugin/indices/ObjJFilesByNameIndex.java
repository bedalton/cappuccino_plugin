package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.jetbrains.annotations.NotNull;

public class ObjJFilesByNameIndex extends ObjJStringStubIndexBase<ObjJFile> {

    private static final ObjJFilesByNameIndex INSTANCE = new ObjJFilesByNameIndex();

    private static final StubIndexKey<String, ObjJFile> KEY = IndexKeyUtil.createIndexKey(ObjJFilesByNameIndex.class);

    private static final int VERSION = 3;


    private ObjJFilesByNameIndex(){}

    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    public static ObjJFilesByNameIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    protected Class<ObjJFile> getIndexedElementClass() {
        return ObjJFile.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJFile> getKey() {
        return KEY;
    }


}
