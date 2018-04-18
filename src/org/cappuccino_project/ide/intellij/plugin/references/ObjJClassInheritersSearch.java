package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.search.searches.DefinitionsScopedSearch.SearchParameters;
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;

public class ObjJClassInheritersSearch extends ExtensibleQueryFactory<ObjJClassDeclarationElement, SearchParameters> {

    private static final ObjJClassInheritersSearch INSTANCE = new ObjJClassInheritersSearch();

    public static Query<ObjJClassDeclarationElement> search(final ObjJClassDeclarationElement aClass) {
        return search(aClass, GlobalSearchScope.allScope(aClass.getProject()));
    }

    public static Query<ObjJClassDeclarationElement> search(final ObjJClassDeclarationElement aClass, SearchScope scope) {
        return INSTANCE.createUniqueResultsQuery(new SearchParameters(aClass));
    }
}
