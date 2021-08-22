package cappuccino.ide.intellij.plugin.contributor.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJMethodInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJSelectorInsertHandler
import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclarationSelector
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil.getContainingClassOrFileName
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getTrailingSelectorStrings
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.subList
import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Contract
import javax.swing.Icon

/**
 * Utility for looking up possible selector completions
 */
object ObjJSelectorLookupUtil {

    private val getterAccessorPropertyTypes = listOf("getter", "readonly", "copy", "property")
    private val setterAccessorPropertyTypes = listOf("setter", "property")


    /**
     * Adds a selector lookup element to the completion contributor result set.
     */
    fun addSelectorLookupElement(
        resultSet: CompletionResultSet,
        selector: ObjJSelector,
        selectorIndex: Int,
        priority: Double,
        addSpaceAfterColon: Boolean,
    ) {
        if (addAccessors(resultSet, selector, selectorIndex, priority)) {
            return
        }
        addSelectorLookupElement(resultSet = resultSet,
            selector = selector,
            isGetter = false,
            selectorIndex = selectorIndex,
            priority = priority,
            addSpaceAfterColon = addSpaceAfterColon)

    }

    /**
     * Adds a selector lookup element while specifying if it is a getter or not
     */
    private fun addSelectorLookupElement(
        resultSet: CompletionResultSet,
        selector: ObjJSelector,
        isGetter: Boolean,
        selectorIndex: Int,
        priority: Double,
        addSpaceAfterColon: Boolean,
    ) {
        val tailText = getSelectorLookupElementTailText(selector, isGetter, selectorIndex)
        val addColonSuffix = !isGetter && (tailText != null || selectorIndex > 0)
        val containingFileOrClassName = getContainingClassOrFileName(selector)
        addSelectorLookupElement(
            resultSet = resultSet,
            suggestedText = selector.text,
            className = containingFileOrClassName,
            tailText = tailText ?: "",
            priority = priority,
            addSuffix = addColonSuffix,
            addSpaceAfterColon = addSpaceAfterColon,
            icon = ObjJPsiImplUtil.getIcon(selector))
    }

    /**
     * Adds an accessor method, branching if is getter, setter, or both
     */
    private fun addAccessors(
        resultSet: CompletionResultSet,
        selector: ObjJSelector,
        selectorIndex: Int,
        priority: Double,
    ): Boolean {
        if (selectorIndex != 0) return false
        val isGetter = isGetterAccessor(selector)
        if (isGetter) {
            addSelectorLookupElement(resultSet = resultSet,
                selector = selector,
                isGetter = true,
                selectorIndex = selectorIndex,
                priority = priority,
                addSpaceAfterColon = false)
        }
        val isSetter = isSetterAccessor(selector)
        if (isSetter) {
            addSelectorLookupElement(resultSet = resultSet,
                selector = selector,
                isGetter = false,
                selectorIndex = selectorIndex,
                priority = priority,
                addSpaceAfterColon = false)
        }
        return isGetter || isSetter
    }

    /**
     * Gets the tail text for a given element
     */
    private fun getSelectorLookupElementTailText(
        selector: ObjJSelector, isGetter: Boolean, selectorIndex: Int,
    ): String? {
        // Gets all selectors that come after this one
        val trailingSelectors = getTrailingSelectorStrings(selector, selectorIndex)
        //Creates a string builder for building the tail text
        val stringBuilder = StringBuilder(ObjJMethodPsiUtils.SELECTOR_SYMBOL)

        // Add parameter type if it exists
        val parameterType = getSelectorVariableType(selector)
        if (parameterType != null) {
            if (isGetter) {
                return stringBuilder.append(parameterType).toString()
            }
            stringBuilder.append("(").append(parameterType).append(")")
            val variableName = getSelectorVariableName(selector)
            if (variableName != null) {
                stringBuilder.append(variableName)
            }
        }

        // Add trailing selectors if any
        if (trailingSelectors.isNotEmpty()) {
            stringBuilder.append(" ")
                .append(ArrayUtils.join(trailingSelectors, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true))
        }
        // Return tail text if any or null if empty
        return if (stringBuilder.length > 1) stringBuilder.toString() else null
    }

    /**
     * Determines whether accessor flags for selector include a getter
     */
    private fun isGetterAccessor(selector: ObjJSelector): Boolean {
        val property: ObjJAccessorProperty = selector.getParentOfType(ObjJAccessorProperty::class.java) ?: return false
        return property.accessorPropertyType.text in getterAccessorPropertyTypes
    }

    /**
     * Determines whether accessor flags for selector include a setter
     */
    private fun isSetterAccessor(selector: ObjJSelector): Boolean {
        val property: ObjJAccessorProperty = selector.getParentOfType(ObjJAccessorProperty::class.java) ?: return false
        return property.accessorPropertyType.text in setterAccessorPropertyTypes
    }

    /**
     * Gets the selector variable type
     */
    @Contract("null -> null")
    private fun getSelectorVariableType(selector: ObjJSelector?): String? {
        if (selector == null) {
            return null
        }
        val declarationSelector = selector.getParentOfType(ObjJMethodDeclarationSelector::class.java)
        if (declarationSelector != null) {
            return if (declarationSelector.formalVariableType != null) declarationSelector.formalVariableType!!.text else null
        }
        val instanceVariableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
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
        val declarationSelector = selector.getParentOfType(ObjJMethodDeclarationSelector::class.java)
        if (declarationSelector != null) {
            return if (declarationSelector.variableName != null) declarationSelector.variableName!!.text else null
        }
        val instanceVariableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
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
    fun addSelectorLookupElement(
        resultSet: CompletionResultSet,
        suggestedText: String,
        className: String?,
        tailText: String?,
        priority: Double,
        addSuffix: Boolean,
        addSpaceAfterColon: Boolean,
        icon: Icon? = null,
    ) {
        ProgressIndicatorProvider.checkCanceled()
        val selectorLookupElement = when (priority) {
            TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY, TARGETTED_METHOD_SUGGESTION_PRIORITY ->
                createSelectorLookupElement(
                    suggestedText = suggestedText,
                    className = className,
                    tailText = tailText,
                    useInsertHandler = addSuffix,
                    addSpaceAfterColon = addSpaceAfterColon,
                    icon = icon).bold()
            else -> createSelectorLookupElement(
                suggestedText = suggestedText,
                className = className,
                tailText = tailText,
                useInsertHandler = addSuffix,
                addSpaceAfterColon = addSpaceAfterColon,
                icon = icon)
                .withRenderer(renderer(suggestedText, className, tailText, icon))
        }
        val prioritizedLookupElement = PrioritizedLookupElement.withPriority(selectorLookupElement, priority)
        resultSet.addElement(prioritizedLookupElement)
    }

    private fun renderer(
        suggestedText: String,
        className: String?,
        tailText: String?,
        icon: Icon? = null,
    ): LookupElementRenderer<LookupElement> {
        return object : LookupElementRenderer<LookupElement>() {
            override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
                presentation.itemText = suggestedText
                if (tailText != null)
                    presentation.tailText = tailText
                if (className != null)
                    presentation.typeText = "in $className"
                if (icon != null)
                    presentation.icon = icon
                presentation.isTypeGrayed = true
            }
        }
    }

    /**
     * Creates a lookup element builder base for a selector
     */
    private fun createSelectorLookupElement(
        suggestedText: String,
        className: String?,
        tailText: String?,
        useInsertHandler: Boolean,
        addSpaceAfterColon: Boolean,
        icon: Icon?,
    ): LookupElementBuilder {
        ProgressIndicatorProvider.checkCanceled()
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
            elementBuilder = elementBuilder.withInsertHandler(ObjJSelectorInsertHandler(addSpaceAfterColon))
        }
        return elementBuilder
    }


    fun addSelectorLookupElement(
        file: PsiFile,
        resultSet: CompletionResultSet,
        selectorStructs: List<ObjJSelectorStruct>,
        priority: Double? = null,
        icon: Icon? = null,
    ) {
        val lookupElement = createSelectorLookupElement(file, selectorStructs, icon)
            ?: return
        if (priority != null) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, priority))
        } else {
            resultSet.addElement(lookupElement)
        }
    }

    /**
     * Creates a lookup element builder base for a selector
     */
    private fun createSelectorLookupElement(
        file: PsiFile,
        selectorList: List<ObjJSelectorStruct>,
        icon: Icon? = null
    ): LookupElementBuilder? {
        if (selectorList.isEmpty())
            return null
        val addSpace = CodeStyle.getCustomSettings(file,
            ObjJCodeStyleSettings::class.java).SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL
        val firstSelector = selectorList[0]
        ProgressIndicatorProvider.checkCanceled()
        var elementBuilder = LookupElementBuilder
            .create(selectorList.get(0).selector)
            .withTailText(":" + selectorList.subList(1).joinToString(":") { it.selector })
        if (firstSelector.isContainerAClass) {
            elementBuilder = elementBuilder.withTypeText("in ${firstSelector.containerName}")
        }
        if (icon != null) {
            elementBuilder = elementBuilder.withIcon(icon)
        }
        elementBuilder = elementBuilder.withInsertHandler(ObjJMethodInsertHandler(selectorList = selectorList,
            spaceAfterSelector = addSpace))
        return elementBuilder
    }

}