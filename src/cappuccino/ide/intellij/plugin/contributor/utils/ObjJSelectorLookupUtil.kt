package cappuccino.ide.intellij.plugin.contributor.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJSelectorInsertHandler
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.*
import org.jetbrains.annotations.Contract

import javax.swing.*

import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil.getContainingClassOrFileName

object ObjJSelectorLookupUtil {

    /**
     * Adds a selector lookup element to the completion contributor result set.
     */
    fun addSelectorLookupElement(resultSet: CompletionResultSet, selector: ObjJSelector, selectorIndex: Int, priority: Double) {
        val tailText = getSelectorLookupElementTailText(selector, selectorIndex)
        val addColonSuffix = tailText != null || selectorIndex > 0
        val containingFileOrClassName = getContainingClassOrFileName(selector)
        addSelectorLookupElement(resultSet, selector.text, containingFileOrClassName, tailText
                ?: "", priority, addColonSuffix, ObjJPsiImplUtil.getIcon(selector))

    }

    /**
     * Gets the tail text for a given element
     */
    private fun getSelectorLookupElementTailText(
            selector: ObjJSelector, selectorIndex: Int): String? {
        // Gets all selectors that come after this one
        val trailingSelectors = ObjJMethodPsiUtils.getTrailingSelectorStrings(selector, selectorIndex)
        //Creates a string builder for building the tail text
        val stringBuilder = StringBuilder(ObjJMethodPsiUtils.SELECTOR_SYMBOL)

        // Add parameter type if it exists
        val paramType = getSelectorVariableType(selector)
        if (paramType != null) {
            stringBuilder.append("(").append(paramType).append(")")
            val variableName = getSelectorVariableName(selector)
            if (variableName != null) {
                stringBuilder.append(variableName)
            }
        }

        // Add trailing selectors if any
        if (trailingSelectors.isNotEmpty()) {
            stringBuilder.append(" ").append(ArrayUtils.join(trailingSelectors, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true))
        }
        // Return tail text if any or null if empty
        return if (stringBuilder.length > 1) stringBuilder.toString() else null
    }

    /**
     * Gets the selector variable type
     */
    @Contract("null -> null")
    private fun getSelectorVariableType(selector: ObjJSelector?): String? {
        if (selector == null) {
            return null
        }
        val declarationSelector = selector.getParentOfType( ObjJMethodDeclarationSelector::class.java)
        if (declarationSelector != null) {
            return if (declarationSelector.formalVariableType != null) declarationSelector.formalVariableType!!.text else null
        }
        val instanceVariableDeclaration = selector.getParentOfType( ObjJInstanceVariableDeclaration::class.java)
        return instanceVariableDeclaration?.formalVariableType?.text
    }

    /**
     * Gets selector variable name
     */
    @Contract("null -> null")
    private fun getSelectorVariableName(selector: ObjJSelector?): String? {
        if (selector == null) {
            return null
        }
        val declarationSelector = selector.getParentOfType( ObjJMethodDeclarationSelector::class.java)
        if (declarationSelector != null) {
            return if (declarationSelector.variableName != null) declarationSelector.variableName!!.text else null
        }
        val instanceVariableDeclaration = selector.getParentOfType( ObjJInstanceVariableDeclaration::class.java)
        return if (instanceVariableDeclaration != null && instanceVariableDeclaration.variableName != null) instanceVariableDeclaration.variableName!!.text else null
    }

    /**
     * Adds selector lookup element
     * @param resultSet resultSet set
     * @param suggestedText text to suggest in completion
     * @param className containing class name used in tail text
     * @param priority se
     * @param tailText lookup element tail text
     * @param icon icon to use in completion list
     */
    @JvmOverloads
    fun addSelectorLookupElement(resultSet: CompletionResultSet, suggestedText: String, className: String?, tailText: String?, priority: Double, addSuffix: Boolean, icon: Icon? = null) {
        val selectorLookupElement = when (priority) {
            TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY, TARGETTED_METHOD_SUGGESTION_PRIORITY ->
                createSelectorLookupElement(
                        suggestedText = suggestedText,
                        className = className,
                        tailText = tailText,
                        useInsertHandler = addSuffix,
                        icon = icon).bold()
            else -> createSelectorLookupElement(
                    suggestedText = suggestedText,
                    className = className,
                    tailText = tailText,
                    useInsertHandler = addSuffix,
                    icon = icon)
        }
        val prioritizedLookupElement = PrioritizedLookupElement.withPriority(selectorLookupElement, priority)
        resultSet.addElement(prioritizedLookupElement)
    }

    /**
     * Creates a lookup element builder base for a selector
     */
    private fun createSelectorLookupElement(suggestedText: String, className: String?, tailText: String?, useInsertHandler: Boolean, icon: Icon?): LookupElementBuilder {
        var elementBuilder = LookupElementBuilder
                .create(suggestedText)
        if (tailText != null) {
            elementBuilder = elementBuilder.withTailText(tailText)
        }
        if (className != null) {
            elementBuilder = elementBuilder.withTypeText("in $className")
        }
        if (icon != null) {
            elementBuilder = elementBuilder.withIcon(icon)
        }
        if (useInsertHandler) {
            elementBuilder = elementBuilder.withInsertHandler(ObjJSelectorInsertHandler)
        }
        return elementBuilder
    }

}