package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.fixes.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.lang.annotation.Annotation

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
            holder: AnnotationHolder) {

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

    /**
     * Validates and annotates method selector signature
     * @param methodCall method call
     * @param annotationHolder annotation annotationHolder
     * @return **true** if valid, **false** otherwise
     */
    private fun validMethodSelector(methodCall: ObjJMethodCall, annotationHolder: AnnotationHolder): Boolean {
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
            val annotation = annotationHolder.createErrorAnnotation(selector, "Failed to find selector matching <${selector.getSelectorString(true)}>")
            addInvalidSelectorFixes(annotation, methodCall, fullSelector)
            return false
        }

        //Selector is invalid, so find first non-matching selector
        val failIndex = getSelectorFailedIndex(methodCall.selectorStrings, project)

        //If fail index is less than one, mark all selectors and return;
        if (failIndex < 0) {
            val annotation = annotationHolder.createErrorAnnotation(methodCall, "Failed to find selector matching <$fullSelector>")
            addInvalidSelectorFixes(annotation, methodCall, fullSelector)
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
    private fun annotateInvalidSelectorsIndividually(methodCall:ObjJMethodCall, selectors:List<ObjJSelector>, failIndex:Int, fullSelector: String, annotationHolder: AnnotationHolder){
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
            annotationHolder: AnnotationHolder
    ) {
        // Uses fail point and not strictly the selector as some
        // qualified selectors do not have text selectors, but are just colons
        val failPoint = if (selector.selector != null && !selector.selector!!.text.isEmpty()) selector.selector else selector.colon

        // Append fail text to this option
        selectorToFailPointTextSoFar.append(getSelectorString(selector.selector, true))

        // Create annotation
        val annotation = annotationHolder.createErrorAnnotation(failPoint!!, "Failed to find selector matching <$selectorToFailPointTextSoFar>")
        annotation.setNeedsUpdateOnTyping(true)
        // Add fixes
        addInvalidSelectorFixes(annotation, methodCall, fullSelector)
    }

    /**
     * Adds all fixes for an invalid selector
     * @todo add fix to add a selector matching to a class
     */
    private fun addInvalidSelectorFixes(annotation: Annotation, methodCall: ObjJMethodCall, fullSelector: String) {
        annotation.registerFix(ObjJAlterIgnoredSelector(fullSelector, true))
        for(scope in scopeList) {
            val fix = ObjJAddSuppressInspectionForScope(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR, scope)
            annotation.registerFix(fix)
        }
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
    private fun isIgnored(methodCall: ObjJMethodCall, fullSelector: String, annotationHolder: AnnotationHolder) : Boolean {
        // Ensure that selector is not listed in project level ignore list
        if (ObjJPluginSettings.isIgnoredSelector(fullSelector)) {
            // If ignored, add fix to remove it from ignored list
            for (selector in methodCall.selectorList) {
                annotationHolder.createInfoAnnotation(selector, "missing selector: <$fullSelector> is ignored")
                        .registerFix(ObjJAlterIgnoredSelector(fullSelector, false))
            }
            return true
        }

        // Ensure that selector is not annoted with ignore statement
        if (ObjJIgnoreEvaluatorUtil.isIgnored(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR)) {
            return true
        }
        return false
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

}
