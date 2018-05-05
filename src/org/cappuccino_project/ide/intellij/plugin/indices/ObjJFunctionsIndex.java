package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ObjJFunctionsIndex extends ObjJStringStubIndexBase<ObjJFunctionDeclarationElement> {
    private static final StubIndexKey<String, ObjJFunctionDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJFunctionsIndex.class);
    private static final ObjJFunctionsIndex INSTANCE = new ObjJFunctionsIndex();
    private static final int VERSION = 0;


    private ObjJFunctionsIndex() {

    }

    public static ObjJFunctionsIndex getInstance()
    {
        return INSTANCE;
    }

    @Nullable
    public ObjJFunctionDeclarationElement get(@NotNull String fileName, @NotNull String functionName, @NotNull Project project) {
        for (ObjJFunctionDeclarationElement declarationElement : get(fileName, project)) {
            if (declarationElement.getFunctionNameAsString().equals(functionName)) {
                return declarationElement;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJFunctionDeclarationElement> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJFunctionDeclarationElement> getIndexedElementClass() {
        return ObjJFunctionDeclarationElement.class;
    }
}
