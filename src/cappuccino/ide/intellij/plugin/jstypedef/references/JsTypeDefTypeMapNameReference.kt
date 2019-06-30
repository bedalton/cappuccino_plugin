package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeMapByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefTypeMapNameReference(element:JsTypeDefTypeMapName) : PsiPolyVariantReferenceBase<JsTypeDefTypeMapName>(element, TextRange(0, element.textLength)) {

    val isDeclaration:Boolean by lazy {
        element.parent is JsTypeDefTypeMapElement
    }

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val project = element.project
        if (isDeclaration)
            return PsiElementResolveResult.createResults(element)
        val found = JsTypeDefTypeMapByNameIndex.instance[myElement.text, project]
        if (found.isNotEmpty())
            return PsiElementResolveResult.createResults(found)
        val classesWithName = JsTypeDefClassesByNamespaceIndex.instance[myElement.text, project].mapNotNull {
            it.typeName
        }
        if (classesWithName.isNotEmpty()) {
            return PsiElementResolveResult.createResults(classesWithName)
        }
        return PsiElementResolveResult.EMPTY_ARRAY
    }

}