package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiElementProcessorAdapter
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassImplementationsSearch : QueryExecutor<ObjJClassDeclarationElement<*>, DefinitionsScopedSearch.SearchParameters> {

    override fun execute(
            queryParameters: DefinitionsScopedSearch.SearchParameters,
            consumer: Processor<ObjJClassDeclarationElement<*>>): Boolean {

        val sourceElement = queryParameters.element
        return sourceElement !is ObjJClassDeclarationElement<*> || processImplementations(sourceElement, consumer, queryParameters.scope)
    }

    companion object {

        fun processImplementations(psiClass: ObjJClassDeclarationElement<*>, processor: Processor<ObjJClassDeclarationElement<*>>, scope: SearchScope): Boolean {
            if (!ObjJClassInheritorsSearch.search(psiClass, scope).forEach(Processor { processor.process(it) })) {
                return false
            }

            val showInterfaces = Registry.`is`("ide.goto.implementation.show.interfaces")
            return ObjJClassInheritorsSearch.search(psiClass, scope).forEach(PsiElementProcessorAdapter<ObjJClassDeclarationElement> { classDeclarationElement ->
                if (!showInterfaces && classDeclarationElement is ObjJProtocolDeclaration) {
                    return@ObjJClassInheritorsSearch.search(psiClass, scope).forEach true
                }
                processor.process(classDeclarationElement)
            })
        }
    }
}
