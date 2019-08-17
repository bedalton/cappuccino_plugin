package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeMapByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefArgument
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefKeyValuePair
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJStringLiteral
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class ObjJStringLiteralReference(stringLiteral:ObjJStringLiteral)  : PsiPolyVariantReferenceBase<ObjJStringLiteral>(stringLiteral, TextRange(0, stringLiteral.textLength)) {

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val project = element.project
        val arguments = arguments ?: return PsiElementResolveResult.EMPTY_ARRAY
        val stringValue = element.stringValue
        if (stringValue.isBlank())
            return PsiElementResolveResult.EMPTY_ARRAY
        val stringLiteralArguments = arguments.mapNotNull { it.propertyName.stringLiteral }.filter { it.stringValue == stringValue }
        if (stringLiteralArguments.isNotEmpty())
            return PsiElementResolveResult.createResults(stringLiteralArguments)
        val typeMapKeys = arguments.mapNotNull{ it.keyOfType?.typeMapName?.text }.flatMap {
            collapseKeyValuePairs(project, it)
        } .map { it.stringLiteral }.filter {
            it.stringValue == stringValue
        }
        return if (typeMapKeys.isNotNullOrEmpty()) {
            PsiElementResolveResult.createResults(typeMapKeys)
        } else {
            PsiElementResolveResult.EMPTY_ARRAY
        }
    }

    private val arguments: List<JsTypeDefArgument>? get() {
        val expr = element.getParentOfType(ObjJExpr::class.java) ?: return null
        val functionCallParent = expr.parent as? ObjJFunctionCall ?: return  null
        val resolved = functionCallParent.functionName?.reference?.multiResolve(false)
                .orEmpty()
                .mapNotNull {
                    (it.element as? JsTypeDefFunctionName)?.parent as? JsTypeDefFunction
                }
        if (resolved.isEmpty())
            return null

        var indexOfExpr = -1
        functionCallParent.exprList.forEachIndexed { i, element ->
            if (element.isEquivalentTo(expr)) {
                indexOfExpr = i
                return@forEachIndexed
            }
        }
        if (indexOfExpr < 0)
            return null
        val result = resolved.mapNotNull { function ->
            val argument = function.argumentsList?.arguments?.getOrNull(indexOfExpr) ?: return null
            if (argument.keyOfType == null && argument.propertyName.stringLiteral == null)
                return@mapNotNull null
            argument
        }
        if (result.isEmpty())
            return null
        return result
    }

}

fun getSuperTypes(project:Project, className:String, out:MutableList<String>): Set<String> {
    val classElements = JsTypeDefTypeMapByNameIndex.instance[className, project]
    for (classElement in classElements) {
        classElement.extendsStatement?.typeList?.mapNotNull { it.typeName?.text }.orEmpty().forEach{
            if (it in out)
                return@forEach
            out.add(it)
            getSuperTypes(project, it, out)
        }
    }
    return out.toSet()
}

fun collapseKeyValuePairs(project:Project, classNameIn:String):List<JsTypeDefKeyValuePair> {
    val allClasses = getSuperTypes(project, classNameIn, mutableListOf()) + classNameIn
    return allClasses.flatMap {className ->
        JsTypeDefTypeMapByNameIndex.instance[className, project].flatMap {
            it.keyValuePairList
        }
    }
}