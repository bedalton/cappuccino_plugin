package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to find parent classes of a class
 */
public class ObjJParentClassesByChildIndex extends ObjJStringStubIndexBase<ObjJClassDeclarationElement> {
    private static final ObjJParentClassesByChildIndex INSTANCE = new ObjJParentClassesByChildIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJParentClassesByChildIndex.class);
    private static final int VERSION = 0;
    private ObjJParentClassesByChildIndex() {}

    public static ObjJParentClassesByChildIndex getInstance() {
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


    @NotNull
    public List<ObjJClassDeclarationElement> getParentClasses(@NotNull String parentClassName, @NotNull Project project) {
        return getParentClasses(parentClassName, project, null);
    }

    @NotNull
    public List<ObjJClassDeclarationElement> getParentClasses(@NotNull String parentClassName, @NotNull Project project, @Nullable GlobalSearchScope scope) {
        //ProgressIndicatorProvider.checkCanceled();
        return new ArrayList<>(StubIndex.getElements(KEY, parentClassName, project, scope, ObjJClassDeclarationElement.class));
    }
}
