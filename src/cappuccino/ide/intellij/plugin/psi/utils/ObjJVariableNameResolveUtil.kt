package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import java.util.logging.Level
import java.util.logging.Logger

object ObjJVariableNameResolveUtil {

    //private val LOGGER = Logger.getLogger(ObjJVariableNameResolveUtil::class.java.name)

    fun getVariableDeclarationElement(variableNameElement: ObjJVariableName): PsiElement? {
        val variableNameString = variableNameElement.text

        if (variableNameString == "class") {
            return null
        }

        if (variableNameString == "this") {
            return null
        }

        if (variableNameString == "self") {
            return variableNameElement.containingClass?.getClassName()
        }
        if (variableNameString == "super") {
            return variableNameElement.getContainingSuperClass()
        }

        if (variableNameElement.indexInQualifiedReference != 0) {
            return ObjJVariableNameUtil.resolveQualifiedReferenceVariable(variableNameElement) ?: variableNameElement
        }

        val className = getClassNameIfVariableNameIsStaticReference(variableNameElement)
        if (className != null) {
            return className
        }

        if (variableNameElement.parent is ObjJPropertyAssignment) {
            return variableNameElement
        }
        if (variableNameElement.getParentOfType(ObjJBodyVariableAssignment::class.java)?.varModifier != null &&
                variableNameElement.getParentOfType(ObjJExpr::class.java) == null
        ) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJMethodHeaderDeclaration::class.java)) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJFormalParameterArg::class.java)) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJInstanceVariableList::class.java)) {
            return variableNameElement
        }
        return ObjJVariableNameUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) { possibleFirstVar -> isPrecedingVar(variableNameElement, possibleFirstVar) }// ?: variableNameElement

    }

    fun getVariableDeclarationElementForFunctionName(variableNameElement: ObjJFunctionName): PsiElement? {
        val variableNameString = variableNameElement.text

        if (variableNameString == "class") {
            return null
        }

        if (variableNameString == "this") {
            return null
        }

        if (variableNameElement.indexInQualifiedReference > 0) {
            return null
        }

        if (variableNameElement.parent is ObjJPropertyAssignment) {
            return variableNameElement
        }
        if (variableNameElement.getParentOfType(ObjJBodyVariableAssignment::class.java)?.varModifier != null &&
                variableNameElement.getParentOfType(ObjJExpr::class.java) == null
        ) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJMethodHeaderDeclaration::class.java)) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJFormalParameterArg::class.java)) {
            return variableNameElement
        }
        if (variableNameElement.hasParentOfType(ObjJInstanceVariableList::class.java)) {
            return variableNameElement
        }
        return ObjJVariableNameUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) {
            possibleFirstDeclaration ->
            variableNameElement.text == possibleFirstDeclaration.text &&
                    (!variableNameElement.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || variableNameElement.textRange.startOffset > possibleFirstDeclaration.textRange.startOffset) &&
                    variableNameElement.indexInQualifiedReference == 0
        }// ?: variableNameElement

    }


    private fun getClassNameIfVariableNameIsStaticReference(variableNameElement: ObjJVariableName): ObjJClassName? {
        var classNameElement: ObjJClassName? = null
        var className = variableNameElement.text
        val containingClass = ObjJPsiImplUtil.getContainingClass(variableNameElement)
        if (className == "self") {
            //LOGGER.log(Level.INFO, "Var name matches 'self'.");
            if (containingClass != null) {
                //LOGGER.log(Level.INFO, "Var name 'self' resolves to <" + variableNameElement.getText() + ">");
                return containingClass.getClassName()
            }
        }
        if (variableNameElement.text == "super") {
            classNameElement = variableNameElement.getContainingSuperClass(true)
            if (classNameElement == null && containingClass != null) {
                classNameElement = containingClass.getClassName()
            }
        }
        if (classNameElement != null) {
            className = classNameElement.text
        }

        /*
            Tries to find the most relevant class reference,
            if variable name element is part of a method call
         */
        if (!DumbService.isDumb(variableNameElement.project)) {
            val classDeclarationElements = ObjJClassDeclarationsIndex.instance[className, variableNameElement.project]
            if (!classDeclarationElements.isEmpty()) {
                val methodCall = variableNameElement.getParentOfType(ObjJMethodCall::class.java)
                val methodCallSelectorString = methodCall?.selectorString
                for (classDeclarationElement in classDeclarationElements) {
                    if (methodCallSelectorString != null) {
                        if (classDeclarationElement.hasMethod(methodCallSelectorString)) {
                            return classDeclarationElement.getClassName()
                        }
                    } else if (classDeclarationElement is ObjJImplementationDeclaration) {
                        if (!classDeclarationElement.isCategory) {
                            return classDeclarationElement.getClassName()
                        }
                    } else {
                        return classDeclarationElement.getClassName()
                    }
                }
            }
        }
        return null
    }


    private fun isPrecedingVar(baseVar: ObjJVariableName, possibleFirstDeclaration: ObjJVariableName): Boolean {
        // Variable is a proceeding variable if it is not in same file(globals),
        // Or if it is declared before other in same file.
        return baseVar.text == possibleFirstDeclaration.text && (!baseVar.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || baseVar.textRange.startOffset > possibleFirstDeclaration.textRange.startOffset) && baseVar.indexInQualifiedReference == possibleFirstDeclaration.indexInQualifiedReference
    }
}
