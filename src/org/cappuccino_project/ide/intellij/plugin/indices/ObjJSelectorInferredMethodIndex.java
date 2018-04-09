package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjJSelectorInferredMethodIndex extends ObjJMethodHeaderDeclarationsIndexBase<ObjJSelectorLiteral> {
    private static final ObjJSelectorInferredMethodIndex INSTANCE = new ObjJSelectorInferredMethodIndex();
    private static final StubIndexKey<String, ObjJSelectorLiteral> KEY = IndexKeyUtil.createIndexKey(ObjJSelectorInferredMethodIndex.class);
    private static final int VERSION = 1;

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
