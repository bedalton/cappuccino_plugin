package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class ObjJProtocolDeclarationAnnotatorUtil {

    static void annotateProtocolDeclaration(@NotNull
                                        ObjJProtocolDeclaration protocolDeclaration, @NotNull AnnotationHolder annotationHolder)
    {
        annotateIfDuplicateProtocol(protocolDeclaration, annotationHolder);
    }


    private static void annotateIfDuplicateProtocol(@NotNull
                                                            ObjJProtocolDeclaration thisProtocolDeclaration, @NotNull AnnotationHolder annotationHolder) {
        final ObjJClassName classNameElement = thisProtocolDeclaration.getClassName();
        if (classNameElement == null) {
            return;
        }
        final String className = classNameElement.getText();
        final List<ObjJProtocolDeclaration> duplicates = new ArrayList<>();
        for (ObjJProtocolDeclaration protocolDeclaration :
                ObjJProtocolDeclarationsIndex.getInstance().get(className, classNameElement.getProject())) {
            if (protocolDeclaration.isEquivalentTo(thisProtocolDeclaration)) {
                continue;
            }
            duplicates.add(protocolDeclaration);
        }
        if (duplicates.isEmpty()) {
            return;
        }
        StringBuilder errorMessage = new StringBuilder("Duplicate protocol <"+className+"> found in ");
        for (ObjJProtocolDeclaration protocolDeclaration : duplicates) {
            errorMessage.append(ObjJFileUtil.getContainingFileName(protocolDeclaration)).append(", ");
        }
        annotationHolder.createErrorAnnotation(classNameElement, errorMessage.substring(0, errorMessage.length()-2));

    }

}
