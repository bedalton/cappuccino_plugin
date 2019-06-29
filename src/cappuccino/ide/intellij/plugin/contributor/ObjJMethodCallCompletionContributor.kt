package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.CARET_INDICATOR
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.GENERIC_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.references.getClassConstraints
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icons.ObjJIcons
import java.util.logging.Level
import java.util.logging.Logger

object ObjJMethodCallCompletionContributor {

    private val LOGGER = Logger.getLogger(ObjJMethodCallCompletionContributor::class.java.name)

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
        if (selectorLiteral != null) {
            addSelectorLiteralCompletions(result, psiElement, selectorLiteral);
            return
        }
        val methodCall = psiElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //LOGGER.log(Level.INFO, "Cannot get completion parameters. Method call is null.");
                return
        addMethodCallCompletions(result, psiElement, methodCall)
    }

    private fun addMethodCallCompletions(result: CompletionResultSet, psiElement: PsiElement, elementsParentMethodCall: ObjJMethodCall?, useAllSelectors:Boolean = true) {

        //LOGGER.log(Level.INFO, "Add method call completions");
        if (elementsParentMethodCall == null) {
            LOGGER.log(Level.SEVERE, "Cannot add method call completions. Method call parent element is null")
            result.stopHere()
            return
        }
        if (DumbService.isDumb(psiElement.project)) {
            result.stopHere()
            return
        }


        var selectors: List<ObjJSelector> = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall)
        val selectorIndex: Int = getSelectorIndex(selectors, psiElement)
        if (!useAllSelectors && selectorIndex >= 0) {
            selectors =  selectors.subList(0, selectorIndex+1)
        }

        val selectorStrings = selectors.map {
            val selector = it.getSelectorString(false)
            if (selector.contains(CARET_INDICATOR))
                selector.toIndexPatternString()
            else
                selector
        }
        val selectorString: String = getSelectorStringFromSelectorStrings(selectorStrings)
        val selector: ObjJSelector? = if (selectorIndex >= 0 && selectorIndex < selectors.size) selectors[selectorIndex] else null

        addRespondsToSelectors(result, elementsParentMethodCall, selectorIndex)
        //Determine target scope
        val scope: TargetScope = getTargetScope(elementsParentMethodCall)
        //LOGGER.log(Level.INFO, String.format("Call target: <%s> has scope of <%s> with selector: <%s>", elementsParentMethodCall.callTargetText, scope.toString(), selectorString))
        //Determine possible containing class names
        val possibleContainingClassNames: List<String> = when {
            scope == TargetScope.STATIC -> ObjJInheritanceUtil.getAllInheritedClasses(elementsParentMethodCall.callTargetText, psiElement.project).toList()
            selector != null -> getClassConstraints(selector, createTag())
            else -> emptyList()
        }
        //Add actual method call completions
        var didAdd = addMethodDeclarationLookupElements(psiElement.project, psiElement.containingFile?.name, result, possibleContainingClassNames, scope, selectorString, selectorIndex, elementsParentMethodCall.containingClassName)

        val hasLocalScope: Boolean = (scope == TargetScope.INSTANCE || scope == TargetScope.ANY)
        // Add accessor and instance variable elements if selector size is equal to one
        // Accessors getMethods only apply to single element selectors
        if (hasLocalScope && selectors.size == 1) {
            didAdd = addAccessorLookupElements(result, psiElement.project, possibleContainingClassNames, selectorString) || didAdd
        }
        if (!didAdd && useAllSelectors) {
            addMethodCallCompletions(result, psiElement, elementsParentMethodCall, false)
        }
    }

    private fun addRespondsToSelectors(result: CompletionResultSet, elementsParentMethodCall: ObjJMethodCall?, index:Int) {
        val resolved = elementsParentMethodCall?.callTarget?.singleVariableNameElementOrNull
                ?: return
        val respondsToSelectors = resolved.respondsToSelectors().mapNotNull { it.selectorList.getOrNull(index)}
        respondsToSelectors.forEach {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    selector = it,
                    selectorIndex = index,
                    priority = GENERIC_METHOD_SUGGESTION_PRIORITY,
                    addSpaceAfterColon = false)
        }

    }

    private fun addMethodDeclarationLookupElements(project: Project, fileName: String?, result: CompletionResultSet, possibleContainingClassNames: List<String>, targetScope: TargetScope, selectorString: String, selectorIndex: Int, containingClass:String?) : Boolean {

        if (selectorString.trim() == CARET_INDICATOR && possibleContainingClassNames.isNotEmpty()) {
            LOGGER.info("Adding all possible class methods. Classes: $possibleContainingClassNames")
            return addMethodDeclarationLookupElementsForClasses(project, result, possibleContainingClassNames, targetScope)
        }
        val methodHeaders: List<ObjJMethodHeaderDeclaration<*>> = ObjJUnifiedMethodIndex.instance
                .getByPatternFlat(selectorString.toIndexPatternString(), project)
                .filter {
                    ProgressIndicatorProvider.checkCanceled()
                    val allowUnderscore = it.containingClassName == containingClass
                    //val isIgnored = it.stub?.ignored ?: ObjJIgnoreEvaluatorUtil.isIgnored(it, ObjJSuppressInspectionFlags.IGNORE_METHOD) ||
                      //      ObjJIgnoreEvaluatorUtil.isIgnored(it.parent, ObjJSuppressInspectionFlags.IGNORE_METHOD)
                    if (!allowUnderscore && it.selectorString.startsWith("_")) {
                        false
                    } else {
                        !ObjJPluginSettings.ignoreUnderscoredClasses || !it.containingClassName.startsWith("_") || it.containingFile?.name == fileName
                    }
                }
        if (methodHeaders.isEmpty()) {
            return false
        }
        var out = mutableListOf<SelectorCompletionPriorityTupple>()
        val filteredOut = mutableListOf<SelectorCompletionPriorityTupple>()
        //LOGGER.log(Level.INFO, "Found <"+methodHeaders.size+"> method headers in list")
        for (methodHeader: ObjJMethodHeaderDeclaration<*> in methodHeaders) {
            ProgressIndicatorProvider.checkCanceled()
            //LOGGER.log(Level.INFO, String.format("Scope for target is <%s>; Method scope is <%s>;",targetScope.toString(),if(methodHeader.static)"static" else "instance"))
            //Determine if method call matches scope, continue loop if it does not
            if (!inScope(targetScope, methodHeader)) {
                continue
            }
            //Get the selector at index, or continue loop
            val selector: ObjJSelector = getSelectorAtIndex(methodHeader, selectorIndex) ?: continue
            //Determine the priority
            val priority: Double = getPriority(possibleContainingClassNames, selector.containingClassName, TARGETTED_METHOD_SUGGESTION_PRIORITY, GENERIC_METHOD_SUGGESTION_PRIORITY)


            //if (ObjJClassType.ID !in possibleContainingClassNames && ObjJClassType.UNDETERMINED !in possibleContainingClassNames && filterIfStrict) {
                if ((possibleContainingClassNames.isNotEmpty() && methodHeader.containingClassName !in possibleContainingClassNames)) {
                    filteredOut.add(SelectorCompletionPriorityTupple(selector, GENERIC_METHOD_SUGGESTION_PRIORITY))
                    continue
                }
            //}//Add the lookup element
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
                    addSpaceAfterColon = true)
        }
        return out.isNotEmpty()
    }

    private fun collapseContainingClasses(project:Project, containingClasses:Collection<String>) : Set<String> {
        return containingClasses.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project, true)
        }.toSet()
    }

    private fun addMethodDeclarationLookupElementsForClasses(project: Project, result: CompletionResultSet, possibleContainingClassNames: List<String>, targetScope: TargetScope) : Boolean {
        var didAdd = false
        collapseContainingClasses(project, possibleContainingClassNames).forEach {
            didAdd = addMethodDeclarationLookupElementsForClass(project, it, result, targetScope) || didAdd
        }
        return didAdd
    }
    private fun addMethodDeclarationLookupElementsForClass(project: Project, className: String, result: CompletionResultSet, targetScope: TargetScope) : Boolean {
        var didAdd = false
        ObjJClassMethodIndex.instance[className, project].forEach {
            if (!targetScope.equals(it.methodScope))
                return@forEach
            didAdd = true
            val selector = it.selectorList.getOrNull(0) ?: return@forEach
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    selector = selector,
                    selectorIndex = 0,
                    priority = TARGETTED_METHOD_SUGGESTION_PRIORITY,
                    addSpaceAfterColon = true)
        }
        return didAdd
    }

    private fun inScope(scope: TargetScope, methodHeader: ObjJMethodHeaderDeclaration<*>): Boolean {
        return when (scope) {
            TargetScope.STATIC -> methodHeader.isStatic
            TargetScope.INSTANCE -> !methodHeader.isStatic
            else -> true
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
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorStringIn selector string to match
     */
    private fun addAccessorLookupElements(result: CompletionResultSet, project: Project, possibleContainingClassNames: List<String>, selectorStringIn: String?) : Boolean {
        // Determines if string contains caret indicator
        // If it does, it changes the index fetch method
        val hasCaretIndicator: Boolean = selectorStringIn != null && selectorStringIn.indexOf(CARET_INDICATOR) > -1
        //Gets the selector string with wildcard as necessary
        val selectorString = selectorStringIn?.toIndexPatternString() ?: return false
        //Add Declaration Selectors
        var didAdd = false
        val declarations: List<ObjJInstanceVariableDeclaration> = getInstanceVariableDeclarationsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariableDeclaration in declarations) {
            ProgressIndicatorProvider.checkCanceled()
            didAdd = true
            addInstanceVariableDeclarationCompletion(result, possibleContainingClassNames, instanceVariableDeclaration)
        }
        //Add Accessors
        val accessors: List<ObjJInstanceVariableDeclaration> = getAccessorsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariable in accessors) {
            ProgressIndicatorProvider.checkCanceled()
            didAdd = true
            addInstanceVariableAccessorMethods(result, possibleContainingClassNames, instanceVariable)
        }
        return didAdd
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
    private fun addInstanceVariableDeclarationCompletion(result: CompletionResultSet, possibleContainingClassNames: List<String>, instanceVariableDeclaration: ObjJInstanceVariableDeclaration) {
        if (instanceVariableDeclaration.variableName == null) {
            return
        }
        val containingClass = instanceVariableDeclaration.containingClassName
        if (ObjJClassType.ID !in possibleContainingClassNames && ObjJClassType.UNDETERMINED !in possibleContainingClassNames && containingClass !in possibleContainingClassNames) {
            return
        }
        //ProgressIndicatorProvider.checkCanceled();
        val priority = if (possibleContainingClassNames.contains(containingClass))
            ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
        else
            ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY

        val variableName = instanceVariableDeclaration.variableName?.name ?: return
        val variableType = instanceVariableDeclaration.formalVariableType.text
        ObjJSelectorLookupUtil.addSelectorLookupElement(
                resultSet = result,
                suggestedText = variableName,
                className = containingClass,
                tailText = "<$variableType>",
                priority = priority,
                addSuffix = false,
                addSpaceAfterColon = true,
                icon = ObjJIcons.VARIABLE_ICON)
    }


    /**
     * Adds an instance variable accessor method
     * Instance variable can have both getters and setters, and this covers both
     */
    private fun addInstanceVariableAccessorMethods(result: CompletionResultSet, possibleContainingClassNames: List<String>, instanceVariable: ObjJInstanceVariableDeclaration) {
        //Get className
        val className = instanceVariable.containingClassName

        if (ObjJClassType.ID !in possibleContainingClassNames && ObjJClassType.UNDETERMINED !in possibleContainingClassNames && className !in possibleContainingClassNames && ObjJPluginSettings.filterMethodCallsStrictIfTypeKnown) {
            return
        }

        //Find completion contribution list priority
        val priority: Double = getPriority(possibleContainingClassNames, className, TARGETTED_METHOD_SUGGESTION_PRIORITY, GENERIC_METHOD_SUGGESTION_PRIORITY)

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
                    addSpaceAfterColon = true,
                    icon = ObjJIcons.ACCESSOR_ICON
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
                    addSpaceAfterColon = true,
                    icon = ObjJIcons.ACCESSOR_ICON)
        }
    }

    private fun addSelectorLiteralCompletions(resultSet:CompletionResultSet, psiElement: PsiElement, selectorLiteral:ObjJSelectorLiteral, useAllSelectors: Boolean = true) {
        var selectors = selectorLiteral.selectorList
        val selectorIndex: Int = getSelectorIndex(selectors, psiElement)
        if (!useAllSelectors && selectorIndex >= 0) {
            selectors =  selectors.subList(0, selectorIndex+1)
        }

        val selectorStrings = selectors.map {
            val selector = it.getSelectorString(false)
            if (selector.contains(CARET_INDICATOR))
                selector.toIndexPatternString()
            else
                selector
        }
        val project = psiElement.project
        val selectorString: String = getSelectorStringFromSelectorStrings(selectorStrings)
        var didAddOne = false
        ObjJUnifiedMethodIndex.instance
                .getByPatternFlat(selectorString.toIndexPatternString(), project).mapNotNull { it.selectorList.getOrNull(selectorIndex) }.toSet().forEach {
                    if (!didAddOne)
                        didAddOne = true
                    ObjJSelectorLookupUtil.addSelectorLookupElement(
                            resultSet = resultSet,
                            selector = it,
                            selectorIndex = selectorIndex,
                            priority = TARGETTED_METHOD_SUGGESTION_PRIORITY,
                            addSpaceAfterColon = false)
                }
        if (!didAddOne && useAllSelectors) {
            addSelectorLiteralCompletions(resultSet, psiElement, selectorLiteral, false)
        }

    }


    /**
     * Gets the scope for the suggested getMethods we should have
     */
    private fun getTargetScope(methodCall: ObjJMethodCall): TargetScope {
        return when {
            ObjJImplementationDeclarationsIndex.instance[methodCall.callTargetText, methodCall.project].isNotEmpty() -> TargetScope.STATIC
            else -> TargetScope.ANY
        }
    }

    /**
     * Gets the index of the selector that we are looking for suggestions on
     */
    private fun getSelectorIndex(selectors: List<ObjJSelector>, psiElement: PsiElement): Int {
        val elementAsSelector = psiElement.getSelfOrParentOfType(ObjJSelector::class.java)
        for (i in selectors.indices) {
            if (selectors[i] equals elementAsSelector) {
                return i
            }
        }
        return selectors.size - 1
    }

    private fun getPriority(possibleContainingClassNames: List<String>, className: String, priorityIfTarget: Double, priorityIfNotTarget: Double): Double {
        return if (possibleContainingClassNames.contains(className)) {
            priorityIfTarget
        } else {
            priorityIfNotTarget
        }
    }

    internal enum class TargetScope {
        STATIC,
        INSTANCE,
        ANY;

        fun equals(scope: MethodScope) : Boolean {
            return when (this) {
                ANY -> true
                STATIC -> scope == MethodScope.STATIC
                INSTANCE -> scope == MethodScope.INSTANCE
            }
        }
    }

    internal data class SelectorCompletionPriorityTupple (val selector:ObjJSelector, val priority:Double)

}

