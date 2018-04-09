package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.project.Project;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;

import java.util.List;

public class ObjJProtocolAnnotatorUtil {


    public static void annotateImplementationDeclaration(ObjJImplementationDeclaration declaration) {
        annotateUnimplementedProtocols(declaration);
    }

    private static void annotateUnimplementedProtocols(ObjJImplementationDeclaration declaration) {
        ObjJInheritedProtocolList protocolListElement = declaration.getInheritedProtocolList();
        if (protocolListElement == null) {
            return;
        }
        final Project project = declaration.getProject();
        List<ObjJClassName> protocols = protocolListElement.getClassNameList();
        for (ObjJClassName className : protocols) {
            ObjJProtocolDeclaration protocolDeclaration;
            for (ObjJClassDeclarationElement classDeclarationElement : ObjJClassDeclarationsIndex.getInstance().get(className.getText(), project)) {
                if (!(classDeclarationElement instanceof ObjJProtocolDeclaration)) {
                    continue;
                }
                protocolDeclaration = ((ObjJProtocolDeclaration)classDeclarationElement);
                List<ObjJProtocolScopedBlock> scopedBlocks = protocolDeclaration.getProtocolScopedBlockList();
            }
        }
    }

    private static boolean validateAndAnnotateMethod(ObjJMethodHeader expected, ObjJMethodHeader actual, AnnotationHolder annotationHolder) {
        if (!expected.getSelectorString().equals(actual.getSelectorString())) {
            return false;
        }
        List<ObjJFormalVariableType> actualParamTypes = actual.getParamTypes();
        List<ObjJFormalVariableType> expectedParamTypes = expected.getParamTypes();
        if (actualParamTypes.size() != expectedParamTypes.size()) {
            annotationHolder.createErrorAnnotation(actual, "Mismatched expected parameter types, and actual parameter types");
        }
        ObjJFormalVariableType actualParamType;
        ObjJFormalVariableType expectedParamType;
        for (int i=0;i<actualParamTypes.size();i++) {
            expectedParamType = expectedParamTypes.size() > i ? expectedParamTypes.get(i) : null;
            actualParamType = actualParamTypes.get(i);
            if (expectedParamType == null || actualParamType == null) {
                continue;
            }
            if (expectedParamType.getVarTypeId() != null) {
                if ( actualParamType.getVarTypeId() != null) {
                    continue;
                }
            }

            if (expectedParamType.getText().equals(actualParamType.getText())) {
                annotationHolder.createErrorAnnotation(actualParamType, "Method in protocol expected parameter type <"+expectedParamType.getText()+">, but found type <"+actualParamType.getText()+">");
            }
        }
        if (!expected.getReturnType().equals(actual.getReturnType())) {
            annotationHolder.createErrorAnnotation(actual.getMethodHeaderReturnTypeElement() != null ? actual.getMethodHeaderReturnTypeElement() : actual, "Unexpected return type from delegate method. Expected return type <"+expected.getReturnType()+">");
        }
        return true;
    }

}
