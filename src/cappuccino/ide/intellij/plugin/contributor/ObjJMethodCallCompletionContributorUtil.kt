package cappuccino.ide.intellij.plugin.contributor

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil
import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.lang.ObjJIcons
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil

import java.util.logging.Level
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil.addSelectorLookupElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*

object ObjJMethodCallCompletionContributorUtil {

    private val LOGGER = Logger.getLogger(ObjJMethodCallCompletionContributorUtil::class.java.name)
    val CARET_INDICATOR = ObjJCompletionContributor.CARET_INDICATOR

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
        val methodCall = psiElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //LOGGER.log(Level.INFO, "Cannot get completion parameters. Method call is null.");
                return
        addMethodCallCompletions(result, psiElement, methodCall)

    }


    private fun addMethodCallCompletions(result: CompletionResultSet, psiElement: PsiElement, elementsParentMethodCall: ObjJMethodCall?) {

        //LOGGER.log(Level.INFO, "Add method call completions");
        if (elementsParentMethodCall == null) {
            LOGGER.log(Level.SEVERE, "Cannot add method call completions. Method call parent element is null")
            return
        }
        val selectors = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall)
        if (selectors.isEmpty()) {
            //selectors = Collections.singletonList(ObjJElementFactory.createSelector(psiElement.getProject(), psiElement.getText()));
        }
        val selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)
        var selectorIndex = selectors.size - 1
        val thisSelectorTrimmedText = psiElement.text.replace("\\s+".toRegex(), "")
        for (i in selectors.indices) {
            if (selectors[i] equals psiElement) {
                selectorIndex = i
                break
            }
        }
        val selector = if (selectorIndex >= 0 && selectorIndex < selectors.size) selectors[selectorIndex] else null
        addMethodDeclarationLookupElements(result, selectors, selectorIndex)
        if (selectors.size == 1 && !DumbService.isDumb(psiElement.project)) {
            addAccessorLookupElements(result, psiElement.project, if (selector != null) ObjJSelectorReferenceResolveUtil.getClassConstraints(selector) else emptyList(), selectorString)
        }
    }

    /**
     * Gets selectors from incomplete method call
     * Used while building completion results
     * @param psiElement currently editing psi element
     * @return array of selectors in this editing psi element
     */
    private fun getSelectorsFromIncompleteMethodCall(psiElement: PsiElement, selectorParentMethodCall: ObjJMethodCall?): List<ObjJSelector> {
        val project = psiElement.project

        //Check to ensure this element is part of a method
        if (selectorParentMethodCall == null) {
            //LOGGER.log(Level.INFO, "PsiElement is not a selector in a method call element");
            return listOf()
        }

        // If element is selector or direct parent is a selector,
        // then element is well formed, and can return the basic selector list
        // which will hold selectors up to self, which is what is needed for completion
        var selectorIndex: Int
        val selectors = selectorParentMethodCall.selectorList as MutableList
        if (psiElement is ObjJSelector || psiElement.parent is ObjJSelector) {
            return selectors
        }

        // If psi parent is a qualified method call,
        // find it's index in selector array for autocompletion
        if (psiElement.parent is ObjJQualifiedMethodCallSelector) {
            val qualifiedMethodCallSelector = psiElement.parent as ObjJQualifiedMethodCallSelector
            if (qualifiedMethodCallSelector.selector != null) {
                selectorIndex = selectors.indexOf(qualifiedMethodCallSelector.selector!!)
            } else {
                selectorIndex = selectors.size - 1
            }
        } else {
            selectorIndex = selectors.size - 1
        }

        // Find orphaned elements in method call
        // and create selector elements for later use.
        val orphanedSelectors = psiElement.parent.getChildrenOfType(ObjJTypes.ObjJ_ID)
        for (subSelector in orphanedSelectors) {
            val selectorText = subSelector.text.replace("[^a-zA-Z_]".toRegex(), "").trim()
            if (selectorText.isEmpty()) {
                continue
            }

            selectors.add(++selectorIndex, ObjJElementFactory.createSelector(project, selectorText)!!)
        }
        //Finally add the current psi element as a selector
        try {
            val selectorString = psiElement.text.replace("[^a-zA-Z_]".toRegex(), "").trim()
            if (selectorString.isNotEmpty()) {
                val selector = ObjJElementFactory.createSelector(project, selectorString)
                if (selector != null) {
                    selectors.add(selector)
                }
            }
        } catch (e:Exception) {}
        return selectors
    }

    /**
     * Adds lookup elements from matching method headers
     * @param result completion result set
     * @param selectors current method call selector list
     * @param selectorIndex index of currently editing selector
     */
    private fun addMethodDeclarationLookupElements(result: CompletionResultSet, selectors: List<ObjJSelector>, selectorIndex: Int) {
        if (selectors.isEmpty()) {
            return
        }
        val file = selectors[0].containingFile
        val selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)
        val resolveResult = ObjJSelectorReferenceResolveUtil.resolveSelectorReferenceAsPsiElement(selectors, selectorIndex)
        if (resolveResult == null) {
            //LOGGER.log(Level.INFO, "Resolve result is null");
            if (selectors.size <= selectorIndex) {
                //LOGGER.log(Level.INFO, "Cannot add method selector elements to result set. Selector index foldingDescriptors of bounds.");
                return
            }
            val selectorResolveResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(selectors[selectorIndex])
            if (selectorResolveResult.isEmpty) {
                //LOGGER.log(Level.INFO, "Selector literal resolve result is empty for selector <"+selectorString+">");
                return
            }
            for (selector in ObjJResolveableElementUtil.onlyResolveableElements(selectorResolveResult.naturalResult, file)) {
                ProgressIndicatorProvider.checkCanceled()
                //LOGGER.log(Level.INFO, "Adding natural result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY)
            }
            for (selector in ObjJResolveableElementUtil.onlyResolveableElements(selectorResolveResult.otherResult, file)) {
                ProgressIndicatorProvider.checkCanceled()
                //LOGGER.log(Level.INFO, "Adding other result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY)
            }
            return
        }
        //LOGGER.log(Level.INFO, "Searching for selector completions. Found <" + resolveResult.naturalResult.size + "> natural results and <" + resolveResult.otherResult.size + "> general results, for selector: <" + selectorString + ">; Possible call target types: [" + resolveResult.possibleContainingClassNames.joinToString(", ")+"]")
        val naturalResult = ObjJResolveableElementUtil.onlyResolveableElements(resolveResult.naturalResult, file)
        addSelectorLookupElementsFromSelectorList(result, naturalResult, selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY)
        if (resolveResult.possibleContainingClassNames.isEmpty() || resolveResult.possibleContainingClassNames.contains(ObjJClassType.UNDETERMINED)) {
            addSelectorLookupElementsFromSelectorList(result, ObjJResolveableElementUtil.onlyResolveableElements(resolveResult.otherResult, file), selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY)
        }
    }

    /**
     * Adds selector lookup elements from a list of possible selector completions
     * @param resultSet completion result set
     * @param elements selectors to add to completion set
     * @param selectorIndex index of currently editing selector
     * @param priority suggestion placement priority
     */
    private fun addSelectorLookupElementsFromSelectorList(resultSet: CompletionResultSet, elements: List<ObjJSelector>, selectorIndex: Int, priority: Double) {
        for (selector in elements) {
            ProgressIndicatorProvider.checkCanceled()
            //LOGGER.log(Level.SEVERE, "Adding Selector lookup element: <"+selector.getText()+">");
            val instanceVariableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
            if (instanceVariableDeclaration != null) {
                addInstanceVariableAccessorMethods(resultSet, instanceVariableDeclaration, priority)
            } else {
                ObjJSelectorLookupUtil.addSelectorLookupElement(resultSet, selector, selectorIndex, priority)
            }
        }
    }


    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorString selector string to match
     */
    private fun addAccessorLookupElements(result: CompletionResultSet, project: Project, possibleContainingClassNames: List<String>, selectorString: String?) {
        var selectorString = selectorString
        if (selectorString == null || selectorString.isEmpty()) {
            return
        }
        val caretIndicatorIndex = selectorString.indexOf(CARET_INDICATOR)
        selectorString = if (caretIndicatorIndex >= 0) selectorString.replace(CARET_INDICATOR, "(.*)") else selectorString
        val declarations: List<ObjJInstanceVariableDeclaration>
        if (caretIndicatorIndex >= 0) {
            declarations = ObjJInstanceVariablesByNameIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            declarations = ObjJInstanceVariablesByNameIndex.instance.get(selectorString, project)
        }

        for (instanceVariableDeclaration in declarations) {
            ProgressIndicatorProvider.checkCanceled()
            if (instanceVariableDeclaration.variableName == null) {
                continue
            }
            //ProgressIndicatorProvider.checkCanceled();
            val priority = if (possibleContainingClassNames.contains(instanceVariableDeclaration.containingClassName))
                ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
            else
                ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY

            val className = instanceVariableDeclaration.containingClassName
            val variableName = instanceVariableDeclaration.variableName!!.name
            val variableType = instanceVariableDeclaration.formalVariableType.text
            addSelectorLookupElement(result, variableName, className, "<$variableType>", priority, false, ObjJIcons.VARIABLE_ICON)
        }

        val accessors: List<ObjJInstanceVariableDeclaration>
        if (caretIndicatorIndex >= 0) {
            accessors = ObjJClassInstanceVariableAccessorMethodIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            accessors = ObjJClassInstanceVariableAccessorMethodIndex.instance.get(selectorString, project)
        }
        //LOGGER.log(Level.INFO, "Found <"+accessors.size()+"> accessor properties");
        for (instanceVariable in accessors) {

            val className = instanceVariable.containingClassName
            val priority = if (possibleContainingClassNames.contains(className))
                ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY
            else
                ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY
            addInstanceVariableAccessorMethods(result, instanceVariable, priority)
        }
    }


    private fun addInstanceVariableAccessorMethods(result: CompletionResultSet, instanceVariable: ObjJInstanceVariableDeclaration, priority: Double) {
        val className = instanceVariable.containingClassName
        val getter = instanceVariable.getGetter()
        val setter = instanceVariable.getSetter()
        if (getter != null) {
            addSelectorLookupElement(result, getter.selectorString, className, "<" + instanceVariable.formalVariableType.text + ">", priority, false, ObjJIcons.ACCESSOR_ICON)
        }
        if (setter != null) {
            addSelectorLookupElement(result, setter.selectorString, className, "<" + instanceVariable.formalVariableType.text + ">", priority, false, ObjJIcons.ACCESSOR_ICON)
        }
    }
}

/*
    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorString selector string to match
     * /
    private static void addInstanceVariableLookupElements(@NotNull CompletionResultSet result, @NotNull Project project, @NotNull List<String> possibleContainingClassNames, @Nullable String selectorString) {
        if (selectorString == null || selectorString.isEmpty()) {
            return;
        }
        final int caretIndicatorIndex = selectorString.indexOf(CARET_INDICATOR);
        List<ObjJInstanceVariableDeclaration> declarations;
        if (caretIndicatorIndex >= 0) {
            declarations = ObjJInstanceVariablesByNameIndex.getInstance().getByPatternFlat(selectorString.replace(CARET_INDICATOR, "(.*)"), project);
        } else {
            declarations = ObjJInstanceVariablesByNameIndex.getInstance().get(selectorString, project);
        }
        for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : declarations) {
            ProgressIndicatorProvider.checkCanceled();
            if (instanceVariableDeclaration.getVariableName() == null) {
                continue;
            }
            //ProgressIndicatorProvider.checkCanceled();
            double priority = possibleContainingClassNames.contains(instanceVariableDeclaration.getContainingClassName()) ?
                    ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY : ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY;
            addSelectorLookupElement(result, instanceVariableDeclaration.getVariableName(),"<"+instanceVariableDeclaration.getFormalVariableType()+">", priority,false, null);
        }
    }
*/