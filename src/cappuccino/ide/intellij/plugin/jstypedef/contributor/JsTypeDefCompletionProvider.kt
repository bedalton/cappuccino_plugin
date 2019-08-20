package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJBlanketCompletionProvider
import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJTrackInsertionHandler
import cappuccino.ide.intellij.plugin.contributor.toIndexPatternString
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefKeyListsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefQualifiedTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.util.ProcessingContext
import java.util.logging.Logger

object JsTypeDefCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val DOT_REPLACEMENT = "______DOT_____";

    private val LOGGER by lazy {
        Logger.getLogger(ObjJBlanketCompletionProvider::class.java.name)
    }


    private val doNotCompleteAfter = listOf(
            JS_CLASS_KEYWORD,
            JS_INTERFACE,
            JS_TYPE_MAP_KEYWORD,
            JS_KEYS_KEYWORD
    )


    /**
     * Add all possible completions to result set
     */
    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        val element = parameters.position
        val project = element.project
        when {
            element.parent is JsTypeDefQualifiedTypeName || element.parent.parent is JsTypeDefQualifiedTypeName -> {
                val qualifiedTypeName = (element.parent as? JsTypeDefQualifiedTypeName) ?: (element.parent.parent as JsTypeDefQualifiedTypeName)
                val qualifiedNameSearchString = qualifiedTypeName.text
                addTypeNameCompletions(resultSet, project, qualifiedNameSearchString)
                resultSet.stopHere()
            }
            element.parent is JsTypeDefTypeName -> {
                val prevSibling = element.getPreviousNonEmptySibling(true)
                val prevSiblingType = prevSibling?.elementType
                if (prevSiblingType in doNotCompleteAfter) {
                    resultSet.stopHere()
                    return
                }
                addTypeNameCompletions(resultSet, project, element.text)
                resultSet.stopHere()
            }
        }
    }


    /**
     * Add Completions for class types
     */
    private fun addTypeNameCompletions(resultSet: CompletionResultSet, project: Project, inputString:String) {
        // Add Js Completions
        val isQualified = inputString.contains(".")
        val indexSearchString = if (isQualified)
            inputString.replace(".", DOT_REPLACEMENT).toIndexPatternString().replace(DOT_REPLACEMENT, "\\.")
        else
            inputString.toIndexPatternString()
        LOGGER.info(indexSearchString)
        val primitives =  if (isQualified) emptyList() else JsPrimitives.primitives
        val jsCompletions = JsTypeDefClassesByNamespaceIndex.instance.getKeysByPattern(indexSearchString, project) + inputString
        addLookupElementsSimple(resultSet, jsCompletions, JsTypeDefCompletionContributor.JS_CLASS_NAME_COMPLETIONS)

        // Add Js Completions
        // But add at lower priority
        val objjCompletions = ObjJClassDeclarationsIndex.instance.getKeysByPattern(indexSearchString, project)
        addLookupElementsSimple(resultSet, objjCompletions, JsTypeDefCompletionContributor.ObjJ_CLASS_NAME_COMPLETIONS)

        val objjTypeDefCompletions = ObjJTypeDefIndex.instance.getKeysByPattern(indexSearchString, project)
        addLookupElementsSimple(resultSet, objjTypeDefCompletions, JsTypeDefCompletionContributor.ObjJ_AT_TYPEDEF_NAME_COMPLETIONS)

        val keysets = JsTypeDefKeyListsByNameIndex.instance.getKeysByPattern(indexSearchString, project)
        addLookupElementsSimple(resultSet, keysets, JsTypeDefCompletionContributor.JS_KEYSET_NAME_COMPLETIONS)

        val aliases = JsTypeDefTypeAliasIndex.instance.getAllKeys(project).mapNotNull { it }
        addLookupElementsSimple(resultSet, aliases, JsTypeDefCompletionContributor.JS_KEYSET_NAME_COMPLETIONS + 10)
    }

    /**
     * Adds a list of elements as simple types
     */
    private fun addLookupElementsSimple(resultSet: CompletionResultSet, lookupElements: List<String>, priority: Double) {
        lookupElements.forEach {
            addLookupElementSimple(resultSet, it, priority)
        }
    }

    /**
     * Adds a string value as a lookup element
     */
    private fun addLookupElementSimple(resultSet: CompletionResultSet, lookupString: String, priority: Double) {
        val lookupElementBuilder = LookupElementBuilder.create(lookupString).withInsertHandler(ObjJTrackInsertionHandler)
        val lookupElement = PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(lookupString,priority))
        resultSet.addElement(lookupElement)
    }
}