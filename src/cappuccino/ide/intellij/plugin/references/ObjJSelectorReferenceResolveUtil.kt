package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil

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

        val methodHeaders = ObjJUnifiedMethodIndex.instance[fullSelector, element.project]
        return prune(classConstraints, methodHeaders, element.text, selectorIndex)
    }

    /**
     * Gets method selectors with matching first part of string
     */
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

    /**
     * Prunes empty selectors and sorts them
     * @returns a selector resolve result with selectors sorted based on perceived applicability
     */
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
                result.add(selectorElement)
            } else {
                others.add(selectorElement)
            }
        }
        return packageResolveResult(result, others, classConstraints)
    }

    /**
     * Checks if a containing class is in a set of class constraints
     * returns true if an UNDETERMINED class identifier has been found
     */
    private fun sharesContainingClass(classConstraints: List<String>, hasContainingClass: ObjJHasContainingClass): Boolean {
        return classConstraints.isEmpty() || classConstraints.contains(ObjJClassType.UNDETERMINED) || classConstraints.contains(hasContainingClass.containingClassName)
    }

    /**
     * Gets completion based on selector literals.
     */
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
        return packageResolveResult(result, otherResults, containingClasses?.toList())
    }


    /**
     * Gets accessor method references
     */
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


    /**
     * Can be used to find if selector matches an objects properties
     * currently unused
     */
    @Suppress("unused")
    fun getVariableReferences(selectorElement: ObjJSelector, classConstraintsIn: List<String>): SelectorResolveResult<PsiElement> {
        var classConstraints = classConstraintsIn
        val variableName = selectorElement.getSelectorString(false)
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement)
        }
        val result = ArrayList<PsiElement>()
        val otherResult = ArrayList<PsiElement>()
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
            return emptyList()
        }
        val methodCall = element as ObjJMethodCall?
        val callTarget:ObjJCallTarget? = methodCall?.callTarget
        if (callTarget != null) {
            val possibleClasses = getPossibleClassTypesForCallTarget(callTarget)
            if (possibleClasses.isNotEmpty()) {
                return possibleClasses.toList()
            }
        }
        return methodCall?.callTarget?.possibleCallTargetTypes ?: mutableListOf()
    }

    fun getPossibleClassTypesForCallTarget(callTarget:ObjJCallTarget) : Set<String> {
        val qualifiedReference = callTarget.qualifiedReference ?: return setOf()
        val methodCall = qualifiedReference.methodCall
        if (methodCall != null) {
            if (methodCall.selector?.text == "alloc") {
                return ObjJInheritanceUtil.getAllInheritedClasses(methodCall.callTargetText, methodCall.project, true)
            }
        }
        val variables = qualifiedReference.variableNameList

        if (variables.size != 1) {
            return setOf()
        }
        val variableName = variables[0]
        val variableNameText = variableName.text
        val className = when (variableNameText) {
            "self" -> variableName.containingClassName
            "super" -> variableName.getContainingSuperClass(true)?.text
            else -> {
                ObjJIgnoreEvaluatorUtil.getVariableTypesInParent(variableName) ?: getTypeFromInstanceVariables(variableName)
            }
        } ?: return ObjJVariableTypeResolver.resolveVariableType(variableName)
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

    class SelectorResolveResult<T> internal constructor(private val naturalResult: List<T>, private val otherResult: List<T>, val possibleContainingClassNames: List<String>) {
        private val isNatural: Boolean = !naturalResult.isEmpty()

        val result: List<T>
            get() = if (isNatural) naturalResult else otherResult

        val isEmpty: Boolean
            get() = result.isEmpty()
    }
}
