package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList

object ObjJSelectorReferenceResolveUtil {

    private val LOGGER = Logger.getLogger(ObjJSelectorReferenceResolveUtil::class.java.name)
    private val EMPTY_RESULT = SelectorResolveResult(emptyList(), emptyList<PsiElement>(), emptyList())
    private val EMPTY_SELECTORS_RESULT = SelectorResolveResult(emptyList(), emptyList<ObjJSelector>(), emptyList())

    fun getMethodCallReferences(element: ObjJSelector): SelectorResolveResult<ObjJSelector> {
        val classConstraints = getClassConstraints(element)
        val selector = element.getSelectorString(false)
        val parent = element.getParentOfType( ObjJHasMethodSelector::class.java)
        val selectorIndex = parent?.selectorStrings?.indexOf(selector) ?: -1
        val fullSelector = parent?.selectorString ?: return EMPTY_SELECTORS_RESULT
        if (parent is ObjJMethodHeader || parent is ObjJAccessorProperty) {
            return SelectorResolveResult(listOf(element), emptyList(), classConstraints)
        }

        if (DumbService.isDumb(element.project)) {
            return EMPTY_SELECTORS_RESULT
        }
        //LOGGER.log(Level.INFO, "Searching for methods matching selector: <"+fullSelector+">, in file: "+element.getContainingFile().getVirtualFile().getName());
        val methodHeaders = ObjJUnifiedMethodIndex.instance[fullSelector, element.project]
        return prune(classConstraints, methodHeaders, element.text, selectorIndex)
    }

    @Suppress("unused")
    internal fun getMethodCallPartialReferences(
            element: ObjJSelector?, includeSelf: Boolean): SelectorResolveResult<ObjJSelector> {
        if (element == null) {
            return EMPTY_SELECTORS_RESULT
        }
        val classConstraints = getClassConstraints(element)
        val parent = element.getParentOfType( ObjJHasMethodSelector::class.java)
        val selectorIndex = parent?.selectorList?.indexOf(element) ?: -1
        val selectorFragment = ObjJPsiImplUtil.getSelectorUntil(element, includeSelf) ?: return EMPTY_SELECTORS_RESULT
        if (parent is ObjJMethodHeader || parent is ObjJAccessorProperty) {
            return SelectorResolveResult(listOf(element), emptyList(), classConstraints)
        }

        if (DumbService.isDumb(element.project)) {
            return EMPTY_SELECTORS_RESULT
        }
        val methodHeaders = ObjJUnifiedMethodIndex.instance.getByPatternFlat("$selectorFragment(.*)", element.project)
        return if (!methodHeaders.isEmpty()) {
            prune(classConstraints, methodHeaders, element.text, selectorIndex)
        } else {
            getSelectorLiteralReferences(element)
        }
    }

    fun resolveSelectorReferenceAsPsiElement(selectors: List<ObjJSelector>, selectorIndex: Int): SelectorResolveResult<ObjJSelector>? {
        val result = resolveSelectorReferenceRaw(selectors, selectorIndex) ?: return null
        return if (!result.methodHeaders.isEmpty()) {
            prune(result.classConstraints, result.methodHeaders, result.baseSelector.text, result.index)
        } else {
            null
        }
    }

    private fun resolveSelectorReferenceRaw(selectors: List<ObjJSelector>, selectorIndexIn: Int): RawResult? {
        var selectorIndex = selectorIndexIn
        //Have to loop over elements as some selectors in list may have been virtually created
        var parent: ObjJMethodCall? = null
        if (selectors.isEmpty()) {
            return null
        }
        if (selectorIndex < 0 || selectorIndex >= selectors.size) {
            selectorIndex = selectors.size - 1
        }
        var baseSelector: ObjJSelector? = selectors[selectorIndex]
        var project: Project? = baseSelector?.project
        for (selector in selectors) {
            if (parent == null) {
                parent = selector.getParentOfType( ObjJMethodCall::class.java)
            }
            if (parent != null && parent.containingClassName == ObjJElementFactory.PlaceholderClassName) {
                parent = null
            }
        }
        for (i in selectors.size - 1 downTo 1) {
            val tempSelector = selectors[i]
            if (project == null) {
                project = parent?.project ?: tempSelector.project
            }
            if (baseSelector == null || tempSelector.text.contains(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR)) {
                baseSelector = tempSelector
                selectorIndex = i
                break
            }
        }
        if (project == null) {
            //LOGGER.log(Level.INFO, "Parent and base selector are null");
            return null
        }

        val selector = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors).replace("\\s+".toRegex(), "")
        //LOGGER.log(Level.INFO, "Selector for method call is: <"+ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)+">");
        if (selector.isEmpty()) {
            //LOGGER.log(Level.INFO, "Selector search failed with empty selector.");
            return null
        }
        val classConstraints = if (parent != null) getClassConstraints(parent) else emptyList()
        val methodHeaders: Map<String, List<ObjJMethodHeaderDeclaration<*>>>
        methodHeaders = if (selector.contains(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR)) {
            val pattern = selector.replace(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR, "(.+)") + "(.*)"
            ObjJUnifiedMethodIndex.instance.getByPatternFuzzy(pattern, baseSelector!!.getSelectorString(false).replace(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR, ""), project)
            //LOGGER.log(Level.INFO, "Getting selectors for selector pattern: <"+selector+">. Found <"+methodHeaders.size()+"> methods");
        } else {
            ObjJUnifiedMethodIndex.instance.getByPattern(selector, null, project)
            //LOGGER.log(Level.INFO, "Getting selectors with selector beginning: <"+selector+">. Found <"+methodHeaders.size()+"> methods");
        }
        //List<ObjJMethodHeaderDeclaration> methodHeaders = ObjJUnifiedMethodFragmentIndex.getInstance().get(selectorFragment, element.getProject());
        return if (!methodHeaders.isEmpty()) {
            RawResult(methodHeaders, classConstraints, baseSelector!!, selectorIndex)
        } else {
            null
        }
    }

    private fun prune(classConstraints: List<String>, methodHeaders: Map<String, List<ObjJMethodHeaderDeclaration<*>>>, subSelector: String, selectorIndex: Int): SelectorResolveResult<ObjJSelector> {
        val result = HashMap<String, MutableList<ObjJSelector>>()
        val others = HashMap<String, MutableList<ObjJSelector>>()
        var selectorElement: ObjJSelector?
        for (key in methodHeaders.keys) {
            val headers : MutableList<ObjJMethodHeaderDeclaration<*>> = methodHeaders[key] as MutableList<ObjJMethodHeaderDeclaration<*>>
            for (methodHeader in headers) {
                ProgressIndicatorProvider.checkCanceled()
                selectorElement = ObjJPsiImplUtil.getThisOrPreviousNonNullSelector(methodHeader, subSelector, selectorIndex)
                if (selectorElement == null) {
                    //LOGGER.log(Level.SEVERE, "Method header returned an empty selector in matched header");
                    continue
                }
                if (sharesContainingClass(classConstraints, methodHeader)) {
                    put(result, key, selectorElement)
                } else {
                    put(others, key, selectorElement)
                }
            }
        }
        val resultOut = getFirstElements(result)
        val othersOut = getFirstElements(others)
        return SelectorResolveResult(resultOut, othersOut, classConstraints)
    }

    private fun getFirstElements(resultSet: Map<String, List<ObjJSelector>>): List<ObjJSelector> {
        val out = ArrayList<ObjJSelector>()
        for (key in resultSet.keys) {
            val elements = resultSet[key]
            if (!elements!!.isEmpty()) {
                out.add(elements[0])
            }
        }
        return out
    }

    private fun put(map: MutableMap<String, MutableList<ObjJSelector>>, key: String, declaration: ObjJSelector) {
        if (!map.containsKey(key)) {
            map[key] = ArrayList()
        }
        map[key]!!.add(declaration)
    }

    private fun prune(classConstraints: List<String>, methodHeaders: List<ObjJMethodHeaderDeclaration<*>>, subSelector: String, selectorIndex: Int): SelectorResolveResult<ObjJSelector> {
        val result = ArrayList<ObjJSelector>()
        val others = ArrayList<ObjJSelector>()
        var selectorElement: ObjJSelector?
        for (methodHeader in methodHeaders) {
            ProgressIndicatorProvider.checkCanceled()
            selectorElement = ObjJPsiImplUtil.getThisOrPreviousNonNullSelector(methodHeader, subSelector, selectorIndex)
            if (selectorElement == null) {
                LOGGER.log(Level.SEVERE, "Method header returned an empty selector in matched header")
                continue
            }
            if (sharesContainingClass(classConstraints, methodHeader)) {
                //LOGGER.log(Level.INFO, "Method selector shares containing class in list <"+ArrayUtils.join(classConstraints)+">");
                result.add(selectorElement)
            } else {
                //LOGGER.log(Level.INFO, "Method does not share containing class");
                others.add(selectorElement)
            }
        }
        //LOGGER.log(Level.INFO, "Finished pruning method headers.");
        return packageResolveResult(result, others, classConstraints)
    }

    private fun sharesContainingClass(classConstraints: List<String>, hasContainingClass: ObjJHasContainingClass): Boolean {
        return classConstraints.isEmpty() || classConstraints.contains(ObjJClassType.UNDETERMINED) || classConstraints.contains(hasContainingClass.containingClassName)
    }

    fun getSelectorLiteralReferences(selectorElement: ObjJSelector?): SelectorResolveResult<ObjJSelector> {
        if (selectorElement == null) {
            return EMPTY_SELECTORS_RESULT
        }
        val parent = selectorElement.getParentOfType( ObjJHasMethodSelector::class.java)
        val selectorIndex = parent?.selectorStrings?.indexOf(selectorElement.getSelectorString(false)) ?: -1
        val fullSelector = parent?.selectorString ?: return EMPTY_SELECTORS_RESULT
        val containingClass = selectorElement.containingClassName
        val containingClasses = if (!isUniversalMethodCaller(containingClass)) ObjJInheritanceUtil.getAllInheritedClasses(containingClass, selectorElement.project) else null
        val result = ArrayList<ObjJSelector>()
        val otherResults = ArrayList<ObjJSelector>()

        if (DumbService.isDumb(selectorElement.project)) {
            return EMPTY_SELECTORS_RESULT
        }
        val selectorLiterals = ObjJSelectorInferredMethodIndex.instance[fullSelector, selectorElement.project]
        val subSelector = selectorElement.getSelectorString(false)
        for (selectorLiteral in selectorLiterals) {
            ProgressIndicatorProvider.checkCanceled()
            val selector = if (selectorIndex >= 0) selectorLiteral.selectorList[selectorIndex] else ObjJPsiImplUtil.findSelectorMatching(selectorLiteral, subSelector)
            if (selector != null) {
                if (containingClasses == null || containingClasses.contains(selector.containingClassName)) {
                    result.add(selector)
                } else {
                    otherResults.add(selector)
                }
            }
        }
        return packageResolveResult(result, otherResults, containingClasses)
    }


    fun getInstanceVariableSimpleAccessorMethods(selectorElement: ObjJSelector, classConstraintsIn: List<String>): SelectorResolveResult<PsiElement> {
        var classConstraints = classConstraintsIn
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement)
        }
        if (DumbService.isDumb(selectorElement.project)) {
            return packageResolveResult(emptyList(), emptyList(), classConstraints)
        }
        val parent = selectorElement.getParentOfType( ObjJHasMethodSelector::class.java)
        val fullSelector = parent?.selectorString ?: ""
        val result = ArrayList<PsiElement>()
        val otherResult = ArrayList<PsiElement>()
        for (variableDeclaration in ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, selectorElement.project]) {
            ProgressIndicatorProvider.checkCanceled()
            val atAccessors = variableDeclaration.accessor?.atAccessors
            if (sharesContainingClass(classConstraints, variableDeclaration)) {
                result.add(atAccessors ?: variableDeclaration)
            } else if (atAccessors != null){
                otherResult.add(atAccessors)
            }
        }
        return packageResolveResult(result, otherResult, classConstraints)
    }

    fun getVariableReferences(selectorElement: ObjJSelector, classConstraintsIn: List<String>): SelectorResolveResult<PsiElement> {
        var classConstraints = classConstraintsIn
        val variableName = selectorElement.getSelectorString(false)
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement)
        }
        val result = ArrayList<PsiElement>()
        val otherResult = ArrayList<PsiElement>()
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(selectorElement.project)) {
            return EMPTY_RESULT
        }
        for (declaration in ObjJInstanceVariablesByNameIndex.instance[variableName, selectorElement.project]) {
            ProgressIndicatorProvider.checkCanceled()
            val variableNameInLoop = declaration.variableName ?: continue
            if (classConstraints.contains(declaration.containingClassName)) {
                result.add(variableNameInLoop)
            } else {
                otherResult.add(variableNameInLoop)
            }
        }
        return packageResolveResult(result, otherResult, classConstraints)
    }

    private fun <T> packageResolveResult(result: List<T>, otherResult: List<T>, classConstraints: List<String>?): SelectorResolveResult<T> {
        return SelectorResolveResult(result, otherResult, classConstraints ?: emptyList())
    }

    fun getClassConstraints(element: ObjJSelector): List<String> {
        return getClassConstraints(element.getParentOfType( ObjJHasMethodSelector::class.java))
    }


    private fun getClassConstraints(element: ObjJHasMethodSelector?): List<String> {
        if (element !is ObjJMethodCall) {
            //   LOGGER.log(Level.INFO, "Selector is not in method call.");
            return emptyList()
        }
        val classConstraints: List<String>
        val methodCall = element as ObjJMethodCall?
        val callTarget:ObjJCallTarget? = methodCall?.callTarget
        if (callTarget != null) {
            val possibleClasses = getPossibleClassTypesForCallTarget(callTarget)
            if (possibleClasses.isNotEmpty()) {
                LOGGER.info("Found call target type")
                return possibleClasses
            }
        }
        //String callTargetText = ObjJCallTargetUtil.getCallTargetTypeIfAllocStatement(callTarget);
        //LOGGER.log(Level.INFO, "Getting Call Target Class Constraints for target text: <"+callTargetText+">");

        classConstraints = methodCall?.callTarget?.possibleCallTargetTypes ?: mutableListOf()
        /*if (!classConstraints.isEmpty()) {
            LOGGER.log(Level.INFO, "Call target: <"+methodCall.getCallTarget().getText()+"> is possibly of type: ["+ArrayUtils.join(classConstraints)+"]");
        } else {
            LOGGER.log(Level.INFO, "Failed to infer call target type for target named <"+methodCall.getCallTarget().getText()+">.");
        }*/
        return classConstraints
    }

    fun getPossibleClassTypesForCallTarget(callTarget:ObjJCallTarget) : List<String> {
        val qualifiedReference = callTarget.qualifiedReference ?: return listOf()
        val methodCall = qualifiedReference.methodCall
        if (methodCall != null) {
            if (methodCall.selector?.text == "alloc") {
                return ObjJInheritanceUtil.getAllInheritedClasses(methodCall.callTargetText, methodCall.project, true)
            }
        }
        val variables = qualifiedReference.variableNameList

        if (variables.size != 1) {
            return listOf()
        }
        val variableName = variables[0]
        val variableNameText = variableName.text
        val className = when (variableNameText) {
            "self" -> variableName.containingClassName
            "super" -> variableName.getContainingSuperClass(true)?.text
            else -> {
                CommentParserUtil.getVariableTypesInParent(variableName) ?: getTypeFromInstanceVariables(variableName)
            }
        } ?: return listOf(ObjJClassType.UNDETERMINED)
        return ObjJInheritanceUtil.getAllInheritedClasses(className, callTarget.project, true)
    }

    /**
     * Attempts to find a variables type, if the variable is declared as an instance variable
     * @return variable type if it is known form an instance variable declaration
     */
    private fun getTypeFromInstanceVariables(variableName:ObjJVariableName) : String? {
        val referencedVariable = variableName.reference.resolve() ?: return null
        val instanceVariable = referencedVariable.getParentOfType(ObjJInstanceVariableDeclaration::class.java) ?: return null
        val type = instanceVariable.formalVariableType
        if (type.varTypeId != null) {
            return type.varTypeId?.className?.text ?: ObjJClassType.UNDETERMINED
        }
        return type.text
     }

    class SelectorResolveResult<T> internal constructor(val naturalResult: List<T>, val otherResult: List<T>, val possibleContainingClassNames: List<String>) {
        val isNatural: Boolean = !naturalResult.isEmpty()

        val result: List<T>
            get() = if (isNatural) naturalResult else otherResult

        val isEmpty: Boolean
            get() = result.isEmpty()

        init {
            //LOGGER.log(Level.INFO, "Selector resolve result has <"+naturalResult.size()+"> natural results, and <"+otherResult.size()+"> other results");
        }
    }

    class RawResult internal constructor(internal val methodHeaders: Map<String, List<ObjJMethodHeaderDeclaration<*>>>, internal val classConstraints: List<String>, internal val baseSelector: ObjJSelector, val index: Int)

}
