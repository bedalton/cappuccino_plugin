package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.inference.anyTypes
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

object ObjJFunctionNameCompletionProvider {

    fun appendCompletionResults(resultSet: CompletionResultSet, element: PsiElement) {
        addAllGlobalJSFunctionNames(resultSet, element.project, (element.textWithoutCaret.length > 5))
        addAllLocalFunctionNames(resultSet, element)
        addIndexBasedCompletions(resultSet, element)

        if (true || element.node.getPreviousNonEmptyNode(true)?.text == "new") {
            JsTypeDefClassesByNamespaceIndex.instance.getByPatternFlat(element.text.toIndexPatternString(), element.project).mapNotNull{
                (it as? JsTypeDefClassElement)?.className
            }.forEach {
                val lookupElement = LookupElementBuilder.create(it).withInsertHandler(ObjJFunctionNameInsertHandler)
                val prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it, ObjJCompletionContributor.TYPEDEF_PRIORITY))
                resultSet.addElement(prioritizedLookupElement)
            }
        }
    }

    private fun addIndexBasedCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val ignoreFunctionPrefixedWithUnderscore = ObjJPluginSettings.ignoreUnderscoredClasses
        val functionNamePattern = element.text.toIndexPatternString()
        val functionsRaw = ObjJFunctionsIndex.instance.getByPatternFlat(functionNamePattern, element.project)
        val functions = functionsRaw.filter {
            when (it.functionScope) {
                ObjJFunctionScope.GLOBAL_SCOPE -> true
                ObjJFunctionScope.FILE_SCOPE -> element.parent.isEquivalentTo(it.containingFile)
                else -> false
            }
        }
        for (function in functions) {
            ProgressIndicatorProvider.checkCanceled()
            val functionName = function.functionNameString
            val shouldPossiblyIgnore = ignoreFunctionPrefixedWithUnderscore && functionName.startsWith("_")
            if (shouldPossiblyIgnore && element.containingFile != function.containingFile)
                continue
            val priority = if (PsiTreeUtil.findCommonContext(function, element) != null) ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY else ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(function.parameterNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(functionName, priority)))
        }
    }

    private fun addAllLocalFunctionNames(resultSet: CompletionResultSet, element: PsiElement) {
        val functions = element.getParentBlockChildrenOfType(ObjJFunctionDeclarationElement::class.java, true).toMutableList()
        functions.addAll(element.containingFile.getChildrenOfType(ObjJFunctionDeclarationElement::class.java))
        for (function in functions) {
            val functionName = function.functionNameNode?.text ?: continue
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(function.parameterNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(functionName, ObjJCompletionContributor.TYPEDEF_PRIORITY)))
        }
    }

    private fun addAllGlobalJSFunctionNames(resultSet: CompletionResultSet, project:Project, showEvenSkipped:Boolean) {
        val functions = if (showEvenSkipped) {
            JsTypeDefFunctionsByNameIndex.instance.getAll(project).filterNot{ it.isSilent}
        } else {
            JsTypeDefFunctionsByNameIndex.instance.getAll(project, GlobalSearchScope.allScope(project)).filterNot {
                it.isSilent || it.isQuiet
            }
        }.filter {
            it.enclosingNamespaceComponents.isEmpty()
        }

        for (functionIn in functions) {
            val function = functionIn.toJsFunctionType()
            val functionName = function.name ?: continue
            val arguments = StringBuilder()
            function.parameters.forEach {
                arguments.append(", ").append(it.name)
                val type = it.types.toString()
                val nullable = it.nullable
                if (type.isNotNullOrBlank() && type !in anyTypes) {
                    arguments.append(":").append(type)
                }
                if (nullable.orFalse()) {
                    arguments.append("?")
                }
            }
            val argumentsString = if (arguments.length > 2)
                arguments.substring(2)
            else
                ""
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + argumentsString + ") in [" + ObjJPsiImplUtil.getFileName(functionIn)+"]")
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(functionName, ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY)))

        }
    }

    internal fun addGlobalFunctionName(resultSet:CompletionResultSet, functionName:String, priority: Double = ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY) {
        val lookupElementBuilder = LookupElementBuilder
                .create(functionName)
                .withInsertHandler(ObjJFunctionNameInsertHandler)
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(functionName, priority)))
    }
}