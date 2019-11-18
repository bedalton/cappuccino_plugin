package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefKeyListsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeMapByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class JsTypeDefTypeNameReference(element:JsTypeDefTypeName) : PsiPolyVariantReferenceBase<JsTypeDefTypeName>(element, TextRange(0, element.textLength)) {

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        return findClassReferencesByName(myElement.text, myElement.project)
    }



    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

    companion object {
        fun findClassReferencesByName(typeName:String, project:Project): Array<ResolveResult> {
            val fromJsTypeDefClass = JsTypeDefClassesByNamespaceIndex.instance[typeName, project].mapNotNull {
                it.typeName
            }

            val fromTypeMap = JsTypeDefTypeMapByNameIndex.instance[typeName, project].mapNotNull {
                it.typeMapName
            }

            val fromTypeAlias = JsTypeDefTypeAliasIndex.instance.get(typeName, project).mapNotNull { it.typeName }

            val fromObjJClass = ObjJClassDeclarationsIndex.instance.get(typeName, project).mapNotNull { it.getClassName() }

            val fromKeyList = JsTypeDefKeyListsByNameIndex.instance.get(typeName, project).mapNotNull { it.keyName }
            val fromObjJTypeDefElement = ObjJTypeDefIndex.instance.get(typeName, project).mapNotNull {
                it.className
            }
            val found:List<PsiElement> = fromJsTypeDefClass +
                    fromTypeMap +
                    fromTypeAlias +
                    fromObjJClass +
                    fromKeyList +
                    fromObjJTypeDefElement
            return PsiElementResolveResult.createResults(found)
        }
    }

}
