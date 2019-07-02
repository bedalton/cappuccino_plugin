package cappuccino.ide.intellij.plugin.universal.psi

import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope
import cappuccino.ide.intellij.plugin.psi.utils.getScope
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil

interface ObjJUniversalPsiElement : PsiElement {

    fun getModule() : Module? = ModuleUtilCore.findModuleForPsiElement(this)

    fun <PsiT : PsiElement> getParentOfType(parentClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildOfType(childClass:Class<PsiT>) : PsiT?
    fun <PsiT : PsiElement> getChildrenOfType(childClass:Class<PsiT>) : List<PsiT>

    fun commonContext(otherElement:PsiElement) : PsiElement? {
        return PsiTreeUtil.findCommonContext(this, otherElement)
    }

    fun commonScope(otherElement: PsiElement) : ReferencedInScope {
        return getScope(commonContext(otherElement))
    }
}
interface ObjJUniversalStubBasedElement<StubT: StubElement<*>> : StubBasedPsiElement<StubT>, ObjJUniversalPsiElement