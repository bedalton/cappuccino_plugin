package org.cappuccino_project.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettings
import org.cappuccino_project.ide.intellij.plugin.indices.*
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil
import org.cappuccino_project.ide.intellij.plugin.utils.*

import java.util.ArrayList
import java.util.regex.Pattern

/**
 * Annotator for method calls
 */
internal object ObjJMethodCallAnnotatorUtil {
    private val CPSTRING_INIT_WITH_FORMAT = "initWithFormat"
    private val CPSTRING_STRING_WITH_FORMAT = "stringWithFormat"

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

        //Annotate static vs instance method calls.
        annotateStaticMethodCall(methodCall, holder)
        if (isCPStringWithFormat(methodCall)) {
            annotateStringWithFormat(methodCall, holder)
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
     * Validates and annotates static vs instance method calls.
     * @param methodCall method call to annotate
     * @param holder annotation holder
     */
    private fun annotateStaticMethodCall(methodCall: ObjJMethodCall, holder: AnnotationHolder) {
        /*if(IgnoreUtil.shouldIgnore(methodCall, ElementType.METHOD_SCOPE)) {
            return;
        }*/
        val callTarget = methodCall.callTarget.text
        val possibleCallTargetClassTypes = if (ObjJPluginSettings.validateCallTarget()) methodCall.callTarget.getPossibleCallTargetTypes() else null
        if (callTarget == "self" || callTarget == "super" ||
                possibleCallTargetClassTypes != null && possibleCallTargetClassTypes.contains(ObjJClassType.UNDETERMINED)) {
            return
        }
        val isStaticReference = possibleCallTargetClassTypes?.contains(callTarget)
                ?: !ObjJClassDeclarationsIndex.instance.get(callTarget, methodCall.project).isEmpty()
        if (DumbService.isDumb(methodCall.project)) {
            return
        }
        val methodHeaderDeclarations = ObjJUnifiedMethodIndex.instance.get(methodCall.selectorString, methodCall.project)
        var annotate = false
        for (declaration in methodHeaderDeclarations) {
            ProgressIndicatorProvider.checkCanceled()
            if (possibleCallTargetClassTypes != null && !possibleCallTargetClassTypes.contains(declaration.containingClassName)) {
                continue
            }
            if (declaration.isStatic == isStaticReference || declaration.isStatic && (possibleCallTargetClassTypes == null || possibleCallTargetClassTypes.contains(ObjJClassType.CLASS))) {
                return
            } else {
                annotate = true
            }
        }
        if (!annotate) {
            return
        }
        for (selector in methodCall.selectorList) {
            val warning = if (!isStaticReference && possibleCallTargetClassTypes != null) "Static method called from class instance with inferred type of [" + ArrayUtils.join(possibleCallTargetClassTypes, ", ", false) + "]" else "Instance method called from static reference"
            holder.createWeakWarningAnnotation(selector, warning)
        }
    }

    /**
     * Validates and annotates a selector literal
     * @param selectorLiteral selector literal
     * @param holder annotation holder
     */
    fun annotateSelectorLiteral(
            selectorLiteral: ObjJSelectorLiteral,
            holder: AnnotationHolder) {
        //TODO annotations for selector literals are in some cases selector contracts or declarations
        /*
        final Project project = selectorLiteral.getProject();
        final List<ObjJSelector> selectors = selectorLiteral.getSelectorList();
        annotateSelectorReference(project, selectors, holder);
        */
    }

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
        }// else {
        //LOGGER.log(Level.INFO, "Failed to find indexed selector matching : <" + fullSelector + ">;");
        //}
        if (selectors.size == 1) {
            val selector = selectors[0]
            holder.createErrorAnnotation(selector, "Failed to find selector matching <" + selector.getSelectorString(true) + ">")
            return false
        }
        //Selector is invalid, so find first non-matching selector
        val failIndex = getSelectorFailedIndex(methodCall.selectorStrings, project)

        //If fail index is less than one, mark all selectors and return;
        if (failIndex < 0) {
            //LOGGER.log(Level.INFO, "Selector fail index returned a negative index.");
            holder.createErrorAnnotation(methodCall, "Failed to find selector matching <$fullSelector>")
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
        return !ObjJUnifiedMethodIndex.instance.get(fullSelector, project).isEmpty() ||
                !ObjJSelectorInferredMethodIndex.instance.get(fullSelector, project).isEmpty() ||
                !ObjJInstanceVariablesByNameIndex.instance.get(fullSelector.substring(0, fullSelector.length - 1), project).isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance.get(fullSelector, project).isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance.get(fullSelector, project).isEmpty()
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
            if (!childContainingClasses.isEmpty()) {
                weakWarning = "Method found in possible child classes [" + ArrayUtils.join(childContainingClasses) + "] of [" + ArrayUtils.join(expectedContainingClasses) + "]"
            } else {
                weakWarning = "Method found in classes [" + ArrayUtils.join(actualContainingClasses) + "], not in inferred classes [" + ArrayUtils.join(expectedContainingClasses) + "]"
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

    /**
     * Validates and annotates CPString formatter
     * Simply checks that there are enough arguments in the list for all wildcards in string format
     * @param methodCall method call
     * @param annotationHolder annotation holder
     */
    private fun annotateStringWithFormat(methodCall: ObjJMethodCall, annotationHolder: AnnotationHolder) {
        if (!isCPStringWithFormat(methodCall)) {
            return
        }
        if (methodCall.qualifiedMethodCallSelectorList.isEmpty()) {
            return
        }
        val expressions = methodCall.qualifiedMethodCallSelectorList[0].exprList
        if (expressions.size < 1) {
            annotationHolder.createWarningAnnotation(methodCall, "String with format requires first parameter to be a non-nil string")
        }
        val format = expressions.removeAt(0)
        var formatVarType: String?
        try {
            formatVarType = getReturnType(format, true)
        } catch (e: MixedReturnTypeException) {
            formatVarType = e.returnTypesList[0]
        }

        if (formatVarType == null) {
            return
        }
        if (!isUniversalMethodCaller(formatVarType) && formatVarType != ObjJClassType.STRING) {
            annotationHolder.createWarningAnnotation(format, "First parameter should be of type CPString")
            return
        }
        if (format.leftExpr == null || format.leftExpr!!.primary == null || format.leftExpr!!.primary!!.stringLiteral == null) {
            //   LOGGER.log(Level.INFO, "[CPString initWithFormat] should have string expression first, but does not. Actual text: <"+format.getText()+">");
            return
        }

        val formatString = format.leftExpr!!.primary!!.stringLiteral!!.text
        val pattern = Pattern.compile("%([^%])*")
        val matchResult = pattern.matcher(formatString)
        val matches = ArrayList<String>()
        while (matchResult.find()) {
            matches.add(matchResult.group())
        }
        val numMatches = matches.size
        val numExpressions = expressions.size
        if (numMatches > numExpressions) {
            val elementOffset = format.leftExpr!!.primary!!.stringLiteral!!.textRange.startOffset
            val parts = formatString.split("%([^%])".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var part: String
            val builder = StringBuilder()
            var offset: Int
            val lastIndex = parts.size - if (formatString.lastIndexOf("%") == formatString.length - 2 && formatString.lastIndexOf("%") != formatString.length - 1) 0 else 1
            for (i in 0 until lastIndex) {
                part = parts[i]
                builder.append(part)
                offset = elementOffset + builder.length
                builder.append("%?")
                if (i < numExpressions) {
                    continue
                }
                //LOGGER.log(Level.INFO, "Current substring = <"+builder.toString()+">");
                annotationHolder.createWarningAnnotation(TextRange.create(offset, offset + 2), String.format("Not enough values for format. Expected <%d>, found <%d>", numMatches, numExpressions))
            }
        } else if (numMatches < numExpressions) {
            for (i in numMatches until numExpressions) {
                annotationHolder.createWarningAnnotation(expressions[i], String.format("Too many arguments found for string format. Expected <%d>, found <%d>", numMatches, numExpressions))
            }
        }
        for (i in 1..numMatches) {
            if (expressions.size < 1) {
                break
            }
            val expr = expressions.removeAt(0)
            //TODO check var type for match
        }
    }

    /**
     * Checks whether method call is a CPString format method call.
     * @param methodCall method call
     * @return true if method call is string formatting method call, false otherwise.
     */
    private fun isCPStringWithFormat(methodCall: ObjJMethodCall): Boolean {
        if (methodCall.getCallTargetText() == ObjJClassType.STRING && methodCall.selectorList.size == 1) {
            val selectorText = methodCall.selectorList[0].text
            return selectorText == CPSTRING_INIT_WITH_FORMAT || selectorText == CPSTRING_STRING_WITH_FORMAT
        }
        return false
    }

}
