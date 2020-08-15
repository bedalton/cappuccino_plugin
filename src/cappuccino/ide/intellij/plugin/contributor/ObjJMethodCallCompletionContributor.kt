package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.CARET_INDICATOR
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.GENERIC_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_SUPERCLASS_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferExpressionType
import cappuccino.ide.intellij.plugin.inference.withoutAnyType
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.references.getClassConstraints
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.stubs.stucts.getMethodStructs
import cappuccino.ide.intellij.plugin.stubs.stucts.toSelectorStruct
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.subList
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icons.ObjJIcons
import java.util.logging.Logger

object ObjJMethodCallCompletionContributor {

    private val LOGGER by lazy {
        Logger.getLogger("#"+ObjJMethodCallCompletionContributor::class.java.name)
    }

    /**
     * Method to manage adding selector lookup elements to result set
     * @param result completion result set
     * @param psiElement currently editing psi element
     */
    internal fun addSelectorLookupElementsFromSelectorList(
            result: CompletionResultSet,
            psiElement: PsiElement?) {
        if (psiElement == null) {
            //LOGGER.severe("Cannot add selector lookup elements. Selector element is null")
            return
        }
        val selectorLiteral = psiElement.getSelfOrParentOfType(ObjJSelectorLiteral::class.java)
        if (selectorLiteral != null) {
            addSelectorLiteralCompletions(result, psiElement, selectorLiteral)
            return
        }
        val methodCall = psiElement.getParentOfType(ObjJMethodCall::class.java)
                ?: ////LOGGER.info("Cannot get completion parameters. Method call is null.");
                return
        addMethodCallCompletions(result, psiElement, methodCall)
    }

    private fun addMethodCallCompletions(result: CompletionResultSet, psiElement: PsiElement, elementsParentMethodCall: ObjJMethodCall?, useAllSelectors: Boolean = true) {
        // Check for null parent method call
        if (elementsParentMethodCall == null) {
            //LOGGER.severe("Cannot add method call completions. Method call parent element is null")
            result.stopHere()
            return
        }
        // Check if service is dumb, and close out if it is
        if (DumbService.isDumb(psiElement.project)) {
            result.stopHere()
            return
        }

        // Create tag for inference type resolution
        val tag = createTag()

        //Determine target scope
        val scope: MethodScope = getTargetScope(elementsParentMethodCall.callTargetText, elementsParentMethodCall.project)


        // Find all possible completion elements, even those from a broken method call element
        var selectors: List<ObjJSelector>? = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall)
        val strictType = mutableListOf<String>()
        //Determine possible containing class names
        val possibleContainingClassNames: List<String> = when {
            scope == MethodScope.STATIC -> {
                strictType.add(elementsParentMethodCall.callTargetText)
                ObjJInheritanceUtil.getAllInheritedClasses(elementsParentMethodCall.callTargetText, psiElement.project).toList()
            }
            selectors.isNotNullOrEmpty() -> getClassConstraints(selectors!![0], tag, strictType)
            psiElement is ObjJCompositeElement -> getClassConstraints(psiElement, tag, strictType)
            else -> emptyList()
        }
        val usePrivate = elementsParentMethodCall.containingClassName in possibleContainingClassNames

        // Selectors is empty
        if (selectors.isNullOrEmpty()) {
            addMethodDeclarationLookupElementsForClasses(psiElement.project, result, possibleContainingClassNames, scope, usePrivate)
            return
        }
        val selectorIndex: Int = getSelectorIndex(selectors, psiElement)

        // Get all selectors possibly checked for
        addRespondsToSelectors(result, elementsParentMethodCall, selectorIndex)

        // Trim Selectors if needed
        if (!useAllSelectors && selectorIndex >= 0) {
            selectors = selectors.subList(0, selectorIndex + 1)
        }

        // Get selectors as string
        val selectorString = getSelectorString(selectors)


        // Attempt to add completions for known classes
        val project = psiElement.project
        if (selectorString.trim() == CARET_INDICATOR && possibleContainingClassNames.isNotEmpty()) {
            if (addMethodDeclarationLookupElementsForClasses(project, result, possibleContainingClassNames, scope, usePrivate))
                return
        }
        var didAddCompletions = addCompletionsForKnownClasses(
                resultSet = result,
                project = project,
                strictTypes = strictType,
                possibleContainingClassNames = possibleContainingClassNames,
                selectorIndex = selectorIndex,
                targetScope = scope,
                selectorString = selectors.subList(0, selectorIndex + 1).joinToString("") { it.getSelectorString(true) },
                usePrivate = usePrivate
        )

        // If completions added for known classes, return
        if (didAddCompletions) {
            return
        }

        //Attempt other was to add completions
        didAddCompletions = addMethodDeclarationLookupElements(
                project = psiElement.project,
                fileName = psiElement.containingFile?.name,
                result = result,
                targetScope = scope,
                selectorString = selectorString,
                selectorIndex = selectorIndex,
                containingClass = elementsParentMethodCall.containingClassName
        )


        // Add accessor and instance variable elements if selector size is equal to one
        // Accessors getMethods only apply to single element selectors
        if (scope.hasLocalScope && selectors.size == 1) {
            didAddCompletions = addAccessorLookupElements(result, psiElement.project, possibleContainingClassNames, selectorString) || didAddCompletions
        }
        if (!didAddCompletions && useAllSelectors) {
            addMethodCallCompletions(result, psiElement, elementsParentMethodCall, false)
        }
    }

    fun getInArrayMethodCallCompletions(result: CompletionResultSet, arrayLiteral: ObjJArrayLiteral): Boolean {
        if (arrayLiteral.exprList.size > 0) {
            return false
        }
        val callTargetLike = arrayLiteral.exprList.first()
                ?: return false
        if (callTargetLike.getNextNonEmptySibling(true)?.elementType == ObjJTypes.ObjJ_COMMA) {
            return false
        }
        val project = callTargetLike.project
        val callTargetText = callTargetLike.text
        //Determine target scope
        val scope: MethodScope = getTargetScope(callTargetLike.text, project)
        val tag = createTag()
        val strictType = mutableListOf<String>()
        //Determine possible containing class names
        val possibleContainingClassNames: List<String> = if (scope == MethodScope.STATIC) {
            strictType.add(callTargetText)
            ObjJInheritanceUtil.getAllInheritedClasses(callTargetText, project).toList()
        } else {
            inferExpressionType(callTargetLike, tag)?.withoutAnyType().orEmpty().toList()
        }

        val containingClass = callTargetLike
                .getParentOfType(ObjJClassDeclarationElement::class.java)
                ?.classNameString
                .orEmpty()
        val usePrivate = containingClass in possibleContainingClassNames

        // Attempt to add completions for known classes
        addMethodDeclarationLookupElementsForClasses(project, result, possibleContainingClassNames, scope, usePrivate)
        result.stopHere()
        return true
    }

    private fun getSelectorString(selectors: List<ObjJSelector>): String {

        // Get selector strings, replacing the one in need of completion
        val selectorStrings = selectors.map {
            it.getSelectorString(false)
        }
        // Get selector string
        return getSelectorStringFromSelectorStrings(selectorStrings)
    }

    private fun addRespondsToSelectors(result: CompletionResultSet, elementsParentMethodCall: ObjJMethodCall?, index: Int) {
        val resolved = elementsParentMethodCall?.callTarget?.singleVariableNameElementOrNull
                ?: return
        val respondsToSelectors = resolved.respondsToSelectors().mapNotNull { it.selectorList.getOrNull(index) }
        respondsToSelectors.forEach {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    selector = it,
                    selectorIndex = index,
                    priority = GENERIC_METHOD_SUGGESTION_PRIORITY,
                    addSpaceAfterColon = false)
        }

    }

    private fun addMethodDeclarationLookupElements(
            project: Project,
            fileName: String?,
            result: CompletionResultSet,
            targetScope: MethodScope,
            selectorString: String,
            selectorIndex: Int,
            containingClass: String?
    ): Boolean {

        // Check if underscores are allowed
        val allowUnderscoreOverride = selectorString.startsWith("_")

        // Find matching method headers
        val methodHeaders: List<ObjJMethodHeaderDeclaration<*>> = ObjJUnifiedMethodIndex.instance
                .getByPatternFlat(selectorString.toIndexPatternString(), project)
                .filter {
                    ProgressIndicatorProvider.checkCanceled()
                    val allowUnderscore = it.containingClassName == containingClass || allowUnderscoreOverride
                    if (!allowUnderscore && it.isPrivate) {
                        false
                    } else {
                        !ObjJPluginSettings.ignoreUnderscoredClasses || !it.isPrivate || it.containingFileName == fileName
                    }
                }

        // If there are no matching method headers, return
        if (methodHeaders.isEmpty()) {
            return false
        }

        // Loop through headers adding selectors
        val out = mutableListOf<SelectorCompletionPriorityTuple>()
        for (methodHeader: ObjJMethodHeaderDeclaration<*> in methodHeaders) {
            ProgressIndicatorProvider.checkCanceled()
            //Determine if method call matches scope, continue loop if it does not
            if (!inScope(targetScope, methodHeader)) {
                continue
            }
            //Get the selector at index, or continue loop
            val selectors = methodHeader.selectorStructs.subList(selectorIndex)
            //Determine the priority
            val priority: Double = GENERIC_METHOD_SUGGESTION_PRIORITY
            out.add(SelectorCompletionPriorityTuple(selectors, priority))
        }
        out.sortByDescending { it.priority }
        out.forEach {
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    project = project,
                    selectorStructs = it.selectors,
                    priority = it.priority,
                    icon = null)
        }
        return out.isNotEmpty()
    }


    private fun addCompletionsForKnownClasses(
            resultSet: CompletionResultSet,
            project: Project,
            possibleContainingClassNames: List<String>,
            targetScope: MethodScope,
            selectorIndex: Int,
            selectorString: String,
            strictTypes: List<String>? = null,
            usePrivate: Boolean
    ): Boolean {
        // Check if class names are empty
        if (possibleContainingClassNames.isEmpty())
            return false
        // initialize base variables
        var didAddCompletions = false
        var selectorStringBefore = selectorString.split(CARET_INDICATOR).firstOrNull() ?: return false
        val lastColon = selectorStringBefore.lastIndexOf(":")
        if (lastColon > 0) {
            selectorStringBefore = selectorStringBefore.substring(0, lastColon - 1)
        }
        // Loop through all possible target classes and add appropriate completions
        possibleContainingClassNames
                .flatMap {
                    ObjJClassDeclarationsIndex.instance[it, project]
                }
                .flatMap {
                    val constructs = it.getMethodStructs(true, createTag())
                    constructs
                }.filter {
                    selectorStringBefore.isEmpty() || it.selectorStringWithColon.startsWith(selectorStringBefore)
                }
                .forEach {
                    if (it.isPrivate && !usePrivate)
                        return@forEach
                    if (it.methodScope != targetScope)
                        return@forEach
                    val selectorStruct = it.selectors.subList(selectorIndex).ifEmpty { null } ?: return@forEach
                    ObjJSelectorLookupUtil.addSelectorLookupElement(
                            resultSet = resultSet,
                            project = project,
                            selectorStructs = selectorStruct,
                            icon = ObjJIcons.METHOD_ICON,
                            priority = if (strictTypes == null || it.containingClassName in strictTypes) {
                                TARGETTED_METHOD_SUGGESTION_PRIORITY
                            } else {
                                TARGETTED_SUPERCLASS_METHOD_SUGGESTION_PRIORITY
                            }
                    )
                    didAddCompletions = true
                }

        val indexOfColon = selectorString.indexOf(":")
        if (targetScope.hasLocalScope && indexOfColon < 0 || indexOfColon == selectorString.lastIndex) {
            didAddCompletions = addAccessorLookupElements(resultSet, project, possibleContainingClassNames, selectorString) || didAddCompletions
        }
        return didAddCompletions
    }

    private fun collapseContainingClasses(project: Project, containingClasses: Collection<String>): Set<String> {
        return containingClasses.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project, true)
        }.toSet()
    }


    private fun addMethodDeclarationLookupElementsForClasses(
            project: Project,
            result: CompletionResultSet,
            possibleContainingClassNames: List<String>,
            targetScope: MethodScope,
            usePrivate: Boolean
    ): Boolean {
        var didAdd = false
        collapseContainingClasses(project, possibleContainingClassNames).forEach {
            didAdd = addMethodDeclarationLookupElementsForClass(project, it, result, targetScope, usePrivate) || didAdd
        }
        return didAdd
    }

    private fun addMethodDeclarationLookupElementsForClass(project: Project, className: String, result: CompletionResultSet, targetScope: MethodScope, usePrivate: Boolean): Boolean {
        var didAdd = false
        ObjJClassMethodIndex.instance[className, project].forEach {
            if (targetScope != it.methodScope)
                return@forEach
            didAdd = true
            val selectors = it.selectorStructs.ifEmpty { null } ?: return@forEach
            if (it.isPrivate && !usePrivate)
                return@forEach
            if (it.methodScope != targetScope)
                return@forEach
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    project = project,
                    selectorStructs = selectors,
                    priority = TARGETTED_METHOD_SUGGESTION_PRIORITY
            )
        }
        return didAdd
    }

    private fun inScope(scope: MethodScope, methodHeader: ObjJMethodHeaderDeclaration<*>): Boolean {
        return when (scope) {
            MethodScope.STATIC -> methodHeader.isStatic
            MethodScope.INSTANCE -> !methodHeader.isStatic
            else -> true
        }
    }

    private fun getSelectorAtIndex(methodHeader: ObjJMethodHeaderDeclaration<*>, selectorIndex: Int): ObjJSelectorStruct? {
        return methodHeader.selectorStructs.getOrNull(selectorIndex)
                ?: methodHeader.selectorList.getOrNull(selectorIndex)?.toSelectorStruct(methodHeader.containingClassName)
    }

    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorStringIn selector string to match
     */
    private fun addAccessorLookupElements(result: CompletionResultSet, project: Project, possibleContainingClassNames: List<String>, selectorStringIn: String?): Boolean {
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
        //ProgressIndicatorProvider.checkCanceled();
        if (instanceVariableDeclaration.variableName == null) {
            return
        }
        val containingClass = instanceVariableDeclaration.containingClassName
        val priority = if (possibleContainingClassNames.contains(containingClass))
            ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
        else
            ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY
        val variableName = instanceVariableDeclaration.variableNameString
        val variableType = instanceVariableDeclaration.variableType
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

        instanceVariable.getMethodStructs().forEach {
            val selector = it.selectors.ifEmpty { null } ?: return@forEach
            ObjJSelectorLookupUtil.addSelectorLookupElement(
                    resultSet = result,
                    project = instanceVariable.project,
                    selectorStructs = selector,
                    priority = priority,
                    icon = ObjJIcons.ACCESSOR_ICON
            )
        }
    }

    private fun addSelectorLiteralCompletions(resultSet: CompletionResultSet, psiElement: PsiElement, selectorLiteral: ObjJSelectorLiteral, useAllSelectors: Boolean = true) {
        var selectors = selectorLiteral.selectorList
        val selectorIndex: Int = getSelectorIndex(selectors, psiElement)
        if (!useAllSelectors && selectorIndex >= 0) {
            selectors = selectors.subList(0, selectorIndex + 1)
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
                .getByPatternFlat(selectorString.toIndexPatternString(), project)
                .mapNotNull { try {it.selectorStructs.subList(selectorIndex).ifEmpty { null } } catch (e:Exception) { null } }
                .toSet()
                .forEach {
                    if (!didAddOne)
                        didAddOne = true
                    ObjJSelectorLookupUtil.addSelectorLookupElement(
                            resultSet = resultSet,
                            project = project,
                            selectorStructs = it,
                            priority = TARGETTED_METHOD_SUGGESTION_PRIORITY
                    )
                }
        if (!didAddOne && useAllSelectors) {
            addSelectorLiteralCompletions(resultSet, psiElement, selectorLiteral, false)
        }

    }


    /**
     * Gets the scope for the suggested getMethods we should have
     */
    private fun getTargetScope(callTargetText: String, project: Project): MethodScope {
        return when {
            ObjJImplementationDeclarationsIndex.instance[callTargetText, project].isNotEmpty() -> MethodScope.STATIC
            else -> MethodScope.INSTANCE
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

    @Suppress("SameParameterValue")
    private fun getPriority(possibleContainingClassNames: List<String>, className: String, priorityIfTarget: Double, priorityIfNotTarget: Double): Double {
        return if (possibleContainingClassNames.contains(className)) {
            priorityIfTarget
        } else {
            priorityIfNotTarget
        }
    }

    internal data class SelectorCompletionPriorityTuple(val selectors: List<ObjJSelectorStruct>, val priority: Double)

}


