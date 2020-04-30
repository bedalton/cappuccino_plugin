package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJSyntaxHighlighter
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Adds highlighting colors to Objective-J Fiels
 */
class ObjJSyntaxHighlighterAnnotator : Annotator {

    /**
     * Entry method to begin highlighting elements
     */
    override fun annotate(
            psiElement: PsiElement,
            annotationHolderIn: AnnotationHolder) {
        if (DumbService.isDumb(psiElement.project))
            return
        val annotationHolder = AnnotationHolderWrapper(annotationHolderIn)
        when (psiElement) {
            is ObjJFormalVariableType -> annotateFormalVariableType(psiElement, annotationHolder)
            is ObjJClassName -> highlightClassName(psiElement, annotationHolder)
            is ObjJVariableName -> highlightVariableName(psiElement, annotationHolder)
            is ObjJFunctionCall -> highlightFunctionName(psiElement, annotationHolder)
            is ObjJFunctionName -> if (psiElement.hasParentOfType(ObjJFunctionCall::class.java)) colorize(psiElement, annotationHolder, ObjJSyntaxHighlighter.FUNCTION_NAME)
            is ObjJFrameworkDescriptor -> colorize(psiElement, annotationHolder, ObjJSyntaxHighlighter.STRING)
            is ObjJPropertyName -> stripAnnotation(psiElement, annotationHolder)
            else -> {
                if (psiElement.isOrHasParentOfType(ObjJPropertyName::class.java)) {
                    stripAnnotation(psiElement, annotationHolder)
                    return
                }
                when (psiElement.tokenType()) {
                    in ObjJTokenSets.VARIABLE_TYPE_KEYWORDS ->
                        stripVariableTypesAnnotationIfNotInValidBlock(psiElement, annotationHolder)
                    in ObjJTokenSets.ITERATION_STATEMENT_KEYWORDS ->
                        stripLoopWordsIfInSelector(psiElement, annotationHolder)
                }
            }
        }
    }

    /**
     * Highlights variable name elements
     * Differentiates between static class name references, and variable names
     */
    private fun highlightVariableName(variableNameElement: ObjJVariableName, annotationHolder: AnnotationHolderWrapper) {
        val project = variableNameElement.project
        // Ensure indices are ready
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                highlightVariableName(variableNameElement, annotationHolder)
            }
            return
        }

        // Gets variable name and checks for keywords usage
        val variableName = variableNameElement.text
        if (variableNameElement.hasParentOfType(ObjJClassDeclarationElement::class.java) && (variableName == "self" || variableName == "super" || variableName == "this")) {
            colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.KEYWORD)
        }

        // Get parent for use in evaluations
        val parent = variableNameElement.parent
        // Branch for parent being in qualified reference
        if (parent is ObjJQualifiedReference) {
            annotateVariableNameInQualifiedReference(variableNameElement, parent, project, annotationHolder)
        }
    }

    /**
     * Add colorization to variables names in qualified references
     * Parses reference to root declaration and colors based on that
     */
    private fun annotateVariableNameInQualifiedReference(variableNameElement: ObjJVariableName, parent: ObjJQualifiedReference, project: Project, annotationHolder: AnnotationHolderWrapper) {
        val variableName = variableNameElement.text
        val index = parent.variableNameList.indexOf(variableNameElement)
        if (index == 0) {
            if (ObjJClassDeclarationsIndex.instance[variableName, project].isNotEmpty()) {// || variableName in ObjJTypeDefIndex.instance.getAllKeys(project)) {
                colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
                return
            }
        }
        if (variableNameElement.indexInQualifiedReference != 0)
            return
        val referencedVariable: PsiElement? = ObjJVariableReference(variableNameElement).resolve(nullIfSelfReferencing = true)
                ?: return
        if (referencedVariable equals variableNameElement) {
            return
        }
        if (referencedVariable.hasParentOfType(ObjJInstanceVariableList::class.java)) {
            colorizeInstanceVariable(variableNameElement, referencedVariable, annotationHolder)
            return
        } else if (referencedVariable.hasParentOfType(ObjJMethodDeclarationSelector::class.java) || referencedVariable.hasParentOfType(ObjJFormalParameterArg::class.java)) {
            colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.PARAMETER_VARIABLE)
            return
        }/* else if (referencedVariable.hasParentOfType(ObjJBlockElement::class.java)) {

        } */else if (referencedVariable.hasParentOfType(ObjJGlobalVariableDeclaration::class.java) || referencedVariable.hasParentOfType(ObjJGlobal::class.java) || referencedVariable?.containingFile is JsTypeDefFile) {
            colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.GLOBAL_VARIABLE)
            return
        } else if (referencedVariable.getParentOfType(ObjJBodyVariableAssignment::class.java)?.getContainingScope() == ReferencedInScope.FILE) {
            val containingClassName = (referencedVariable ?: variableNameElement).containingFileName
            val message = ObjJBundle.message("objective-j.general.defined-in-file.text", containingClassName)
            colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.FILE_LEVEL_VARIABLE, message)
        }

    }

    /**
     * Colors instance variables and annotates them with their declared class
     */
    private fun colorizeInstanceVariable(variableNameElement: ObjJVariableName, referencedVariable: PsiElement?, annotationHolder: AnnotationHolderWrapper) {
        val message: String? = if (referencedVariable is ObjJVariableName) {
            ObjJBundle.message("objective-j.general.defined-in-class.text", referencedVariable.containingClassName)
        } else {
            null
        }
        colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.INSTANCE_VARIABLE, message)
    }

    /**
     * Highlights a given classname element
     */
    private fun highlightClassName(classNameElement: ObjJClassName, annotationHolder: AnnotationHolderWrapper) {
        colorize(classNameElement, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
    }

    /**
     * Strips variable type highlighting, if it is not used inside a valid block
     * Sometimes keywords are used as variable names, and this has to remove misleading colorization
     */
    private fun stripVariableTypesAnnotationIfNotInValidBlock(psiElement: PsiElement, annotationHolder: AnnotationHolderWrapper) {
        if (psiElement.hasParentOfType(ObjJBlock::class.java)) {
            stripAnnotation(psiElement, annotationHolder)
            return
        }
        if (psiElement.doesNotHaveParentOfType(ObjJInstanceVariableList::class.java) && psiElement.doesNotHaveParentOfType(ObjJMethodDeclarationSelector::class.java)) {
            stripAnnotation(psiElement, annotationHolder)
        }
    }

    private fun stripLoopWordsIfInSelector(psiElement: PsiElement, annotationHolder: AnnotationHolderWrapper) {
        if (psiElement.isOrHasParentOfType(ObjJSelector::class.java)) {
            stripAnnotation(psiElement, annotationHolder)
        }
    }

    private fun annotateFormalVariableType(type: ObjJFormalVariableType, annotationHolder: AnnotationHolderWrapper) {
        colorize(type, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
    }

    private fun highlightFunctionName(functionCall: ObjJFunctionCall, annotationHolder: AnnotationHolderWrapper) {
        if (functionCall.indexInQualifiedReference > 0)
            return
        val functionName = functionCall.functionName ?: return
        val resolved = functionName.reference.resolve()
        if (resolved?.getParentOfType(ObjJMethodHeader::class.java) != null) {
            colorize(functionName, annotationHolder, ObjJSyntaxHighlighter.PARAMETER_VARIABLE)
            return
        }
        if (resolved?.getParentOfType(ObjJInstanceVariableList::class.java) != null) {
            colorize(functionName, annotationHolder, ObjJSyntaxHighlighter.INSTANCE_VARIABLE)
            return
        }
        if (resolved == null)
            return

        val commonScope = PsiTreeUtil.findCommonContext(resolved, functionCall)?.getContainingScope()
        if (commonScope == null && functionCall.indexInQualifiedReference > 0) {
            return
        }

        if (commonScope == null || resolved.getContainingScope() == ReferencedInScope.FILE || commonScope == ReferencedInScope.FILE) {
            colorize(functionName, annotationHolder, ObjJSyntaxHighlighter.GLOBAL_FUNCTION_NAME, ObjJBundle.message("objective-j.general.defined-in-file.text", functionName.containingFileName))
        }

    }

    /**
     * Strips info annotations from a given element
     * Making it appear as regular text
     */
    private fun stripAnnotation(psiElement: PsiElement, annotationHolder: AnnotationHolderWrapper) {
        annotationHolder.newInfoAnnotation("")
                .range(psiElement)
                .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
                .create()
    }

    /**
     * Helper function to add color and style to a given element
     */
    private fun colorize(psiElement: PsiElement, annotationHolder: AnnotationHolderWrapper, attribute: TextAttributesKey, message: String? = null) {
        annotationHolder.newInfoAnnotation(message)
                .range(psiElement)
                .textAttributes(attribute)
                .create()
    }
}