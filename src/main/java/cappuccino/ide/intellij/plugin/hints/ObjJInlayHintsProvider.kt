@file:Suppress("UnstableApiUsage")

package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.indices.ObjJClassAndSelectorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJStringLiteral
import cappuccino.ide.intellij.plugin.psi.utils.functionDeclarationReference
import cappuccino.ide.intellij.plugin.utils.ifEmptyNull
import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement

class ObjJInlayHintsProvider : InlayParameterHintsProvider {

    private val methodCallParameterHints:Option =
            Option("objj.hints.method_call_parameter_types", "Method call parameter types", false)
    private val functionParameterNameHints =
            Option("objj.hints.function_parameter_name", "Function parameter names", true)


    override fun getParameterHints(element: PsiElement): MutableList<InlayInfo> {
        val project = element.project;
        if (DumbService.isDumb(project))
            return mutableListOf()
        if (element is ObjJMethodCall) {
            return hintMethodCall(element)
        }
        if (element is ObjJFunctionCall) {
            return hintFunctionCallParameterNames(element)
        }
        return mutableListOf()
    }

    private fun hintMethodCall(element:ObjJMethodCall) : MutableList<InlayInfo> {

        // Check if method param type hints are enabled
        if (methodCallParameterHints.disabled)
            return mutableListOf()

        val project = element.project
        val callTargetTypes = element.callTarget.getPossibleCallTargetTypes(createTag())
        val selectorString = element.selectorString
        val methodSelectors = callTargetTypes.flatMap{
            ObjJClassAndSelectorMethodIndex.instance.getByClassAndSelector(it, selectorString, project)
        }.firstOrNull()?.selectorStructs ?: ObjJUnifiedMethodIndex.instance[selectorString, project].firstOrNull()?.selectorStructs ?: return mutableListOf()
        return element.qualifiedMethodCallSelectorList.mapIndexedNotNull{ i, selector ->
            val string = methodSelectors[i].variableType.ifEmptyNull() ?: return@mapIndexedNotNull null
            val offset = selector.colon.textOffset+1
            InlayInfo("($string)",  offset, false)
        }.toMutableList()
    }

    private fun hintFunctionCallParameterNames(element:ObjJFunctionCall) : MutableList<InlayInfo> {
        // Check if function param name hints are enabled
        if (functionParameterNameHints.disabled)
            return mutableListOf()
        val referencedFunction = element.functionDeclarationReference ?: return mutableListOf()
        val params = referencedFunction.parameterNames
        return element.arguments.exprList.mapIndexedNotNull { i, arg ->
            if (!arg.isLiteral())
                return@mapIndexedNotNull null
            val param = params.getOrNull(i)?.ifEmptyNull() ?: return@mapIndexedNotNull null
            InlayInfo(param, arg.textOffset, false)
        }.toMutableList()
    }

    override fun getSupportedOptions(): MutableList<Option> {
        return mutableListOf(
                functionParameterNameHints,
                methodCallParameterHints
        )
    }

    override fun getDefaultBlackList(): MutableSet<String> {
        return mutableSetOf()
    }

    override fun getHintInfo(element: PsiElement): HintInfo? {
        return null
    }

}

private fun ObjJExpr.isLiteral(): Boolean  {
    if (this.rightExprList.isNotEmpty())
        return false
    if (this.leftExpr?.primary != null || this.leftExpr?.regularExpressionLiteral != null)
        return true
    return leftExpr?.qualifiedReference?.qualifiedNameParts.orEmpty().let {
        it.size == 1 && it.firstOrNull() is ObjJStringLiteral
    }
}

private val Option.disabled get() = !isEnabled()