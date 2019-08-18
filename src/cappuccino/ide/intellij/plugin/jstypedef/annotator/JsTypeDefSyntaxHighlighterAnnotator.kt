package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefSyntaxHighlighter
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefPropertyName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.getFileReferenceRangeInComment
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.getFileReferenceWithPrefix
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.getFrameworkTextRangeInComment
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement

/**
 * Adds highlighting colors to Objective-J Fiels
 */
class JsTypeDefSyntaxHighlighterAnnotator : Annotator {

    /**
     * Entry method to begin highlighting elements
     */
    override fun annotate(
            psiElement: PsiElement,
            annotationHolder: AnnotationHolder) {
        if (psiElement is PsiComment) {
            highlightComment(psiElement, annotationHolder)
            return
        }
        if (psiElement !is JsTypeDefElement)
            return
        when (psiElement) {
            is JsTypeDefTypeName -> highlightTypeName(psiElement, annotationHolder)
            is JsTypeDefPropertyName -> highlightPropertyName(psiElement, annotationHolder)
            is JsTypeDefFunctionName -> highlightFunctionName(psiElement, annotationHolder)
        }
    }

    /**
     * Highlights variable name elements
     * Differentiates between static class name references, and variable names
     */
    private fun highlightTypeName(variableNameElement:JsTypeDefTypeName, annotationHolder: AnnotationHolder) {
        val project = variableNameElement.project
        // Ensure indices are ready
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                highlightTypeName(variableNameElement, annotationHolder)
            }
            return
        }
    }
    /**
     * Highlights function name elements
     */
    private fun highlightFunctionName(functionName:JsTypeDefFunctionName, annotationHolder: AnnotationHolder) {
        val project = functionName.project
        // Ensure indices are ready
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                highlightFunctionName(functionName, annotationHolder)
            }
            return
        }
        when (functionName.text) {
            in NO_COLOR_KEYWORDS -> stripAnnotation(functionName, annotationHolder)
            "self" -> colorize(functionName, annotationHolder, JsTypeDefSyntaxHighlighter.KEYWORD)
        }
    }

    /**
     * Highlights property names
     */
    private fun highlightPropertyName(propertyName:JsTypeDefPropertyName, annotationHolder: AnnotationHolder) {
        val project = propertyName.project
        // Ensure indices are ready
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                highlightPropertyName(propertyName, annotationHolder)
            }
            return
        }
        if (propertyName.text in NO_COLOR_KEYWORDS) {
            stripAnnotation(propertyName, annotationHolder)
        }
    }


    /**
     * Highlights comment text for keywords
     */
    private fun highlightComment(comment: PsiComment, annotationHolder: AnnotationHolder) {
        highlightFileNameInComment(comment, annotationHolder)
        highlightFrameworkNameInComment(comment, annotationHolder)
    }

    private fun highlightFileNameInComment(comment: PsiComment, annotationHolder: AnnotationHolder) {
        val rangeInComment = comment.getFileReferenceRangeInComment(true) ?: return
        val startOffset = comment.textRange.startOffset
        colorize(TextRange.create(startOffset + rangeInComment.startOffset, startOffset+rangeInComment.endOffset), annotationHolder, JsTypeDefSyntaxHighlighter.FILE_IN_COMMENT)
    }

    private fun highlightFrameworkNameInComment(comment: PsiComment, annotationHolder: AnnotationHolder) {
        val rangeInComment = comment.getFrameworkTextRangeInComment(true) ?: return
        val startOffset = comment.textRange.startOffset
        colorize(TextRange.create(startOffset + rangeInComment.startOffset, startOffset+rangeInComment.endOffset), annotationHolder, JsTypeDefSyntaxHighlighter.FILE_IN_COMMENT)
    }

    /**
     * Strips info annotations from a given element
     * Making it appear as regular text
     */
    private fun stripAnnotation(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        annotationHolder.createInfoAnnotation(psiElement, "").enforcedTextAttributes = TextAttributes.ERASE_MARKER
    }

    /**
     * Helper function to add color and style to a given element
     */
    private fun colorize(psiElement: PsiElement, annotationHolder: AnnotationHolder, attribute:TextAttributesKey, message:String? = null) {
        annotationHolder.createInfoAnnotation(psiElement, message).textAttributes = attribute
    }

    /**
     * Helper function to add color and style to a given element
     */
    private fun colorize(range:TextRange, annotationHolder: AnnotationHolder, attribute:TextAttributesKey, message:String? = null) {
        annotationHolder.createInfoAnnotation(range, message).textAttributes = attribute
    }

    companion object {
        val NO_COLOR_KEYWORDS = listOf(
                "keys",
                "keyof",
                "declare",
                "array",
                "Array",
                "Map",
                "extends",
                "module",
                "alias"
        )
    }

}