package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch.SearchParameters
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassInheritorsSearch : ExtensibleQueryFactory<ObjJClassDeclarationElement<*>, SearchParameters>() {
    companion object {

        private val INSTANCE = ObjJClassInheritorsSearch()

        @JvmOverloads
        fun search(aClass: ObjJClassDeclarationElement<*>, scope: SearchScope = GlobalSearchScope.allScope(aClass.project)): Query<ObjJClassDeclarationElement<*>> {
            return INSTANCE.createUniqueResultsQuery(SearchParameters(aClass))
        }
    }
}
