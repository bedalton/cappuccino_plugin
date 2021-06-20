package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJQuoteInsertHandler
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.psi.ObjJArguments
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJStringLiteral
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

internal fun addStringCompletions(element: ObjJStringLiteral, resultSet: CompletionResultSet) {
    val expr = element.getParentOfType(ObjJExpr::class.java)
    val arguments = expr?.parent as? ObjJArguments
    val quoteType = if (element.doubleQuoteStringLiteral != null) "\"" else "'"
    if (arguments != null) {
        addArgumentCompletion(expr, quoteType, arguments, resultSet)
    }
}

private fun addArgumentCompletion(expr:ObjJExpr, quoteType:String, arguments:ObjJArguments, resultSet:CompletionResultSet) {
    val function = arguments.parent as? ObjJFunctionCall
            ?: return
    val index = arguments.exprList.indexOf(expr);
    if (index < 0)
        return
    function.functionName?.reference?.multiResolve(true).orEmpty().forEach { resolvedFunctionName ->
        val functionDeclaration = resolvedFunctionName.element?.parent as? ObjJUniversalFunctionElement
        val functionType = when (functionDeclaration) {
            is JsTypeDefFunction -> functionDeclaration.argumentsList?.arguments
            is JsTypeDefAnonymousFunction -> functionDeclaration.argumentsList?.arguments
            else -> return@forEach
        }
        val maps = functionType?.flatMap {
            val keyOfType = it.keyOfType?.typeMapName ?: return@flatMap  emptyList<String>()
            keyOfType.reference.multiResolve(true).flatMap {
                val keys = (it.element as? JsTypeDefElement)?.getSelfOrParentOfType(JsTypeDefTypeMapElement::class.java)?.keys.orEmpty()
                keys
            }
        }.orEmpty()
        maps.forEach {
            val lookupElement = LookupElementBuilder.create("$quoteType$it$quoteType").withInsertHandler(ObjJQuoteInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it, 0.0)))
        }
    }


}