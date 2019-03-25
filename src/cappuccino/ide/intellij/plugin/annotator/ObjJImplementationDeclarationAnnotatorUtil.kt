package cappuccino.ide.intellij.plugin.annotator


import com.intellij.lang.annotation.AnnotationHolder
import cappuccino.ide.intellij.plugin.fixes.ObjJMissingProtocolMethodFix
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJTypeDefIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJClassTypePsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbService

/**
 * Implementation annotator
 * Used to annotate invalid implementation declarations
 */
internal object ObjJImplementationDeclarationAnnotatorUtil {


    /**
     * Annotation entry point
     */
    fun annotateImplementationDeclaration(declaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        if (declaration.isCategory) {
            annotateIfUndefinedImplementationForCategory(declaration.getClassName(), annotationHolder)
        }/*else {
            // Annotation for duplicate implementations was removed due to conflicts in test files
            annotateIfDuplicateImplementation(declaration, annotationHolder)
        }*/
        annotateUnimplementedProtocols(declaration, annotationHolder)
        annotateInvalidClassNamesInInstanceVariables(declaration, annotationHolder)
    }

    /**
     * Annotate categories, if we cannot find a definition for their base implementation
     */
    private fun annotateIfUndefinedImplementationForCategory(classNameElement: ObjJClassName?, annotationHolder: AnnotationHolder) {

        // Check that className element is not null
        if (classNameElement == null) {
            return
        }

        // Ensure that class name can be resolved
        val className = classNameElement.text
        if (className.isEmpty() || isUniversalMethodCaller(className)) {
            return
        }

        // Ensure that a non-category implementation exists for class with name
        for (implementationDeclaration in ObjJImplementationDeclarationsIndex.instance[className, classNameElement.project]) {
            if (!implementationDeclaration.isCategory) {
                return
            }
        }
        annotationHolder.createErrorAnnotation(classNameElement,  ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.invalidClassForCategory", className))
    }


    /*
    @todo make smarter to work with duplicates in test files
    @todo check for duplicates only on import, to ensure no clash is made.
    @removed due to conflicts in library with test classes having the same name
    private fun annotateIfDuplicateImplementation(thisImplementationDeclaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        val classNameElement = thisImplementationDeclaration.getClassName() ?: return
        val className = classNameElement.text
        for (implementationDeclaration in ObjJImplementationDeclarationsIndex.instance.get(className, classNameElement.project)) {
            if (!implementationDeclaration.isCategory() && !implementationDeclaration.isEquivalentTo(thisImplementationDeclaration)) {
                annotationHolder.createErrorAnnotation(classNameElement, "Duplicate class declared with name: <$className>")
                return
            }
        }
    }*/

    /**
     * Annotates protocol problems
     */
    private fun annotateUnimplementedProtocols(declaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        val protocolListElement = declaration.inheritedProtocolList ?: return
        val protocols = protocolListElement.classNameList
        for (className in protocols) {
            annotateUndefinedProtocolName(declaration, className, annotationHolder)
            annotateUnimplementedProtocolMethods(declaration, className, annotationHolder)
        }
    }

    /**
     * Annotate undefined protocol names
     */
    private fun annotateUndefinedProtocolName(declaration: ObjJImplementationDeclaration, protocolNameElement: ObjJClassName, annotationHolder: AnnotationHolder) {
        val protocolName = protocolNameElement.text
        if (ObjJProtocolDeclarationsIndex.instance[protocolName, declaration.project].isEmpty()) {
            annotationHolder.createErrorAnnotation(protocolNameElement, ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.invalidProtocolName", protocolName))
        }
    }

    /**
     * Annotate unimplemented or partially implemented protocols
     */
    private fun annotateUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolNameElement: ObjJClassName, annotationHolder: AnnotationHolder) {
        // Get protocol name as string
        val protocolName = protocolNameElement.text
        // Find all unimplemented methods
        val unimplementedMethods = declaration.getUnimplementedProtocolMethods(protocolName)
        if (unimplementedMethods.required.isEmpty())
            return
        // Annotate and register fix for missing required members
        annotationHolder.createErrorAnnotation(protocolNameElement, ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.missingProtocolMethod"))
                .registerFix(ObjJMissingProtocolMethodFix(declaration, protocolName, unimplementedMethods))
    }

    private fun annotateInvalidClassNamesInInstanceVariables(declaration: ObjJImplementationDeclaration, annotationHolder: AnnotationHolder) {
        if (DumbService.isDumb(declaration.project)) {
            return
        }
        val variables = declaration.instanceVariableList?.instanceVariableDeclarationList ?: return
        for (variable:ObjJInstanceVariableDeclaration in variables) {
            annotateInstanceVariableIfClassNameInvalid(variable, annotationHolder)
        }
    }

    private fun annotateInstanceVariableIfClassNameInvalid(variable: ObjJInstanceVariableDeclaration, annotationHolder: AnnotationHolder) {
        val variableType = variable.formalVariableType
        val className:ObjJClassName
        className = if (variableType.varTypeId != null) {
            // No className present return as valid
            if (variableType.varTypeId?.className == null){
                return
            }
            variableType.varTypeId?.className ?: return
        } else {
            variableType.className ?: return
        }

        val isValidClass = ObjJClassTypePsiUtil.isValidClass(className) ?: true
        if (isValidClass)
            return

        val classNameString:String = className.text ?: return
        var severity = HighlightSeverity.ERROR
        var message = ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.instanceVarClassNF")
        if (classNameString.startsWith("CG")) {
            severity = HighlightSeverity.WARNING
            message = ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.instanceVarClassPNF")
        }
        annotationHolder.createAnnotation(severity, className.textRange, message.format(classNameString))
    }

}
