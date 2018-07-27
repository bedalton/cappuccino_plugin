package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.isType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import org.jetbrains.annotations.Nls

class ObjJChangeVarTypeToMatchQuickFix(element: PsiElement, private val newType:String) : LocalQuickFixOnPsiElement(element) {
    override fun getText(): String {
        return "Change variable type to '$newType'?"
    }

    override fun invoke(
            project: Project,
            psiFile: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement) {
        Logger.getInstance(ObjJChangeVarTypeToMatchQuickFix::class.java).assertTrue(startElement == endElement)
        val varType = ObjJElementFactory.createFormalVariableType(startElement.project, newType)
        when {
            startElement is ObjJFormalVariableType -> replaceWith(startElement, varType)
            startElement.isType(ObjJTypes.ObjJ_VOID) -> replaceWith(startElement, varType)
            startElement is ObjJMethodHeaderReturnTypeElement -> replaceWith(startElement.formalVariableType, varType)
            startElement is ObjJMethodDeclarationSelector -> replaceInSelector(startElement, varType)
            startElement.isType(ObjJTypes.ObjJ_OPEN_PAREN) -> addAfter(startElement, varType)
            startElement.isType(ObjJTypes.ObjJ_CLOSE_PAREN) -> addBefore(startElement, varType)
            startElement is ObjJMethodHeader -> addReturnToMethodHeader(startElement, varType)
            else -> return
        }
        DaemonCodeAnalyzer.getInstance(startElement.project).restart(startElement.containingFile)
    }

    private fun replaceWith(psiElement: PsiElement, varType: ObjJFormalVariableType) {
        psiElement.replace(varType)
    }

    private fun addAfter(psiElement: PsiElement, varType: ObjJFormalVariableType) {
        psiElement.parent.addAfter(varType, psiElement)
    }

    private fun addBefore(psiElement: PsiElement, varType: ObjJFormalVariableType) {
        psiElement.parent.addBefore(varType, psiElement)
    }

    private fun addReturnToMethodHeader(psiElement: ObjJMethodHeader, varType: ObjJFormalVariableType) {
        if (psiElement.methodHeaderReturnTypeElement != null) {
            replaceWith(psiElement.methodHeaderReturnTypeElement!!.formalVariableType, varType)
            return
        }
        var firstChild:PsiElement? = psiElement.firstChild
        while (firstChild != null) {
            when {
                firstChild.isType(ObjJTypes.ObjJ_OPEN_PAREN) -> addAfter(firstChild, varType)
                firstChild.isType(ObjJTypes.ObjJ_CLOSE_PAREN) -> addBefore(firstChild, varType)
                firstChild is ObjJMethodScopeMarker || firstChild.isType(TokenType.WHITE_SPACE) -> firstChild = firstChild.nextSibling
                else -> return
            }
        }
    }

    private fun replaceInSelector(selector: ObjJMethodDeclarationSelector, varType: ObjJFormalVariableType) {
        if (selector.varType != null) {
            replaceWith(selector.varType!!, varType)
            return
        }
        val closeParen = selector.closeParen
        if (closeParen != null) {
            addBefore(closeParen, varType)
            return
        }
        val openParen = selector.openParen
        if (openParen != null) {
            addAfter(openParen, varType)
        }
    }

    @Nls
    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }
}
