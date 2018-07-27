package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.fixes.ObjJAlterIgnoredSelector
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil
import cappuccino.ide.intellij.plugin.utils.*

import java.util.ArrayList

/**
 * Annotator for method calls
 */
internal object ObjJMethodCallAnnotatorUtil {

    //private static final Logger LOGGER = Logger.getLogger(ObjJMethodCallAnnotatorUtil.class.getName());

    /**
     * Responsible for annotating method calls
     * @param methodCall method call to annotate
     * @param holder annotation holder used for markup
     */
    fun annotateMethodCall(
            methodCall: ObjJMethodCall,
            holder: AnnotationHolder) {
        //First validate that all selector sub elements are present
        validateMissingSelectorElements(methodCall, holder)
        //Validate the method for selector exists. if not, stop annotation
        if (!validMethodSelector(methodCall, holder)) {
            //Method call is invalid, stop annotations
            return
        }
        //Check that call target for method call is valid
        //Only used is setting for validate call target is set.
        if (ObjJPluginSettings.validateCallTarget()) {// && !IgnoreUtil.shouldIgnore(methodCall, ElementType.CALL_TARGET)) {
            validateCallTarget(methodCall, holder)
        }
    }

    /**
     * Validates and annotates missing children of selector elements
     * somewhat of a hack for the way selector elements are handled in the psi tree
     * @param methodCall method call to evaluate
     * @param holder annotation holder
     */
    private fun validateMissingSelectorElements(methodCall: ObjJMethodCall, holder: AnnotationHolder) {
        if (methodCall.selectorList.size > 1) {
            for (selector in methodCall.qualifiedMethodCallSelectorList) {
                if (selector.exprList.isEmpty() && selector.selector != null) {
                    holder.createErrorAnnotation(selector.selector!!, "Missing expression")
                    return
                }
            }
        }
    }

    /*
     * Validates and annotates a selector literal
     * @param selectorLiteral selector literal
     * @param holder annotation holder
     * /
    fun annotateSelectorLiteral(
            selectorLiteral: ObjJSelectorLiteral,
            holder: AnnotationHolder) {
        //TODO annotations for selector literals are in some cases selector contracts or declarations
        /*
        final Project project = selectorLiteral.getProject();
        final List<ObjJSelector> selectors = selectorLiteral.getSelectorList();
        annotateSelectorReference(project, selectors, holder);
        */
    }*/

    /**
     * Validates and annotates method selector signature
     * @param methodCall method call
     * @param holder annotation holder
     * @return **true** if valid, **false** otherwise
     */
    private fun validMethodSelector(methodCall: ObjJMethodCall, holder: AnnotationHolder): Boolean {

        /*if (false && IgnoreUtil.shouldIgnore(methodCall, ElementType.METHOD)) {
            return true;
        }*/
        //Checks that there are selectors
        val selectors = methodCall.selectorList
        if (selectors.isEmpty()) {
            return false
        }
        val project = methodCall.project
        //Get full selector signature
        val fullSelector = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)

        //Check that method selector signature is valid, and return if it is
        if (isValidMethodCall(fullSelector, project)) {
            return true
        }


        if (ObjJPluginSettings.isIgnoredSelector(fullSelector)) {
            for (selector in selectors) {
                holder.createInfoAnnotation(selector, "missing selector: <$fullSelector> is ignored")
                        .registerFix(ObjJAlterIgnoredSelector(fullSelector, false))
            }
            return true
        }

        if (selectors.size == 1) {
            val selector = selectors.getOrNull(0) ?: return true
            holder.createErrorAnnotation(selector, "Failed to find selector matching <${selector.getSelectorString(true)}>")
                    .registerFix(ObjJAlterIgnoredSelector(fullSelector, true))
            return false
        }
        //Selector is invalid, so find first non-matching selector
        val failIndex = getSelectorFailedIndex(methodCall.selectorStrings, project)

        //If fail index is less than one, mark all selectors and return;
        if (failIndex < 0) {
            //LOGGER.log(Level.INFO, "Selector fail index returned a negative index.");
            holder.createErrorAnnotation(methodCall, "Failed to find selector matching <$fullSelector>")
                    .registerFix(ObjJAlterIgnoredSelector(fullSelector, true))
            return false
        }

        val selectorToFailPoint = StringBuilder(ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors.subList(0, failIndex)))
        val methodCallSelectors = methodCall.qualifiedMethodCallSelectorList
        //assert methodCallSelectors.size() == methodCall.getSelectorStrings().size() : "Method call is returning difference lengthed selector lists. Call: <"+methodCall.getText()+">. Qualified: <"+methodCallSelectors.size()+">; Strings: <"+methodCall.getSelectorStrings().size()+">";
        val numSelectors = methodCallSelectors.size
        var selector: ObjJQualifiedMethodCallSelector
        var failPoint: PsiElement?
        //Markup invalid
        for (i in failIndex until numSelectors) {
            selector = methodCallSelectors[i]
            failPoint = if (selector.selector != null && !selector.selector!!.text.isEmpty()) selector.selector else selector.colon
            selectorToFailPoint.append(ObjJMethodPsiUtils.getSelectorString(selectors[i], true))
            holder.createErrorAnnotation(failPoint!!, "Failed to find selector matching <" + selectorToFailPoint.toString() + ">")
                    .registerFix(ObjJAlterIgnoredSelector(fullSelector, true))
        }

        return false
    }

    /**
     * Brute force method to check if method call is valid
     * @param fullSelector full selector for method call
     * @param project project
     * @return true if method selector is valid in any place, false otherwise
     */
    private fun isValidMethodCall(fullSelector: String, project: Project): Boolean {
        return !ObjJUnifiedMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJSelectorInferredMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJInstanceVariablesByNameIndex.instance[fullSelector.substring(0, fullSelector.length - 1), project].isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, project].isEmpty()
    }

    /**
     * Gets selector index where selector stops being valid.
     * @param selectors selector list
     * @param project project
     * @return index of first invalid selector
     */
    private fun getSelectorFailedIndex(selectors: List<String>, project: Project): Int {
        if (selectors.size < 2 || DumbService.isDumb(project)) {
            return 0
        }
        val builder = StringBuilder()
        var selector: String
        for (i in selectors.indices) {
            selector = selectors[i]
            builder.append(selector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            selector = builder.toString()
            if (!ObjJUnifiedMethodIndex.instance.getStartingWith(selector, project).isEmpty() || !ObjJSelectorInferredMethodIndex.instance.getStartingWith(selector, project).isEmpty()) {
                continue
            }
            //LOGGER.log(Level.INFO, "Selector match failed at index: <"+i+"> in with selector: <"+builder.toString()+">");
            return i
        }
        return 0
    }

    /**
     * Validates and annotates the call target of a method call to ensure method call is possible.
     * @param methodCall method call to check
     * @param holder annotation holder
     */
    private fun validateCallTarget(methodCall: ObjJMethodCall, holder: AnnotationHolder) {
        if (methodCall.selectorList.isEmpty()) {
            return
        }
        val selectorList = methodCall.selectorList
        var lastIndex = selectorList.size - 1
        var lastSelector: ObjJSelector? = selectorList[lastIndex]
        while (lastSelector == null && lastIndex > 0) {
            lastSelector = selectorList[--lastIndex]
        }
        if (lastSelector == null) {
            return
        }
        var selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(lastSelector)
        var weakWarning: String? = null
        val childClasses = ArrayList<String>()
        val expectedContainingClasses: List<String>
        if (!selectorResult.isEmpty) {
            if (selectorResult.isNatural) {
                return
            }
            expectedContainingClasses = selectorResult.possibleContainingClassNames
            for (containingClass in expectedContainingClasses) {
                ProgressIndicatorProvider.checkCanceled()
                childClasses.addAll(ObjJClassInheritanceIndex.instance.getChildClassesAsStrings(containingClass, methodCall.project))
            }
            val actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNamesFromSelector(selectorResult.result)
            val childContainingClasses = ArrayList<String>()
            for (className in actualContainingClasses) {
                if (childClasses.contains(className) && !childContainingClasses.contains(className)) {
                    childContainingClasses.add(className)
                }
            }
            weakWarning = if (!childContainingClasses.isEmpty()) {
                "Method found in possible child classes [" + ArrayUtils.join(childContainingClasses) + "] of [" + ArrayUtils.join(expectedContainingClasses) + "]"
            } else {
                "Method found in classes [" + ArrayUtils.join(actualContainingClasses) + "], not in inferred classes [" + ArrayUtils.join(expectedContainingClasses) + "]"
            }
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(lastSelector)
        if (!selectorResult.isEmpty) {
            if (selectorResult.isNatural) {
                return
            } else if (weakWarning == null) {
                weakWarning = "Method seems to reference a selector literal, but not in enclosing class"
            }
        }
        if (selectorList.size == 1) {
            var psiElementResult = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(selectorList[0], selectorResult.possibleContainingClassNames)
            if (!psiElementResult.isEmpty) {
                if (psiElementResult.isNatural) {
                    return
                } else if (weakWarning == null) {
                    val actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNames(psiElementResult.result)
                    weakWarning = "Selector seems to reference an accessor method in classes [" + ArrayUtils.join(actualContainingClasses) + "], not in the inferred classes [" + ArrayUtils.join(psiElementResult.possibleContainingClassNames) + "]"
                }
            } else {
                psiElementResult = ObjJSelectorReferenceResolveUtil.getVariableReferences(selectorList[0], psiElementResult.possibleContainingClassNames)
                if (!psiElementResult.isEmpty) {
                    if (psiElementResult.isNatural) {
                        return
                    } else if (weakWarning == null) {
                        val actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNames(psiElementResult.result)
                        weakWarning = "Selector seems to reference an instance variable in [" + ArrayUtils.join(actualContainingClasses) + "], not in the inferred classes [" + ArrayUtils.join(psiElementResult.possibleContainingClassNames) + "]"
                    }
                }
            }
        }
        if (weakWarning != null) {
            for (selector in methodCall.selectorList) {
                holder.createWeakWarningAnnotation(selector, weakWarning)
            }
        }
    }

}
