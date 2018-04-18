package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Annotator for missing semi-colons.
 * Annotator marks ObjJNeedsSemiColon elements, if missing semi-colons
 * ObjJNeedsSemiColon must be contained in a ObjJRequiresChildSemiColons element
 * @see ObjJNeedsSemiColon
 * @see ObjJChildrenRequireSemiColons
 */
public class ObjJSemiColonAnnotatorUtil {

    private static final IElementType[] NO_SEMI_COLON_BEFORE = new IElementType[] {ObjJTypes.ObjJ_CLOSE_PAREN,ObjJTypes.ObjJ_OPEN_PAREN, ObjJTypes.ObjJ_OPEN_BRACE, ObjJTypes.ObjJ_CLOSE_BRACE, ObjJTypes.ObjJ_COMMA, ObjJTypes.ObjJ_CLOSE_BRACKET, ObjJTypes.ObjJ_OPEN_BRACKET, ObjJTypes.ObjJ_COLON, ObjJTypes.ObjJ_SEMI_COLON};


    /**
     * Actual annotation method for ObjJNeedsSemiColonElements
     * @param element element to possibly annotate
     * @param annotationHolder annotation holder
     */
    public static void annotateMissingSemiColons(@NotNull
                                                   ObjJNeedsSemiColon element, @NotNull AnnotationHolder annotationHolder) {
        //Checks whether this element actually requires a semi colon and whether it already has one
        if (!requiresSemiColon(element) || isNextElementSemiColonBlocking(element) || ObjJPsiImplUtil.eos(element)) {
            return;
        }

        //Annotate element as it is missing semi-colon
        if (!didAnnotateWithErrorElement(element)) {
            doAnnotateWithAnnotationHolder(element, annotationHolder);
        }

    }

    /**
     * Try to annotate missing semi-colon error with an error element
     *
     * {NOTE} Element could not be added as PSI tree cannot be changed during annotation
     * @param element element to annotate
     * @return <code>true</code> if error element was added, <code>false</code> otherwise
     */
    private static boolean didAnnotateWithErrorElement(
            @SuppressWarnings("unused")
            final @NotNull ObjJNeedsSemiColon element) {
        return false;

        /*final PsiErrorElement errorElement = ObjJElementFactory.createSemiColonErrorElement(element.getProject());
        if (errorElement == null) {
            return false;
        }
        new WriteCommandAction.Simple(element.getProject(), element.getContainingFile()) {
            public void run() {
                element.getParent().addAfter(errorElement, element);
            }
        }.execute();
        return errorElement.getParent() == element.getParent();
        */
    }

    /**
     * Annotates missing semi-colon with the annotation holder.
     * @param element element to annotate
     * @param annotationHolder annotation holder
     */
    private static void doAnnotateWithAnnotationHolder(@NotNull ObjJNeedsSemiColon element, @NotNull AnnotationHolder annotationHolder) {
        TextRange errorRange = TextRange.create(element.getTextRange().getEndOffset()-1, element.getTextRange().getEndOffset());
        annotationHolder.createErrorAnnotation(errorRange, "Missing terminating semi-colon");
    }

    /**
     * Determines whether this element requires a semi-colon
     * @param psiElement element to check
     * @return <code>true</code> if element requires trailing semi-colon, <code>false</code> otherwise
     */
    private static boolean requiresSemiColon(@Nullable
                                              PsiElement psiElement) {
        // If this element a requires semi-colon element
        if (!(psiElement instanceof ObjJNeedsSemiColon)) {
            return false;
        }
        //Elements only need a semi-colon when they are direct descendants of a ObjJChildrenRequiresSemiColons element
        // This ensures that element that may need semi-colons are not flagged when part of a larger expression
        return psiElement.getParent() instanceof ObjJChildrenRequireSemiColons;
    }

    /**
     * Determines whether an element is within a statement that negates the need for a semi-colon. ie ')'
     * @param psiElement element to check next siblings of
     * @return <code>true</code> if next element blocks semi-colon, <code>false</code> otherwise
     */
    private static boolean isNextElementSemiColonBlocking(PsiElement psiElement) {
        IElementType nextNodeType = ObjJTreeUtil.getNextNonEmptyNodeType(psiElement, true);
        if (nextNodeType == null) {
            return false;
        }
        for (IElementType elementType : NO_SEMI_COLON_BEFORE) {
            if (nextNodeType == elementType) {
                return true;
            }
        }
        //ASTNode nextNode = ObjJTreeUtil.getNextNonEmptyNode(psiElement, true);
        //LOGGER.log(Level.INFO, "Element <"+( nextNode != null ? nextNode.getText() : "{UNDEF}")+"> is not an element blocking semi-colon");
        return false;
    }
}
