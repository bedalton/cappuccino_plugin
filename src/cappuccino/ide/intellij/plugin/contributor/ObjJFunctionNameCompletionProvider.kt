package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
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
        val functionNamePattern = element.text.toIndexPatternString()
        addAllGlobalJSFunctionNames(resultSet, element.project, (functionNamePattern.length - 5) > 8)
        addAllLocalFunctionNames(resultSet, element)
        addIndexBasedCompletions(resultSet, element)

        if (element.node.getPreviousNonEmptyNode(true)?.text == "new") {
            JsTypeDefClassesByNamespaceIndex.instance.getByPatternFlat(element.text.toIndexPatternString(), element.project).mapNotNull{
                (it as? JsTypeDefClassElement)?.className
            }.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
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
            val functionName = function.functionNameAsString
            val shouldPossiblyIgnore = ignoreFunctionPrefixedWithUnderscore && functionName.startsWith("_")
            if (shouldPossiblyIgnore && element.containingFile != function.containingFile)
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

    private fun addAllGlobalJSFunctionNames(resultSet: CompletionResultSet, project:Project, showEvenSkipped:Boolean) {
        val functions = if (showEvenSkipped) {
            JsTypeDefFunctionsByNameIndex.instance.getAll(project).filter{ it.atSilent == null}
        } else {
            JsTypeDefFunctionsByNameIndex.instance.getAll(project, GlobalSearchScope.allScope(project)).filter {
                it.atSilent == null && it.atQuiet == null
            }
        }.filter {
            it.enclosingNamespaceComponents.isEmpty()
        }.map { it.functionNameString }

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