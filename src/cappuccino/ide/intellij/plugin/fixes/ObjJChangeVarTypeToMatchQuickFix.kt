package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.isType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import org.jetbrains.annotations.Nls

class ObjJChangeVariableTypeToMatchQuickFix(element: PsiElement, private val newType:String) : LocalQuickFixOnPsiElement(element) {
    override fun getText(): String {
        return ObjJBundle.message("objective-j.intentions.change-variable-type.prompt", newType)
    }

    override fun invoke(
            project: Project,
            psiFile: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement) {
        Logger.getInstance(ObjJChangeVariableTypeToMatchQuickFix::class.java).assertTrue(startElement == endElement)
        val variableType = ObjJElementFactory.createFormalVariableType(startElement.project, newType)
        when {
            startElement is ObjJFormalVariableType -> replaceWith(startElement, variableType)
            startElement.isType(ObjJTypes.ObjJ_VOID) -> replaceWith(startElement, variableType)
            startElement is ObjJMethodHeaderReturnTypeElement -> replaceWith(startElement.formalVariableType, variableType)
            startElement is ObjJMethodDeclarationSelector -> replaceInSelector(startElement, variableType)
            startElement.isType(ObjJTypes.ObjJ_OPEN_PAREN) -> addAfter(startElement, variableType)
            startElement.isType(ObjJTypes.ObjJ_CLOSE_PAREN) -> addBefore(startElement, variableType)
            startElement is ObjJMethodHeader -> addReturnToMethodHeader(startElement, variableType)
            else -> return
        }
        DaemonCodeAnalyzer.getInstance(startElement.project).restart(startElement.containingFile)
    }

    private fun replaceWith(psiElement: PsiElement, variableType: ObjJFormalVariableType) {
        psiElement.replace(variableType)
    }

    private fun addAfter(psiElement: PsiElement, variableType: ObjJFormalVariableType) {
        psiElement.parent.addAfter(variableType, psiElement)
    }

    private fun addBefore(psiElement: PsiElement, variableType: ObjJFormalVariableType) {
        psiElement.parent.addBefore(variableType, psiElement)
    }

    private fun addReturnToMethodHeader(psiElement: ObjJMethodHeader, variableType: ObjJFormalVariableType) {
        if (psiElement.methodHeaderReturnTypeElement != null) {
            replaceWith(psiElement.methodHeaderReturnTypeElement!!.formalVariableType, variableType)
            return
        }
        var firstChild:PsiElement? = psiElement.firstChild
        while (firstChild != null) {
            when {
                firstChild.isType(ObjJTypes.ObjJ_OPEN_PAREN) -> addAfter(firstChild, variableType)
                firstChild.isType(ObjJTypes.ObjJ_CLOSE_PAREN) -> addBefore(firstChild, variableType)
                firstChild is ObjJMethodScopeMarker || firstChild.isType(TokenType.WHITE_SPACE) -> firstChild = firstChild.nextSibling
                else -> return
            }
        }
    }

    private fun replaceInSelector(selector: ObjJMethodDeclarationSelector, variableType: ObjJFormalVariableType) {
        if (selector.variableType != null) {
            replaceWith(selector.variableType!!, variableType)
            return
        }
        val closeParen = selector.methodHeaderSelectorFormalVariableType?.closeParen
        if (closeParen != null) {
            addBefore(closeParen, variableType)
            return
        }
        val openParen = selector.methodHeaderSelectorFormalVariableType?.openParen
        if (openParen != null) {
            addAfter(openParen, variableType)
        }
    }

    @Nls
    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }
}
