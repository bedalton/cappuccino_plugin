package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import java.util.logging.Logger

object ObjJVariableNameResolveUtil {

    private val LOGGER by lazy {
        Logger.getLogger(ObjJVariableNameResolveUtil::class.java.name)
    }

    fun getVariableDeclarationElement(variableNameElement: ObjJVariableName): PsiElement? {
        val variableNameString = variableNameElement.text

        if (variableNameString == "class") {
            return null
        }

        if (variableNameString == "this") {
            return null
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
        return ObjJVariableNameUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) { possibleFirstVar -> !possibleFirstVar.isEquivalentTo(variableNameElement) && isPrecedingVar(variableNameElement, possibleFirstVar) }// ?: variableNameElement

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
        return ObjJVariableNameUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) { possibleFirstDeclaration ->
            !possibleFirstDeclaration.isEquivalentTo(variableNameElement)
            variableNameElement.text == possibleFirstDeclaration.text &&
                    (!variableNameElement.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || variableNameElement.textRange.startOffset > possibleFirstDeclaration.textRange.startOffset) &&
                    variableNameElement.indexInQualifiedReference == 0
        }// ?: variableNameElement

    }


    private fun getClassNameIfVariableNameIsStaticReference(variableNameElement: ObjJVariableName): ObjJClassName? {

        val variableNameText = variableNameElement.text
        var classNameElement: ObjJClassName? = null
        if (variableNameText == "self") {
            //LOGGER.info("Var name is Self")
            classNameElement = ObjJPsiImplUtil.getContainingClass(variableNameElement)?.getClassName() ?: return null
        }
        if (variableNameText == "super") {
            //LOGGER.info("Is Super")
            classNameElement = getContainingSuperClass(variableNameElement, true)
                    ?: ObjJPsiImplUtil.getContainingClass(variableNameElement)?.getClassName() ?: return null
        }

        if (classNameElement == null) {
            //LOGGER.warning("Class element for static variable name is null")
            return null
        }
        val classNameString = classNameElement.text ?: return null
        //LOGGER.info("Class Name is $classNameString")
        /*
                Tries to find the most relevant class reference,
                if variable name element is part of a method call
             */
        if (!DumbService.isDumb(variableNameElement.project)) {
            val methodCall = variableNameElement.parent?.parent?.parent as? ObjJMethodCall ?: return classNameElement
            val selector = methodCall.selectorString
            return getContainingClassWithSelector(classNameString, selector, classNameElement)
        }
        return null
    }


    private fun isPrecedingVar(baseVar: ObjJVariableName, possibleFirstDeclaration: ObjJVariableName): Boolean {
        // Variable is a proceeding variable if it is not in same file(globals),
        // Or if it is declared before other in same file.
        return baseVar.text == possibleFirstDeclaration.text && (!baseVar.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || baseVar.textRange.startOffset > possibleFirstDeclaration.textRange.startOffset) && baseVar.indexInQualifiedReference == possibleFirstDeclaration.indexInQualifiedReference
    }
}
