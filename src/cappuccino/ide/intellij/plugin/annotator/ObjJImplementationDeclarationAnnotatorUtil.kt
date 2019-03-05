package cappuccino.ide.intellij.plugin.annotator


import com.intellij.lang.annotation.AnnotationHolder
import cappuccino.ide.intellij.plugin.fixes.ObjJMissingProtocolMethodFix
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller

internal object ObjJImplementationDeclarationAnnotatorUtil {


    fun annotateImplementationDeclaration(declaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        if (declaration.isCategory()) {
            annotateIfUndefinedImplementationForCategory(declaration.getClassName(), annotationHolder)
        } else {
            //annotateIfDuplicateImplementation(declaration, annotationHolder)
        }
        annotateUnimplementedProtocols(declaration, annotationHolder)
    }

    private fun annotateIfUndefinedImplementationForCategory(classNameElement: ObjJClassName?, annotationHolder: AnnotationHolder) {
        if (classNameElement == null) {
            return
        }
        val className = classNameElement.text
        if (className.isEmpty() || isUniversalMethodCaller(className)) {
            return
        }
        for (implementationDeclaration in ObjJImplementationDeclarationsIndex.instance.get(className, classNameElement.project)) {
            if (!implementationDeclaration.isCategory()) {
                return
            }
        }
        annotationHolder.createErrorAnnotation(classNameElement, "Category references undefined implementation: <$className>")
    }

    private fun annotateIfDuplicateImplementation(thisImplementationDeclaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        val classNameElement = thisImplementationDeclaration.getClassName() ?: return
        val className = classNameElement.text
        for (implementationDeclaration in ObjJImplementationDeclarationsIndex.instance.get(className, classNameElement.project)) {
            if (!implementationDeclaration.isCategory() && !implementationDeclaration.isEquivalentTo(thisImplementationDeclaration)) {
                annotationHolder.createErrorAnnotation(classNameElement, "Duplicate class declared with name: <$className>")
                return
            }
        }
    }

    private fun annotateUnimplementedProtocols(declaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        val protocolListElement = declaration.inheritedProtocolList ?: return
        val protocols = protocolListElement.classNameList
        for (className in protocols) {
            val protocolName = className.text
            if (ObjJProtocolDeclarationsIndex.instance.get(protocolName, declaration.project).isEmpty()) {
                annotationHolder.createErrorAnnotation(className, "Protocol with name <$protocolName> does not exist in project")
            }
            val unimplementedMethods = declaration.getUnimplementedProtocolMethods(protocolName)
            if (!unimplementedMethods.required.isEmpty()) {
                annotationHolder.createErrorAnnotation(className, "Missing required protocol methods")
                        .registerFix(ObjJMissingProtocolMethodFix(declaration, protocolName, unimplementedMethods))
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
