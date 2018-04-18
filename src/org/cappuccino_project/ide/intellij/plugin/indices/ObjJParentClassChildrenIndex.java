package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
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

public class ObjJParentClassChildrenIndex extends StringStubIndexExtension<ObjJClassDeclarationElement> {
    private static final ObjJParentClassChildrenIndex INSTANCE = new ObjJParentClassChildrenIndex();
    private static final StubIndexKey<String, ObjJClassDeclarationElement> KEY = IndexKeyUtil.createIndexKey(ObjJParentClassChildrenIndex.class);
    private static final int VERSION = 1;
    private ObjJParentClassChildrenIndex() {}

    public static ObjJParentClassChildrenIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion()+ ObjJIndexService.INDEX_VERSION+VERSION;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJClassDeclarationElement> getKey() {
        return KEY;
    }

    @NotNull
    public List<String> getChildClassesAsStrings(@NotNull String parentClassName, @NotNull Project project) {
        //ProgressIndicatorProvider.checkCanceled();
        List<String> childClasses = new ArrayList<>();
        List<ObjJClassDeclarationElement> childClassElements = getChildClasses(parentClassName, project);
        for (ObjJClassDeclarationElement childClass : childClassElements) {
            //ProgressIndicatorProvider.checkCanceled();
            String childClassName = childClass.getStub() != null ? childClass.getStub().getClassName() : childClass.getClassNameString();
            if (childClassName.isEmpty()) {
                continue;
            }
            childClasses.add(childClassName);
        }
        return childClasses;
    }

    @NotNull
    public List<ObjJClassDeclarationElement> getChildClasses(@NotNull String parentClassName, @NotNull Project project) {
        return getChildClasses(parentClassName, project, null);
    }

    @NotNull
    public List<ObjJClassDeclarationElement> getChildClasses(@NotNull String parentClassName, @NotNull Project project, @Nullable GlobalSearchScope scope) {
        ProgressIndicatorProvider.checkCanceled();
        return new ArrayList<>(StubIndex.getElements(KEY, parentClassName, project, scope, ObjJClassDeclarationElement.class));
    }
}
