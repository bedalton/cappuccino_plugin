package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefGenericTypesType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefGenericsKey
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefKeyOfType
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefTypeGenericsKeyReference(element:JsTypeDefGenericsKey) : PsiPolyVariantReferenceBase<JsTypeDefGenericsKey>(element, TextRange(0, element.textLength)) {

    private val isDeclaration:Boolean by lazy {
        element.parent is JsTypeDefGenericTypesType || element.parent is JsTypeDefKeyOfType
    }

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        if (isDeclaration)
            return PsiElementResolveResult.createResults(element)

        val keyText = element.text
        val results:List<PsiElement>? = getIfEnclosedInFunction(keyText)
                ?: getIfEnclosedInClass(keyText)
        if (results.isNotNullOrEmpty()) {
            return PsiElementResolveResult.createResults(results)
        }
        return PsiElementResolveResult.EMPTY_ARRAY
    }

    private fun getIfEnclosedInFunction(keyText:String) : List<PsiElement>? {
        val enclosingFunction = element.getParentOfType(JsTypeDefFunction::class.java) ?: return null
        val keyOfTypes = enclosingFunction.argumentsList?.arguments.orEmpty().mapNotNull { it.keyOfType?.genericsKey }.filter {
            it.text == keyText
        }
        return if (keyOfTypes.isNotEmpty())
            keyOfTypes
        else
            emptyList()
    }

    private fun getIfEnclosedInClass(keyText: String) : List<PsiElement>? {
        val enclosingClass = element.getParentOfType(JsTypeDefClassDeclaration::class.java) ?: return null
        return enclosingClass.genericTypeTypes?.genericTypesTypeList.orEmpty().mapNotNull {
            it.genericsKey
        }.filter {
            it.text == keyText
        }
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

}