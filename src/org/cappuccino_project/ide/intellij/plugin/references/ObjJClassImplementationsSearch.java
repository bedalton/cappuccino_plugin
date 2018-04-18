package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessorAdapter;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

public class ObjJClassImplementationsSearch implements QueryExecutor<ObjJClassDeclarationElement, DefinitionsScopedSearch.SearchParameters> {

    @Override
    public boolean execute(
            @NotNull
                    DefinitionsScopedSearch.SearchParameters queryParameters,
            @NotNull
                    Processor<ObjJClassDeclarationElement> consumer) {

        final PsiElement sourceElement = queryParameters.getElement();
        return !(sourceElement instanceof ObjJClassDeclarationElement) || processImplementations((ObjJClassDeclarationElement)sourceElement, consumer, queryParameters.getScope());
    }

    public static boolean processImplementations(final ObjJClassDeclarationElement psiClass, final Processor<ObjJClassDeclarationElement> processor, SearchScope scope) {
        if (!ObjJClassInheritorsSearch.search(psiClass, scope).forEach((Processor<ObjJClassDeclarationElement>) processor::process)) {
            return false;
        }

        final boolean showInterfaces = Registry.is("ide.goto.implementation.show.interfaces");
        return ObjJClassInheritorsSearch.search(psiClass, scope).forEach(new PsiElementProcessorAdapter<>(classDeclarationElement -> {
            if (!showInterfaces && classDeclarationElement instanceof ObjJProtocolDeclaration) {
                return true;
            }
            return processor.process(classDeclarationElement);
        }));
    }
}
