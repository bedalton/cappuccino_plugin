package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeMapByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefTypeNameReference(element:JsTypeDefTypeName) : PsiPolyVariantReferenceBase<JsTypeDefTypeName>(element, TextRange(0, element.textLength)) {

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val project = element.project
        val found = JsTypeDefClassesByNamespaceIndex.instance[myElement.text, project].mapNotNull {
            it.typeName
        } + JsTypeDefTypeMapByNameIndex.instance[myElement.text, project]
        return PsiElementResolveResult.createResults(found)
    }

}