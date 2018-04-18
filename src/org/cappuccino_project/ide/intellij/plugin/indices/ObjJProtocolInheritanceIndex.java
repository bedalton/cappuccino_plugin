package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ObjJProtocolInheritanceIndex extends ObjJStringStubIndexBase<ObjJClassDeclarationElement> {
    private static final ObjJProtocolInheritanceIndex INSTANCE = new ObjJProtocolInheritanceIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJProtocolInheritanceIndex.class);
    private static final int VERSION = 1;
    private ObjJProtocolInheritanceIndex() {}

    public static ObjJProtocolInheritanceIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion()+ ObjJIndexService.INDEX_VERSION+VERSION;
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

    @NotNull
    public List<String> getChildClassesAsStrings(@NotNull String parentClassName, @NotNull Project project) {
        return getChildClassesRecursive(new ArrayList<>(), parentClassName, project);
    }

    private List<String> getChildClassesRecursive(@NotNull final List<String> descendants, @NotNull final String className, @NotNull Project project) {
        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyRuntimeException();
        }
        for (ObjJClassDeclarationElement classDeclarationElement : get(className, project)) {
            String currentClassName = classDeclarationElement.getClassNameString();
            if (descendants.contains(currentClassName)) {
                continue;
            }
            descendants.add(currentClassName);
            getChildClassesRecursive(descendants, currentClassName, project);
        }
        return descendants;
    }
}
