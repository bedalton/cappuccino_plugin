package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class ObjJMethodCallReferenceProvider(psiElement: ObjJHasMethodSelector) : PsiPolyVariantReferenceBase<ObjJHasMethodSelector>(psiElement, TextRange.create(0, psiElement.textLength)) {

    private val selector: String = psiElement.selectorString

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        if (DumbService.isDumb(myElement.project)) {
            return ResolveResult.EMPTY_ARRAY
        }
        val result = ObjJUnifiedMethodIndex.instance[selector, myElement.project].toSet()
        return if (result.isNotEmpty()) {
            PsiElementResolveResult.createResults(result)
        } else if (myElement != null) {
            val selectorLiteral = ObjJPsiImplUtil.getSelectorLiteralReference(myElement) ?: return arrayOf()
            PsiElementResolveResult.createResults(selectorLiteral)
        } else
            return arrayOf()
    }

    override fun getVariants(): Array<Any> {
        return if (DumbService.isDumb(myElement.project)) {
            arrayOf()
        } else arrayOf()
    }

    override fun handleElementRename(selectorString: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, selectorString)
    }
}
