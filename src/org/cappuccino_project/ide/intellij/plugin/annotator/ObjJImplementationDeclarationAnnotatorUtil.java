package org.cappuccino_project.ide.intellij.plugin.annotator;


import com.intellij.lang.annotation.AnnotationHolder;
import org.cappuccino_project.ide.intellij.plugin.fixes.ObjJMissingProtocolMethodFix;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJClassDeclarationPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class ObjJImplementationDeclarationAnnotatorUtil {


    static void annotateImplementationDeclaration(ObjJImplementationDeclaration declaration, @NotNull AnnotationHolder annotationHolder) {
        if (declaration.isCategory()) {
            annotateIfUndefinedImplementationForCategory(declaration.getClassName(), annotationHolder);
        } else {
            annotateIfDuplicateImplementation(declaration, annotationHolder);
        }
        annotateUnimplementedProtocols(declaration, annotationHolder);
    }

    private static void annotateIfUndefinedImplementationForCategory(@Nullable ObjJClassName classNameElement, @NotNull AnnotationHolder annotationHolder) {
        if (classNameElement == null) {
            return;
        }
        final String className = classNameElement.getText();
        if (className .isEmpty() || ObjJMethodCallPsiUtil.isUniversalMethodCaller(className)) {
            return;
        }
        for (ObjJImplementationDeclaration implementationDeclaration:ObjJImplementationDeclarationsIndex.getInstance().get(className, classNameElement.getProject())) {
            if (!implementationDeclaration.isCategory()) {
                return;
            }
        }
        annotationHolder.createErrorAnnotation(classNameElement, "Category references undefined implementation: <"+className+">");
    }

    private static void annotateIfDuplicateImplementation(@NotNull ObjJImplementationDeclaration thisImplementationDeclaration, @NotNull AnnotationHolder annotationHolder) {
        ObjJClassName classNameElement = thisImplementationDeclaration.getClassName();
        if (classNameElement == null) {
            return;
        }
        final String className = classNameElement.getText();
        for (ObjJImplementationDeclaration implementationDeclaration:ObjJImplementationDeclarationsIndex.getInstance().get(className, classNameElement.getProject())) {
            if (!implementationDeclaration.isCategory() && !implementationDeclaration.isEquivalentTo(thisImplementationDeclaration)) {
                annotationHolder.createErrorAnnotation(classNameElement, "Duplicate class declared with name: <"+className+">");
                return;
            }
        }
    }

    private static void annotateUnimplementedProtocols(ObjJImplementationDeclaration declaration, @NotNull
            AnnotationHolder annotationHolder) {
        ObjJInheritedProtocolList protocolListElement = declaration.getInheritedProtocolList();
        if (protocolListElement == null) {
            return;
        }
        List<ObjJClassName> protocols = protocolListElement.getClassNameList();
        for (ObjJClassName className : protocols) {
            final String protocolName = className.getText();
            if (ObjJProtocolDeclarationsIndex.getInstance().get(protocolName, declaration.getProject()).isEmpty()) {
                annotationHolder.createErrorAnnotation(className, "Protocol with name <"+protocolName+"> does not exist in project");
            }
            ObjJProtocolDeclarationPsiUtil.ProtocolMethods unimplementedMethods = ObjJClassDeclarationPsiUtil.getUnimplementedProtocolMethods(declaration, protocolName);
            if (!unimplementedMethods.required.isEmpty()) {
                annotationHolder.createErrorAnnotation(className, "Missing required protocol methods")
                        .registerFix(new ObjJMissingProtocolMethodFix(unimplementedMethods));
            }
        }
    }

    /*
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
*/

}
