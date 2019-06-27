package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.CARET_INDICATOR
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.GENERIC_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil
import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icons.ObjJIcons
import java.util.logging.Level
import java.util.logging.Logger

object ObjJSelectorLiteralCompletionContributor {

    private val LOGGER = Logger.getLogger(ObjJSelectorLiteralCompletionContributor::class.java.name)

    /**
     * Method to manage adding selector lookup elements to result set
     * @param result completion result set
     * @param psiElement currently editing psi element
     */
    internal fun addSelectorLookupElementsFromSelectorList(
            result: CompletionResultSet,
            psiElement: PsiElement?) {
        if (psiElement == null) {
            LOGGER.log(Level.SEVERE, "Cannot add selector lookup elements. Selector element is null")
            return
        }
        val selectorLiteral = psiElement.getSelfOrParentOfType(ObjJSelectorLiteral::class.java)
                ?: //LOGGER.log(Level.INFO, "Cannot get completion parameters. Method selector is null.");
                return
        addSelectorLiteralCompletions(result, psiElement, selectorLiteral)
    }

    private fun addSelectorLiteralCompletions(result: CompletionResultSet, psiElement: PsiElement, parentSelectorLiteral: ObjJSelectorLiteral?) {

        //LOGGER.log(Level.INFO, "Add method literal completions");
        if (parentSelectorLiteral == null) {
            LOGGER.log(Level.SEVERE, "Cannot add selector literal completions. Selector literal parent element is null")
            return
        }
        if (DumbService.isDumb(psiElement.project)) {
            return
        }

        val selectors: List<ObjJSelector> = getSelectorsFromIncompleteSelectorLiteral(psiElement, parentSelectorLiteral)
        val selectorString: String = getSelectorStringFromSelectorList(selectors)
        val selectorIndex: Int = getSelectorIndex(selectors, psiElement)

        //Add actual selector literal completions
        addSelectorLiteralLookupElements(psiElement.project, psiElement.containingFile?.name, result, selectorString, selectorIndex)

        // Add accessor and instance variable elements if selector size is equal to one
        // Accessors getMethods only apply to single element selectors
        if (selectors.size == 1) {
            addAccessorLookupElements(result, psiElement.project, selectorString)
        }
    }

    /**
     * Gets selectors from incomplete selector literal
     * Used while building completion results
     * @param psiElement currently editing psi element
     * @return array of selectors in this editing psi element
     */
    private fun getSelectorsFromIncompleteSelectorLiteral(psiElement: PsiElement, selectorLiteralParent: ObjJSelectorLiteral?): List<ObjJSelector> {
        val project = psiElement.project

        //Check to ensure this element is part of a method
        if (selectorLiteralParent == null) {
            //LOGGER.log(Level.INFO, "PsiElement is not a selector in a selector element");
            return listOf()
        }

        // If element is selector or direct parent is a selector,
        // then element is well formed, and can return the basic selector list
        // which will hold selectors up to self, which is what is needed for completion
        val selectors = selectorLiteralParent.selectorList as MutableList
        if (psiElement is ObjJSelector || psiElement.parent is ObjJSelector) {
            return selectors
        }

        // If psi parent is a qualified method call,
        // find it's index in selector array for autocompletion
        val selectorElement = psiElement.thisOrParentAs(ObjJSelector::class.java)
        var selectorIndex = selectorElement?.selectorIndex ?: -1

        // Find orphaned elements in method call
        // and create selector elements for later use.
        val orphanedSelectors = psiElement.parent.getChildrenOfType(ObjJTypes.ObjJ_ID)
        for (subSelector in orphanedSelectors) {
            val selector = ObjJElementFactory.createSelector(project, subSelector.text) ?: return selectors
            selectors.add(++selectorIndex, selector)
        }
        //Finally add the current psi element as a selector
        val selector = ObjJElementFactory.createSelector(project, psiElement.text) ?: return selectors
        selectors.add(selector)
        return selectors
    }

    private fun addSelectorLiteralLookupElements(project: Project, fileName: String?, result: CompletionResultSet, selectorString: String, selectorIndex: Int) {
        val methodHeaders: List<ObjJMethodHeaderDeclaration<*>> = ObjJUnifiedMethodIndex.instance
                .getByPatternFlat(selectorString.replace(CARET_INDICATOR, "(.*)"), project)
                .filter {
                    val isIgnored = it.stub?.ignored ?: ObjJCommentEvaluatorUtil.isIgnored(it, ObjJSuppressInspectionFlags.IGNORE_METHOD) ||
                            ObjJCommentEvaluatorUtil.isIgnored(it.parent, ObjJSuppressInspectionFlags.IGNORE_METHOD)
                    if (isIgnored) {
                        false
                    } else {
                        !ObjJPluginSettings.ignoreUnderscoredClasses || !it.containingClassName.startsWith("_") || it.containingFile?.name == fileName
                    }
                }
        if (methodHeaders.isEmpty()) {
            return
        }
        var out = mutableListOf<SelectorCompletionPriorityTupple>()
        val filteredOut = mutableListOf<SelectorCompletionPriorityTupple>()
        //LOGGER.log(Level.INFO, "Found <"+methodHeaders.size+"> method headers in list")
        for (methodHeader: ObjJMethodHeaderDeclaration<*> in methodHeaders) {
            ProgressIndicatorProvider.checkCanceled()

            //Get the selector at index, or continue loop
            val selector: ObjJSelector = getSelectorAtIndex(methodHeader, selectorIndex) ?: continue
            //Determine the priority
            val priority: Double = GENERIC_METHOD_SUGGESTION_PRIORITY
            out.add(SelectorCompletionPriorityTupple(selector, priority))
        }
        if (out.isEmpty()) {
            out = filteredOut
        }
        out.sortByDescending { it.priority }
        out.forEach {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    selector = it.selector,
                    selectorIndex = selectorIndex,
                    priority = it.priority,
                    addSpaceAfterColon = false
                    )
        }
    }

    private fun getSelectorAtIndex(methodHeader: ObjJMethodHeaderDeclaration<*>, selectorIndex: Int): ObjJSelector? {
        return if (methodHeader.selectorList.isNotEmpty()) {
            return methodHeader.selectorList[selectorIndex]
        } else {
            null
        }
    }

    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param selectorStringIn selector string to match
     */
    private fun addAccessorLookupElements(result: CompletionResultSet, project: Project, selectorStringIn: String?) {
        // Determines if string contains caret indicator
        // If it does, it changes the index fetch method
        val hasCaretIndicator: Boolean = selectorStringIn != null && selectorStringIn.indexOf(CARET_INDICATOR) > -1
        //Gets the selector string with wildcard as necessary
        val selectorString = getWildCardSelectorStringOrNull(selectorStringIn) ?: return
        //Add Declaration Selectors
        val declarations: List<ObjJInstanceVariableDeclaration> = getInstanceVariableDeclarationsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariableDeclaration in declarations) {
            ProgressIndicatorProvider.checkCanceled()
            addInstanceVariableDeclarationCompletion(result, instanceVariableDeclaration)
        }
        //Add Accessors
        val accessors: List<ObjJInstanceVariableDeclaration> = getAccessorsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariable in accessors) {
            ProgressIndicatorProvider.checkCanceled()
            addInstanceVariableAccessorMethods(result, instanceVariable)
        }
    }

    /**
     * Inserts a wildcard regex placeholder in place of the caret indicator
     * @return null if string is null, which is a possibility
     */
    private fun getWildCardSelectorStringOrNull(selectorStringIn: String?): String? {
        if (selectorStringIn == null || selectorStringIn.isEmpty()) {
            return null
        }
        val caretIndicatorIndex = selectorStringIn.indexOf(CARET_INDICATOR)
        return if (caretIndicatorIndex >= 0) selectorStringIn.replace(CARET_INDICATOR, "(.*)") else selectorStringIn
    }

    /**
     * Gets the instance variables list determined by whether or not there was a placeholder caret
     */
    private fun getInstanceVariableDeclarationsForSelector(selectorString: String, hasCaretIndicator: Boolean, project: Project): List<ObjJInstanceVariableDeclaration> {
        return if (hasCaretIndicator) {
            ObjJInstanceVariablesByNameIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            ObjJInstanceVariablesByNameIndex.instance[selectorString, project]
        }
    }

    /**
     * Gets all accessors matching this property
     */
    private fun getAccessorsForSelector(selectorString: String, hasCaretIndicator: Boolean, project: Project): List<ObjJInstanceVariableDeclaration> {
        return if (hasCaretIndicator) {
            ObjJClassInstanceVariableAccessorMethodIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            ObjJClassInstanceVariableAccessorMethodIndex.instance[selectorString, project]
        }
    }

    /**
     * Adds simple suggestions for instance variables, without accessor getMethods.
     * These can be called simply by their name without alteration
     * Variables can still have accessors and be accessed by name
     */
    private fun addInstanceVariableDeclarationCompletion(result: CompletionResultSet, instanceVariableDeclaration: ObjJInstanceVariableDeclaration) {
        if (instanceVariableDeclaration.variableName == null) {
            return
        }
        val containingClass = instanceVariableDeclaration.containingClassName
        //ProgressIndicatorProvider.checkCanceled();
        val priority = ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY

        val variableName = instanceVariableDeclaration.variableName?.name ?: return
        val variableType = instanceVariableDeclaration.formalVariableType.text
        ObjJSelectorLookupUtil.addSelectorLookupElement(
                resultSet = result,
                suggestedText = variableName,
                className = containingClass,
                tailText = "<$variableType>",
                priority = priority,
                addSuffix = false,
                icon = ObjJIcons.VARIABLE_ICON,
                addSpaceAfterColon = false)
    }


    /**
     * Adds an instance variable accessor method
     * Instance variable can have both getters and setters, and this covers both
     */
    private fun addInstanceVariableAccessorMethods(result: CompletionResultSet, instanceVariable: ObjJInstanceVariableDeclaration) {
        //Get className
        val className = instanceVariable.containingClassName

        //Find completion contribution list priority
        val priority: Double = GENERIC_METHOD_SUGGESTION_PRIORITY

        //Add Getter
        val getter = instanceVariable.getter
        if (getter != null) {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    suggestedText = getter.selectorString,
                    className = className,
                    tailText = "<" + instanceVariable.formalVariableType.text + ">",
                    priority = priority,
                    addSuffix = false,
                    icon = ObjJIcons.ACCESSOR_ICON,
                    addSpaceAfterColon = false
            )
        }
        //Add Setter
        val setter = instanceVariable.setter
        if (setter != null) {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    suggestedText = setter.selectorString,
                    className = className,
                    tailText = "<" + instanceVariable.formalVariableType.text + ">",
                    priority = priority,
                    addSuffix = false,
                    icon = ObjJIcons.ACCESSOR_ICON,
                    addSpaceAfterColon = false)
        }
    }

    /**
     * Gets the index of the selector that we are looking for suggestions on
     */
    private fun getSelectorIndex(selectors: List<ObjJSelector>, psiElement: PsiElement): Int {
        for (i in selectors.indices) {
            if (selectors[i] equals psiElement) {
                return i
            }
        }
        return selectors.size - 1
    }

    internal data class SelectorCompletionPriorityTupple (val selector:ObjJSelector, val priority:Double)

}
