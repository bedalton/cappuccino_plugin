package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.psi.search.PsiElementProcessor
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassDeclarationProcessor : PsiElementProcessor<ObjJClassDeclarationElement<*>> {
    override fun execute(
            classDeclarationElement: ObjJClassDeclarationElement<*>): Boolean {
        return false
    }
}
