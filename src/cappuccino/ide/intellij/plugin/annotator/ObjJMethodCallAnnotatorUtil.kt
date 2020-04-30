package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJAlterIgnoredSelector
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedMethodCallSelector
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorString
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorStringFromSelectorList
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

/**
 * Annotator/Validator of method calls
 */
internal object ObjJMethodCallAnnotatorUtil {

    // List of scopes to add fixes to
    private val scopeList = listOf(
            ObjJSuppressInspectionScope.STATEMENT,
            ObjJSuppressInspectionScope.METHOD,
            ObjJSuppressInspectionScope.FUNCTION,
            ObjJSuppressInspectionScope.CLASS,
            ObjJSuppressInspectionScope.FILE
    )

    /**
     * Responsible for annotating method calls
     * @param methodCall method call to annotate
     * @param holder annotation holder used for markup
     */
    fun annotateMethodCall(
            methodCall: ObjJMethodCall,
            holder: AnnotationHolderWrapper) {

        //First validate that all selector sub elements are present
        validateMissingSelectorElements(methodCall, holder)

        //Validate the method for selector exists. if not, stop annotation
        if (!validMethodSelector(methodCall, holder)) {
            //Method call is invalid, stop annotations
            return
        }

        // Check that call target for method call is valid
        // Only used is setting for validate call target is set.
        if (ObjJPluginSettings.validateCallTarget()) {// && !IgnoreUtil.shouldIgnore(methodCall, ElementType.CALL_TARGET)) {
            //validateCallTarget(methodCall, holder)
        }
    }

    /**
     * Validates and annotates missing children of selector elements
     * somewhat of a hack for the way selector elements are handled in the psi tree
     * @param methodCall method call to evaluate
     * @param holder annotation holder
     */
    private fun validateMissingSelectorElements(methodCall: ObjJMethodCall, holder: AnnotationHolderWrapper) {
        if (methodCall.selectorList.size > 1) {
            for (selector in methodCall.qualifiedMethodCallSelectorList) {
                if (selector.exprList.isEmpty() && selector.selector != null) {
                    holder.newErrorAnnotation(ObjJBundle.message("objective-j.annotator-messages.method-call-annotator.method-call-missing-expression.message"))
                            .range(selector.selector!!)
                            .create()
                    return
                }
            }
        }
    }

    /**
     * Validates and annotates method selector signature
     * @param methodCall method call
     * @param annotationHolder annotation annotationHolder
     * @return **true** if valid, **false** otherwise
     */
    private fun validMethodSelector(methodCall: ObjJMethodCall, annotationHolder: AnnotationHolderWrapper): Boolean {
        // Get project
        val project = methodCall.project

        //Checks that there are selectors
        val selectors = methodCall.selectorList
        if (selectors.isEmpty()) {
            return false
        }
        //Get full selector signature
        val fullSelector = getSelectorStringFromSelectorList(selectors)

        //Check that method selector signature is valid, and return if it is
        if (isValidMethodCall(fullSelector, project)) {
            return true
        }

        // Check if selector is ignored through annotations or other means
        if (isIgnored(methodCall, fullSelector, annotationHolder)) {
            return true
        }

        // If selector is single in size, markup simply
        if (selectors.size == 1) {
            val selector = selectors.getOrNull(0) ?: return true
            annotationHolder.newErrorAnnotation(ObjJBundle.message("objective-j.annotator-messages.method-call-annotator.selector-not-found.message", selector.getSelectorString(true)))
                    .range(selector)
                    .withFixes(getInvalidSelectorFixes(methodCall, fullSelector))
                    .create()
            return false
        }

        //Selector is invalid, so find first non-matching selector
        val failIndex = getSelectorFailedIndex(methodCall.selectorStrings, project)

        //If fail index is less than one, mark all selectors and return;
        if (failIndex < 0) {
            val errorMessageKey = "objective-j.annotator-messages.method-call-annotator.selector-not-found.message"
            val errorMessage = ObjJBundle.message(errorMessageKey, fullSelector);
             annotationHolder.newErrorAnnotation(errorMessage)
                     .range(methodCall)
                     .withFixes(getInvalidSelectorFixes(methodCall, fullSelector))
                     .create()
            return false
        }

        // Annotate all selectors individually
        annotateInvalidSelectorsIndividually(methodCall, selectors, failIndex, fullSelector, annotationHolder)
        return false
    }

    /**
     * Annotates only the selectors not matching any known selector
     * Highlights these and adds fixes on a selector by selector basis
     */
    private fun annotateInvalidSelectorsIndividually(methodCall:ObjJMethodCall, selectors:List<ObjJSelector>, failIndex:Int, fullSelector: String, annotationHolder: AnnotationHolderWrapper){
        val selectorToFailPointTextSoFar = StringBuilder(getSelectorStringFromSelectorList(selectors.subList(0, failIndex)))
        val methodCallSelectors = methodCall.qualifiedMethodCallSelectorList
        val numSelectors = methodCallSelectors.size
        var selector: ObjJQualifiedMethodCallSelector
        //loop through invalid selectors and annotate them
        for (i in failIndex until numSelectors) {
            selector = methodCallSelectors.getOrNull(i) ?: return
            annotateInvalidSelector(methodCall, selector, selectorToFailPointTextSoFar, fullSelector, annotationHolder)
        }
    }

    /**
     * Annotates a single selector with an error annotation
     */
    private fun annotateInvalidSelector(
            methodCall:ObjJMethodCall,
            selector:ObjJQualifiedMethodCallSelector,
            selectorToFailPointTextSoFar:StringBuilder,
            fullSelector: String,
            annotationHolder: AnnotationHolderWrapper
    ) {
        // Uses fail point and not strictly the selector as some
        // qualified selectors do not have text selectors, but are just colons
        val failPoint = if (selector.selector != null && !selector.selector!!.text.isEmpty()) selector.selector else selector.colon

        // Append fail text to this option
        selectorToFailPointTextSoFar.append(getSelectorString(selector.selector, true))

        // Get fixes
        val fixes = getInvalidSelectorFixes(methodCall, fullSelector)
        // Create annotation
        annotationHolder.newErrorAnnotation(ObjJBundle.message("objective-j.annotator-messages.method-call-annotator.selector-not-found.message", selectorToFailPointTextSoFar))
                .range(failPoint!!)
                .withFixes(fixes)
                .needsUpdateOnTyping(true)
                .create()
    }

    /**
     * Adds all fixes for an invalid selector
     * @todo add fix to add a selector matching to a class
     */
    private fun getInvalidSelectorFixes(methodCall: ObjJMethodCall, fullSelector: String) : List<IntentionAction> {
        val fixes = mutableListOf<IntentionAction>()
        fixes.add(ObjJAlterIgnoredSelector(fullSelector, true))
        fixes += scopeList.map {
            ObjJAddSuppressInspectionForScope(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR, it)
        }
        return fixes
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
     * Checks whether or not a selector is in any way ignored.
     */
    private fun isIgnored(methodCall: ObjJMethodCall, fullSelector: String, annotationHolder: AnnotationHolderWrapper) : Boolean {
        // Ensure that selector is not listed in project level ignore list
        if (ObjJPluginSettings.isIgnoredSelector(fullSelector)) {
            // If ignored, add fix to remove it from ignored list
            for (selector in methodCall.selectorList) {
                annotationHolder.newInfoAnnotation(ObjJBundle.message("objective-j.annotator-messages.method-call-annotator.invalid-selector-ignored.message", fullSelector))
                        .range(selector)
                        .withFix(ObjJAlterIgnoredSelector(fullSelector, false))
                        .create()
            }
            return true
        }

        // Ensure that selector is not annoted with ignore statement
        if (ObjJCommentEvaluatorUtil.isIgnored(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR)) {
            return true
        }
        return false
    }

    /**
     * Gets index of selector where selector stops being valid.
     * This allows for partial matches where possibly the
     * first selector matches a method, but the second does not
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
            return i
        }
        return 0
    }

}
