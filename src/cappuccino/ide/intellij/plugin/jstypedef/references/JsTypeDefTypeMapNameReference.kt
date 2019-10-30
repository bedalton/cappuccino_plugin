package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeMapByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefMapType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapName
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefTypeMapNameReference(element:JsTypeDefTypeMapName) : PsiPolyVariantReferenceBase<JsTypeDefTypeMapName>(element, TextRange(0, element.text.length)) {

    private val isDeclaration:Boolean by lazy {
        element.parent is JsTypeDefTypeMapElement
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element.text != this.element.text)
            return false
        if (element !is JsTypeDefTypeMapName)
            return false
        if (element.parent is JsTypeDefTypeMapElement)
            return false
        return true
    }

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val project = element.project
        if (isDeclaration) {
            return PsiElementResolveResult.createResults(element)
        }
        val found = JsTypeDefTypeMapByNameIndex.instance[element.text, project]
        if (found.isNotEmpty()) {
            return PsiElementResolveResult.createResults(found)
        }
        val classesWithName = JsTypeDefClassesByNamespaceIndex.instance[myElement.text, project].mapNotNull {
            it.typeName
        }
        if (classesWithName.isNotEmpty()) {
            return PsiElementResolveResult.createResults(classesWithName)
        }
        return PsiElementResolveResult.EMPTY_ARRAY
    }


    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

}