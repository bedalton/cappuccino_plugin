package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral;
import org.jetbrains.annotations.NotNull;

public class ObjJSelectorInferredMethodIndex extends ObjJMethodHeaderDeclarationsIndexBase<ObjJSelectorLiteral> {
    private static final ObjJSelectorInferredMethodIndex INSTANCE = new ObjJSelectorInferredMethodIndex();
    private static final StubIndexKey<String, ObjJSelectorLiteral> KEY = IndexKeyUtil.createIndexKey(ObjJSelectorInferredMethodIndex.class);
    private static final int VERSION = 0;

    public static ObjJSelectorInferredMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJSelectorLiteral> getKey() {
        return KEY;
    }

    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJSelectorLiteral> getIndexedElementClass() {
        return ObjJSelectorLiteral.class;
    }
}
