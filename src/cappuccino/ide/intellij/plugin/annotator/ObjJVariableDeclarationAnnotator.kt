package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextSiblingOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange

object ObjJVariableDeclarationAnnotator {

    /**
     * Provides a unified entry point for this annotation to add clarity to Main Annotator
     */
    internal fun annotateVariableDeclarations(variableDeclaration: ObjJVariableDeclaration, annotationHolder: AnnotationHolder) {

        // Only check if direct parent if body variable assignment.
        // Looking for any inherited parent of caused problems with nested declarations
        val inBodyVariableAssignment:Boolean = (variableDeclaration.parent as? ObjJBodyVariableAssignment)?.varModifier != null

        for (qualifiedReference in variableDeclaration.qualifiedReferenceList) {
            // Check that method call is not being assigned to directly
            // Values can only be assigned to (.) or [array] expressions
            if (qualifiedReference.methodCall != null && qualifiedReference.qualifiedNameParts.isEmpty()) {
                annotationHolder.createErrorAnnotation(qualifiedReference.getNextSiblingOfType(ObjJTypes.ObjJ_EQUALS)?:qualifiedReference, ObjJBundle.message("objective-j.annotator-messages.variable-declaration-annotator.cannotAssignValueToMethodCall"))
                return
            }
            // Check that there is not a qualified reference in a 'var' declaration
            if (inBodyVariableAssignment && qualifiedReference.qualifiedNameParts.size > 1) {
                val textRange: TextRange
                val firstDot = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)
                if (firstDot != null) {
                    textRange = firstDot.textRange
                } else {
                    val startOffsetTemp = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)?.textRange?.startOffset
                            ?: qualifiedReference.qualifiedNameParts.getOrNull(1)?.textRange?.startOffset
                    val startOffset: Int
                    startOffset = if (startOffsetTemp != null) {
                        startOffsetTemp - 1
                    } else {
                        qualifiedReference.textRange.startOffset
                    }
                    textRange = TextRange.create(startOffset, variableDeclaration.textRange.endOffset)
                }
                annotationHolder.createErrorAnnotation(textRange, ObjJBundle.message("objective-j.annotator-messages.variable-declaration-annotator.qualifiedWithVar"))
                return
            }
            // Check that the last part of a qualified name is not a function call
            // as these cannot be assigned to
            val lastChild = qualifiedReference.qualifiedNameParts.last() ?: return
            if (lastChild is ObjJFunctionCall) {
                annotationHolder.createErrorAnnotation(TextRange(lastChild.textRange.startOffset, variableDeclaration.textRange.endOffset), ObjJBundle.message("objective-j.annotator-messages.variable-declaration-annotator.cannotAssignValueToFunctionCall"))
            }
        }
    }
}