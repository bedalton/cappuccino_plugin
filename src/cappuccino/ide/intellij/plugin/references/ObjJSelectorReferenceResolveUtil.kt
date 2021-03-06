package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import java.util.logging.Logger

object ObjJSelectorReferenceResolveUtil {

    private val LOGGER = Logger.getLogger(ObjJSelectorReferenceResolveUtil::class.java.name)
    private val EMPTY_RESULT = SelectorResolveResult(emptyList(), emptyList<PsiElement>(), emptyList())
    private val EMPTY_SELECTORS_RESULT = SelectorResolveResult(emptyList(), emptyList<ObjJSelector>(), emptyList())

    fun getMethodCallReferences(element: ObjJSelector, tag: Tag, classConstraints: List<String> = getClassConstraints(element, tag)): SelectorResolveResult<ObjJSelector> {
        val selector = element.getSelectorString(false)
        val parent = element.getParentOfType(ObjJHasMethodSelector::class.java)
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
            element: ObjJSelector?,
            includeSelf: Boolean,
            tag: Tag): SelectorResolveResult<ObjJSelector> {
        if (element == null) {
            return EMPTY_SELECTORS_RESULT
        }
        val classConstraints = getClassConstraints(element, tag)
        val parent = element.getParentOfType(ObjJHasMethodSelector::class.java)
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
            selectorElement = ObjJPsiImplUtil.getThisOrPreviousNonNullSelector(methodHeader, subSelector, selectorIndex)
            if (selectorElement == null) {
                //LOGGER.severe("Method header returned an empty selector in matched header")
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
        return classConstraints.isEmpty() || ObjJClassType.UNDETERMINED in classConstraints || ObjJClassType.ID in classConstraints || classConstraints.contains(hasContainingClass.containingClassName)
    }

    /**
     * Gets completion based on selector literals.
     */
    fun getSelectorLiteralReferences(selectorElement: ObjJSelector?): SelectorResolveResult<ObjJSelector> {
        if (selectorElement == null) {
            return EMPTY_SELECTORS_RESULT
        }
        val parent = selectorElement.getParentOfType(ObjJHasMethodSelector::class.java)
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
    fun getInstanceVariableSimpleAccessorMethods(selectorElement: ObjJSelector, classConstraintsIn: List<String>, tag: Tag): SelectorResolveResult<PsiElement> {
        var classConstraints = classConstraintsIn
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement, tag)
        }
        if (DumbService.isDumb(selectorElement.project)) {
            return packageResolveResult(emptyList(), emptyList(), classConstraints)
        }
        val parent = selectorElement.getParentOfType(ObjJHasMethodSelector::class.java)
        val fullSelector = parent?.selectorString ?: ""
        val result = ArrayList<PsiElement>()
        val otherResult = ArrayList<PsiElement>()
        for (variableDeclaration in ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, selectorElement.project]) {
            val atAccessors = variableDeclaration.accessor?.atAccessors
            if (sharesContainingClass(classConstraints, variableDeclaration)) {
                result.add(atAccessors ?: variableDeclaration)
            } else if (atAccessors != null) {
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
    fun getVariableReferences(selectorElement: ObjJSelector, classConstraintsIn: List<String>, tag: Tag): SelectorResolveResult<PsiElement> {
        var classConstraints = classConstraintsIn
        val variableName = selectorElement.getSelectorString(false)
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement, tag)
        }
        val result = ArrayList<PsiElement>()
        val otherResult = ArrayList<PsiElement>()
        if (DumbService.isDumb(selectorElement.project)) {
            return EMPTY_RESULT
        }
        for (declaration in ObjJInstanceVariablesByNameIndex.instance[variableName, selectorElement.project]) {
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

    class SelectorResolveResult<T> internal constructor(private val naturalResult: List<T>, private val otherResult: List<T>, val possibleContainingClassNames: List<String>) {
        private val isNatural: Boolean = naturalResult.isNotEmpty()

        val result: List<T>
            get() = if (isNatural) naturalResult else otherResult

        val isEmpty: Boolean
            get() = result.isEmpty()

        val isNotEmpty: Boolean
            get() = result.isNotEmpty()
    }
}
