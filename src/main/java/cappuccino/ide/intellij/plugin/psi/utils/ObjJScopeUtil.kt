package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement
import com.intellij.psi.PsiElement


fun PsiElement.getContainingScope() : ReferencedInScope =
    getScope(this)


fun getScope(commonContext: PsiElement?): ReferencedInScope {
    if (commonContext == null) {
        return ReferencedInScope.UNDETERMINED
    }
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

    if (commonContext is ObjJVariableName && ObjJVariableNameAggregatorUtil.isInstanceVariableDeclaredInClassOrInheritance(commonContext)) {
        return ReferencedInScope.CLASS
    }
    return null
}

private fun <PsiT : PsiElement> hasSharedContextOfTypeStrict(commonContext: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
    return sharedContextClass.isInstance(commonContext)
}


enum class ReferencedInScope {
    UNDETERMINED,
    CLASS,
    FILE,
    FUNCTION,
    IF,
    ITERATION_HEADER,
    METHOD,
    PREPROCESSOR_FUNCTION,
    TRY_CATCH
}