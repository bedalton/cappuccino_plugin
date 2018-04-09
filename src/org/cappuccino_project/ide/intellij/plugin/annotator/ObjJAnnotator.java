package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJVariableName;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon;
import org.jetbrains.annotations.NotNull;

public class ObjJAnnotator implements Annotator {
    @Override
    public void annotate(
            @NotNull
                    PsiElement psiElement,
            @NotNull
                    AnnotationHolder annotationHolder) {
        if (DumbService.getInstance(psiElement.getProject()).isDumb()) {
            return;
        }
        try {
            //Annotate Method calls
            if (psiElement instanceof ObjJMethodCall) {
                ObjJMethodCallAnnotatorUtil.annotateMethodCall((ObjJMethodCall) psiElement, annotationHolder);

                //Annotate Selector literals
            } else if (psiElement instanceof ObjJSelectorLiteral) {
                ObjJMethodCallAnnotatorUtil.annotateSelectorLiteral((ObjJSelectorLiteral) psiElement, annotationHolder);
            }
            if (psiElement instanceof ObjJVariableName) {
                ObjJVariableAnnotatorUtil.annotateVariable((ObjJVariableName) psiElement, annotationHolder);
            }
            if (psiElement instanceof ObjJNeedsSemiColon) {
                ObjJSemiColonAnnotatorUtil.annotateMissingSemiColons((ObjJNeedsSemiColon) psiElement, annotationHolder);
            }
        } catch (IndexNotReadyRuntimeException ignored) {

        }
    }
}
