package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ObjJInstanceVariablesByClassIndex extends ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration> {

    private static final ObjJInstanceVariablesByClassIndex INSTANCE = new ObjJInstanceVariablesByClassIndex();
    private static final StubIndexKey<String, ObjJInstanceVariableDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJInstanceVariablesByClassIndex.class);
    private static final int VERSION = 1;

    public static ObjJInstanceVariablesByClassIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJInstanceVariableDeclaration> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION;
    }

    @NotNull
    public List<String> getInstanceVariableNames(@NotNull String className, @NotNull Project project) {
        List<String> out = new ArrayList<>();
        for (ObjJInstanceVariableDeclaration variableDeclaration : get(className, project)) {
            String variableName = variableDeclaration.getStub() != null ? variableDeclaration.getStub().getVariableName() : variableDeclaration.getVariableName() != null ? variableDeclaration.getVariableName().getText() : null;
            if (variableName != null) {
                out.add(variableName);
            }
        }
        return out;
    }

    @NotNull
    @Override
    protected Class<ObjJInstanceVariableDeclaration> getIndexedElementClass() {
        return ObjJInstanceVariableDeclaration.class;
    }
}
