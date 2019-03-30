package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import java.util.logging.Level
import java.util.logging.Logger

object ObjJVariableNameResolveUtil {

    val LOGGER:Logger by lazy {
        Logger.getLogger(ObjJVariableNameResolveUtil::class.java.canonicalName)
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
            return ObjJQualifiedReferenceUtil.resolveQualifiedReferenceVariable(variableNameElement) ?: variableNameElement
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
        return ObjJVariableNameAggregatorUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) { possibleFirstVar -> !possibleFirstVar.isEquivalentTo(variableNameElement) && isPrecedingVar(variableNameElement, possibleFirstVar) }// ?: variableNameElement

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
        val elementStartOffset = variableNameElement.textRange.startOffset
        return ObjJVariableNameAggregatorUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0) { possibleFirstDeclaration ->
            !possibleFirstDeclaration.isEquivalentTo(variableNameElement) &&
            variableNameString == possibleFirstDeclaration.text &&
                    (!variableNameElement.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || elementStartOffset > possibleFirstDeclaration.textRange.startOffset)
        }

    }


    private fun getClassNameIfVariableNameIsStaticReference(variableNameElement: ObjJVariableName): ObjJClassName? {

        val variableNameText = variableNameElement.text
        var classNameElement: ObjJClassName? = null
        if (variableNameText == "self") {
            classNameElement = ObjJPsiImplUtil.getContainingClass(variableNameElement)?.getClassName() ?: return null
        }
        if (variableNameText == "super") {
            classNameElement = getContainingSuperClass(variableNameElement, true)
                    ?: ObjJPsiImplUtil.getContainingClass(variableNameElement)?.getClassName() ?: return null
        }

        if (classNameElement == null) {
            return null
        }
        val classNameString = classNameElement.text ?: return null
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

    fun getMatchingPrecedingVariableNameElements(variableName: ObjJCompositeElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val variableNameQualifiedString: String = if (variableName is ObjJVariableName) {
            ObjJQualifiedReferenceUtil.getQualifiedNameAsString(variableName, qualifiedIndex)
        } else {
            variableName.text
        }

        val hasContainingClass = ObjJHasContainingClassPsiUtil.getContainingClass(variableName) != null
        return ObjJVariableNameAggregatorUtil.getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex) { thisVariable ->
            isMatchingElement(variableNameQualifiedString, thisVariable, hasContainingClass, startOffset, qualifiedIndex)
        }
    }

    private fun isMatchingElement(variableNameQualifiedString: String, variableToCheck: ObjJVariableName?, hasContainingClass: Boolean, startOffset: Int, qualifiedIndex: Int): Boolean {
        if (variableToCheck == null) {
            LOGGER.log(Level.SEVERE, "Variable name to check should not be null")
            return false
        }
        val thisVariablesFqName = ObjJQualifiedReferenceUtil.getQualifiedNameAsString(variableToCheck, qualifiedIndex)
        if (variableNameQualifiedString != thisVariablesFqName) {
            return false
        }
        if (variableToCheck.containingClass == null) {
            if (hasContainingClass) {
                return true
            }
        }
        return variableToCheck.textRange.startOffset < startOffset
    }



    private fun isPrecedingVar(baseVar: ObjJVariableName, possibleFirstDeclaration: ObjJVariableName): Boolean {
        // Variable is a proceeding variable if it is not in same file(globals),
        // Or if it is declared before other in same file.
        return baseVar.text == possibleFirstDeclaration.text && (!baseVar.containingFile.isEquivalentTo(possibleFirstDeclaration.containingFile) || baseVar.textRange.startOffset > possibleFirstDeclaration.textRange.startOffset) && baseVar.indexInQualifiedReference == possibleFirstDeclaration.indexInQualifiedReference
    }
}
