package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil


fun PsiElement.getContainingScope() : ReferencedInScope =
    getScope(this)

fun PsiElement.getContainingScope(otherElement:PsiElement) : ReferencedInScope =
    findSharedReferenceScope(this, otherElement)

fun findSharedReferenceScope(elementOne:PsiElement, elementTwo: PsiElement): ReferencedInScope {
    val commonContext = PsiTreeUtil.findCommonContext(elementTwo, elementOne)
    val scope:ReferencedInScope = getScope(commonContext)
    if (scope != ReferencedInScope.UNDETERMINED) {
        return scope
    }
    if (elementOne is ObjJVariableName && elementTwo is ObjJVariableName) {
        if (ObjJVariableNameUtil.variablesShareInstanceVariableScope(elementOne, elementTwo)) {
            return ReferencedInScope.CLASS
        }
    }
    return ReferencedInScope.UNDETERMINED
}

fun getScope(commonContext: PsiElement?): ReferencedInScope {
    if (commonContext == null) {
        return ReferencedInScope.UNDETERMINED
    }

    /*
    val bodyVariableAssignment:ObjJBodyVariableAssignment? =
            commonContext as? ObjJBodyVariableAssignment ?: commonContext.getParentOfType(ObjJBodyVariableAssignment::class.java)
    if (bodyVariableAssignment != null) {
        if (bodyVariableAssignment.varModifier == null) {
            if (bodyVariableAssignment.parent is IfStatement) {
                return ReferencedInScope.IF
            }

            if (bodyVariableAssignment.parent is ObjJIterationStatement) {
                return ReferencedInScope.ITERATION_HEADER
            }
        }
    }
     */
    var element:PsiElement? = commonContext
    var scope:ReferencedInScope? = null
    while (scope == null && element != null) {
        scope = getScopeStrict(element)
        element = element.parent
    }

    // If a common context exists, it should be the file level at least
    return scope ?: ReferencedInScope.FILE
}

private fun getScopeStrict(commonContext:PsiElement) : ReferencedInScope? {

    if (hasSharedContextOfTypeStrict(commonContext, ObjJIfStatement::class.java)) {
        return ReferencedInScope.IF
    }

    if (hasSharedContextOfTypeStrict(commonContext, ObjJIterationStatement::class.java)) {
        return ReferencedInScope.ITERATION_HEADER
    }

    if (hasSharedContextOfTypeStrict(commonContext, ObjJTryStatement::class.java)) {
        return ReferencedInScope.TRY_CATCH
    }

    if (hasSharedContextOfTypeStrict(commonContext, ObjJPreprocessorDefineFunction::class.java)) {
        return ReferencedInScope.PREPROCESSOR_FUNCTION
    }

    if (hasSharedContextOfTypeStrict(commonContext, ObjJFunctionDeclarationElement::class.java))  {
        return ReferencedInScope.FUNCTION
    }

    if (hasSharedContextOfTypeStrict(commonContext, ObjJMethodDeclaration::class.java)) {
        return ReferencedInScope.METHOD
    }

    if (commonContext is ObjJVariableName && ObjJVariableNameUtil.isInstanceVarDeclaredInClassOrInheritance(commonContext)) {
        return ReferencedInScope.CLASS
    }
    return null;
}

private fun <PsiT : PsiElement> PsiElement.hasSharedContextOfType(other: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
    val commonContext = if (other != null) PsiTreeUtil.findCommonContext(this, other) else return false
    return sharedContextClass.isInstance(commonContext) || commonContext.getParentOfType( sharedContextClass) != null
}

private fun <PsiT : PsiElement> hasSharedContextOfTypeStrict(commonContext: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
    return sharedContextClass.isInstance(commonContext)
}

private fun <PsiT : PsiElement> hasSharedContextOfType(commonContext: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
    return sharedContextClass.isInstance(commonContext) || commonContext.getParentOfType(sharedContextClass) != null
}

enum class ReferencedInScope  (val scopeClass : Class<*>?) {
    UNDETERMINED (null),
    CLASS(ObjJClassDeclarationElement::class.java),
    FILE(PsiFile::class.java),
    FUNCTION(ObjJFunctionDeclarationElement::class.java),
    IF(ObjJIfStatement::class.java),
    ITERATION_HEADER(ObjJIterationStatement::class.java),
    METHOD(ObjJMethodHeaderDeclaration::class.java),
    PREPROCESSOR_FUNCTION(ObjJPreprocessorDefineFunction::class.java),
    TRY_CATCH(ObjJTryStatement::class.java)


}