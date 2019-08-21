package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJBlanketCompletionProvider
import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJTrackInsertionHandler
import cappuccino.ide.intellij.plugin.contributor.toIndexPatternString
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByPartialNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefKeyListsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefQualifiedTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefType
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
            element is JsTypeDefTypeName || element.parent is JsTypeDefTypeName -> {
                val typeName = element as? JsTypeDefTypeName ?: element.parent as JsTypeDefTypeName
                val previousSiblings = typeName.previousSiblings
                if (previousSiblings.isNotEmpty()) {
                    addQualifiedNameCompletions(resultSet, project, typeName, previousSiblings)
                    return
                }
                else {
                    addTypeNameCompletions(resultSet, project, typeName.text.toIndexPatternString())
                }
            }
        }
    }


    /**
     * Add Completions for class types
     */
    private fun addTypeNameCompletions(resultSet: CompletionResultSet, project: Project, indexSearchString:String) {
        // Add Js Completions
        val jsCompletions = JsTypeDefClassesByNamespaceIndex.instance.getKeysByPattern(indexSearchString, project) + JsPrimitives.primitives
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

    private fun addQualifiedNameCompletions(resultSet: CompletionResultSet, project: Project, typeName:JsTypeDefTypeName, previousSiblings:List<JsTypeDefTypeName>) {
        val namespacePrefix = previousSiblings.joinToString(".") { it.text }
        val classes = JsTypeDefClassesByPartialNamespaceIndex.instance[namespacePrefix, project]
        val index = previousSiblings.size
        val out = classes.mapNotNull {
            val namespaceComponents = it.namespaceComponents
            if (namespaceComponents.size > index) {
                namespaceComponents.get(index)
            } else {
                namespaceComponents.lastOrNull()
            }
        }
        addLookupElementsSimple(resultSet, out, JsTypeDefCompletionContributor.JS_CLASS_NAME_COMPLETIONS)
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