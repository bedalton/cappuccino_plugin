package cappuccino.ide.intellij.plugin.contributor.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJSelectorInsertHandler
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.*
import org.jetbrains.annotations.Contract

import javax.swing.*

import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil.getContainingClassOrFileName
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import com.intellij.codeInsight.lookup.LookupElementRenderer

object ObjJSelectorLookupUtil {

    fun addSelectorLookupElement(resultSet: CompletionResultSet, selector: ObjJSelector, selectorIndex: Int, priority: Double) {
        val tailText = getSelectorLookupElementTailText(selector, selectorIndex)
        val addColonSuffix = tailText != null || selectorIndex > 0
        val containingFileOrClassName = getContainingClassOrFileName(selector)
        addSelectorLookupElement(resultSet, selector.text, containingFileOrClassName, tailText
                ?: "", priority, addColonSuffix, ObjJPsiImplUtil.getIcon(selector))

    }

    private fun getSelectorLookupElementTailText(
            selector: ObjJSelector, selectorIndex: Int): String? {
        val trailingSelectors = ObjJMethodPsiUtils.getTrailingSelectorStrings(selector, selectorIndex)
        val stringBuilder = StringBuilder(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
        val paramType = getSelectorVariableType(selector)
        if (paramType != null) {
            stringBuilder.append("(").append(paramType).append(")")
            val variableName = getSelectorVariableName(selector)
            if (variableName != null) {
                stringBuilder.append(variableName)
            }
        }
        if (!trailingSelectors.isEmpty()) {
            stringBuilder.append(" ").append(ArrayUtils.join(trailingSelectors, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true))
        }
        return if (stringBuilder.length > 1) stringBuilder.toString() else null
    }

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
     * @param result result set
     * @param targetElement target element
     * @param priority se
     * @param tailText lookup element tail text
     * @param useInsertHandler use insert handler for colon placement
     * @param icon icon to use in completion list
     */
    @JvmOverloads
    fun addSelectorLookupElement(result: CompletionResultSet, targetElement: PsiElement, tailText: String?, priority: Double, useInsertHandler: Boolean, icon: Icon? = null) {
        if (targetElement !is ObjJCompositeElement) {
            return
        }
        val className = ObjJHasContainingClassPsiUtil.getContainingClassOrFileName(targetElement)
        val elementBuilder = createSelectorLookupElement(targetElement.getText(), className, tailText, useInsertHandler, icon)
        result.addElement(PrioritizedLookupElement.withPriority(elementBuilder, priority))
    }

    @JvmOverloads
    fun addSelectorLookupElement(result: CompletionResultSet, suggestedText: String, className: String?, tailText: String?, priority: Double, addSuffix: Boolean, icon: Icon? = null) {
        val selectorLookupElement = when (priority) {
            TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY, TARGETTED_METHOD_SUGGESTION_PRIORITY -> createSelectorLookupElement(suggestedText, className, tailText, addSuffix, icon).bold()
            else -> createSelectorLookupElement(suggestedText, className, tailText, addSuffix, icon)
        }
        result.addElement(PrioritizedLookupElement.withPriority(selectorLookupElement, priority))
    }

    fun createSelectorLookupElement(suggestedText: String, className: String?, tailText: String?, useInsertHandler: Boolean, icon: Icon?): LookupElementBuilder {
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
            elementBuilder = elementBuilder.withInsertHandler(ObjJSelectorInsertHandler.instance)
        }
        return elementBuilder
    }

}
/**
 * Adds a selector lookup element
 * @param result result set
 * @param targetElement target element
 * @param priority se
 * @param tailText lookup element tail text
 * @param useInsertHandler use insert handler for colon placement
 */