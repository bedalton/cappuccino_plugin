package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

import java.util.*

class ObjJMethodCallReferenceProvider(psiElement: ObjJHasMethodSelector) : PsiPolyVariantReferenceBase<ObjJHasMethodSelector>(psiElement, TextRange.create(0, psiElement.textLength)) {

    private val selector: String
    private val containingClass: String


    init {
        selector = psiElement.selectorString
        this.containingClass = ObjJPsiImplUtil.getContainingClassName(psiElement)
    }


    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        if (DumbService.isDumb(myElement.project)) {
            return ResolveResult.EMPTY_ARRAY
        }
        val result = ArrayList<PsiElement>(ObjJUnifiedMethodIndex.instance.get(selector, myElement.project))
        return if (result.size > 0) {
            PsiElementResolveResult.createResults(result)
        } else PsiElementResolveResult.createResults(ObjJPsiImplUtil.getSelectorLiteralReference(myElement))
    }

    override fun getVariants(): Array<Any> {
        return if (DumbService.isDumb(myElement.project)) {
            arrayOf()
        } else arrayOf()
        //final List<ObjJMethodHeaderDeclaration> result = ObjJUnifiedMethodIndex.getInstance().getKeysByPattern(selector+"(.*)", myElement.getProject());
        //return result.toArray();
    }

    override fun handleElementRename(selectorString: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, selectorString)
    }
}
