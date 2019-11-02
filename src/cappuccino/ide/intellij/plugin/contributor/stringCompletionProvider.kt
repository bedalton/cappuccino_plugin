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
    LOGGER.info("Adding completions to string literal")
    val function = arguments.parent as? ObjJFunctionCall
            ?: return
    LOGGER.info("Is in function call")
    val index = arguments.exprList.indexOf(expr);
    if (index < 0)
        return
    LOGGER.info("Is in argument list")
    function.functionName?.reference?.multiResolve(true).orEmpty().forEach { resolvedFunctionName ->
        val functionDeclaration = resolvedFunctionName.element?.parent as? ObjJUniversalFunctionElement
        LOGGER.info("Checking if js function declaration")
        val functionType = when (functionDeclaration) {
            is JsTypeDefFunction -> functionDeclaration.argumentsList?.arguments
            is JsTypeDefAnonymousFunction -> functionDeclaration.argumentsList?.arguments
            else -> return@forEach
        }
        LOGGER.info("Adding completions for function declaration")
        val maps = functionType?.flatMap {
            val keyOfType = it.keyOfType?.typeMapName ?: return@flatMap  emptyList<String>()
            LOGGER.info("Is key of type: ${keyOfType.text}")
            keyOfType.reference.multiResolve(true).flatMap {
                val keys = (it.element as? JsTypeDefElement)?.getSelfOrParentOfType(JsTypeDefTypeMapElement::class.java)?.keys.orEmpty()
                LOGGER.info("Resolving map type, Key: $keys")
                keys
            }
        }.orEmpty()
        maps.forEach {
            LOGGER.info("ADDING COMPLETION: $it")
            val lookupElement = LookupElementBuilder.create("$quoteType$it$quoteType").withInsertHandler(ObjJQuoteInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it, 0.0)))
        }
    }


}