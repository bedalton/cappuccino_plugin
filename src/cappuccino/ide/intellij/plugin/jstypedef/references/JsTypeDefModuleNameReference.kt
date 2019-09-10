package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefModulesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModuleName
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefModuleNameReference(element:JsTypeDefModuleName)  : PsiPolyVariantReferenceBase<JsTypeDefModuleName>(element, TextRange(0, element.text.length - 1)) {


    private val moduleName: String = myElement.text
    private val namespaceComponents: List<String> by lazy {
        JsTypeDefPsiImplUtil.getEnclosingNamespaceComponents(myElement)
    }
    private val namespaceString: String by lazy {
        namespaceComponents.joinToString(".")
    }

    override fun isReferenceTo(elementToCheck: PsiElement): Boolean {
        if (elementToCheck !is JsTypeDefModuleName)
            return false
        if (elementToCheck.text != moduleName)
            return false
        return namespaceString == elementToCheck.enclosingNamespace
    }

    override fun resolve(): PsiElement? {
        if (DumbService.isDumb(myElement.project))
            if (DumbService.isDumb(myElement.project))
                return null
        return JsTypeDefModulesByNamespaceIndex.instance[myElement.namespacedName, myElement.project].getOrNull(0)
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        if (DumbService.isDumb(myElement.project))
            return ResolveResult.EMPTY_ARRAY
        val results = JsTypeDefModulesByNamespaceIndex.instance[myElement.namespacedName, myElement.project]

        if (results.isEmpty())
            return ResolveResult.EMPTY_ARRAY
        return PsiElementResolveResult.createResults(results)
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

}