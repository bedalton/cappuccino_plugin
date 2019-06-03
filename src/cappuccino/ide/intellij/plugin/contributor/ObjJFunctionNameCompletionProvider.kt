package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger

object ObjJFunctionNameCompletionProvider {

    fun appendCompletionResults(resultSet: CompletionResultSet, element: PsiElement) {
        val functionNamePattern = element.text.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR, "(.*)")
        addAllGlobalJSFunctionNames(resultSet, (functionNamePattern.length - 5) > 8)
        addAllLocalFunctionNames(resultSet, element)
        addIndexBasedCompletions(resultSet, element);

        if (element.node.getPreviousNonEmptyNode(true)?.text == "new") {
            globalJSClassNames.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }
    }

    private fun addIndexBasedCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val ignoreFunctionPrefixedWithUnderscore = ObjJPluginSettings.ignoreUnderscoredClasses
        val functionNamePattern = element.text.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR, "(.*)")
        val functionsRaw = ObjJFunctionsIndex.instance.getByPattern(functionNamePattern, element.project)
        val functions = functionsRaw.flatMap { it.value }
                .filter {
                    when (it.functionScope) {
                        ObjJFunctionScope.GLOBAL_SCOPE -> true
                        ObjJFunctionScope.FILE_SCOPE -> element.parent.isEquivalentTo(it.containingFile)
                        else -> false
                    }
                }
        for (function in functions) {
            ProgressIndicatorProvider.checkCanceled()
            val functionName = function.functionNameAsString
            val shouldPossiblyIgnore = ignoreFunctionPrefixedWithUnderscore && functionName.startsWith("_")
            if (shouldPossiblyIgnore && !element.containingFile.isEquivalentTo(function.containingFile))
                continue
            val priority = if (PsiTreeUtil.findCommonContext(function, element) != null) ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY else ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(function.paramNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
        }
    }

    private fun addAllLocalFunctionNames(resultSet: CompletionResultSet, element: PsiElement) {
        val functions = element.getParentBlockChildrenOfType(ObjJFunctionDeclarationElement::class.java, true).toMutableList()
        functions.addAll(element.containingFile.getChildrenOfType(ObjJFunctionDeclarationElement::class.java))
        for (function in functions) {
            val functionName = function.functionNameNode?.text ?: continue
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(function.paramNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY ))
        }
    }

    private fun addAllGlobalJSFunctionNames(resultSet: CompletionResultSet, showEvenSkipped:Boolean) {
        val functions = if (showEvenSkipped) {
            Logger.getLogger("#ObjJFunctionNameCompletionProvider").info("Showing even skipped")
            globalJsFunctionNames
        } else {
            Logger.getLogger("#ObjJFunctionNameCompletionProvider").info("Not showing skipped")
            globalJsFunctionNamesMinusSkips
        }

        for (function in functions) {
            addGlobalFunctionName(resultSet, function)
        }
    }

    internal fun addGlobalFunctionName(resultSet:CompletionResultSet, functionName:String, priority: Double = ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY) {
        val lookupElementBuilder = LookupElementBuilder
                .create(functionName)
                .withInsertHandler(ObjJFunctionNameInsertHandler)
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
    }
}