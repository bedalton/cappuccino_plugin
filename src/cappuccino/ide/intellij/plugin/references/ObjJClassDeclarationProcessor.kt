package cappuccino.ide.intellij.plugin.references

import com.intellij.psi.search.PsiElementProcessor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassDeclarationProcessor : PsiElementProcessor<ObjJClassDeclarationElement<*>> {
    override fun execute(
            classDeclarationElement: ObjJClassDeclarationElement<*>): Boolean {
        return false
    }
}
