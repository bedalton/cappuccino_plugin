package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

object ObjJFunctionNameCompletionProvider {

    fun appendCompletionResults(resultSet: CompletionResultSet, element: PsiElement) {
        val functionNamePattern = element.text.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR, "(.*)")
        val functions = ObjJFunctionsIndex.instance.getByPattern(functionNamePattern, element.project)
        val ignoreFunctionPrefixedWithUnderscore = ObjJPluginSettings.ignoreUnderscoredClasses
        for (functionName in functions.keys) {
            val shouldPossiblyIgnore = ignoreFunctionPrefixedWithUnderscore && functionName.startsWith("_")
            for (function in functions.getValue(functionName)) {
                if (shouldPossiblyIgnore && !element.containingFile.isEquivalentTo(function.containingFile))
                    continue
                ProgressIndicatorProvider.checkCanceled()
                val priority = if (PsiTreeUtil.findCommonContext(function, element) != null) ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY else ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY

                val lookupElementBuilder = LookupElementBuilder
                        .create(functionName)
                        .withTailText("(" + ArrayUtils.join(function.paramNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                        .withInsertHandler(ObjJFunctionNameInsertHandler)
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
            }
        }
        addAllGlobalJSFIles(resultSet)
        addAllLocalFunctionNames(resultSet, element)
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

    private fun addAllGlobalJSFIles(resultSet: CompletionResultSet) {
        for (function in globalJsFunctions.filterNot { it.skipCompletion || it.functionName.isEmpty() }) {
            addGlobalFunctionName(resultSet, function.functionName)
        }
    }

    private fun addGlobalFunctionName(resultSet:CompletionResultSet, functionName:String) {
        val priority = ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY
        val lookupElementBuilder = LookupElementBuilder
                .create(functionName)
                .withInsertHandler(ObjJFunctionNameInsertHandler)
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
    }
}