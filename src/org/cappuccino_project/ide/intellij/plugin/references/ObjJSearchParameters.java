// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Taken from https://github.com/JetBrains/intellij-community/blob/master/platform/indexing-api/src/com/intellij/psi/search/searches/DefinitionsScopedSearch.java
package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.QueryParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJSearchParameters implements QueryParameters {
    private final PsiElement myElement;
    private final SearchScope myScope;
    private final boolean myCheckDeep;
    private final Project myProject;

    public ObjJSearchParameters(@NotNull
                            final PsiElement element) {
        this(element, ReadAction.compute(element::getUseScope), true);
    }

    public ObjJSearchParameters(@NotNull PsiElement element, @NotNull SearchScope scope, final boolean checkDeep) {
        myElement = element;
        myScope = scope;
        myCheckDeep = checkDeep;
        myProject = PsiUtilCore.getProjectInReadAction(myElement);
    }

    @NotNull
    public PsiElement getElement() {
        return myElement;
    }

    public boolean isCheckDeep() {
        return myCheckDeep;
    }

    @Nullable
    @Override
    public Project getProject() {
        return myProject;
    }

    @Override
    public boolean isQueryValid() {
        return myElement.isValid();
    }

    @NotNull
    public SearchScope getScope() {
        return myScope;
    }
}
