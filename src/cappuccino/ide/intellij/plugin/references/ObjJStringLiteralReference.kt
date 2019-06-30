package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJStringLiteral
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

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
        val typeMapNames = arguments.flatMap { it.keyOfType?.typeMapName?.reference?.multiResolve(false).orEmpty().map { it.element } }
        val typeMapKeys = typeMapNames.mapNotNull { it.getParentOfType(JsTypeDefTypeMapElement::class.java) }.flatMap {
            it.keyValuePairs.filter { it.key == stringValue}
        }
        val interfaceKeys = typeMapNames.mapNotNull { it.getParentOfType(JsTypeDefClassDeclaration::class.java) }.flatMap {
            it.propertyList.mapNotNull { it.stringLiteral }.filter { it.stringValue == stringValue }
        }
        val out:List<JsTypeDefElement> = typeMapKeys + interfaceKeys
        return if (out.isNotEmpty()) {
            PsiElementResolveResult.createResults(out)
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