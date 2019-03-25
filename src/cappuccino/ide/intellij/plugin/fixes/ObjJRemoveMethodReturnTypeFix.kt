package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Changes return type to void if no part of the method returns a value
 */
class ObjJRemoveMethodReturnTypeFix(element:PsiElement) : LocalQuickFixOnPsiElement(element) {
    override fun getFamilyName(): String = ObjJInspectionProvider.GROUP_DISPLAY_NAME

    override fun getText(): String = ObjJBundle.message("objective-j.intentions.remove-return-type.text")

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        Logger.getInstance(ObjJRemoveMethodReturnTypeFix::class.java).assertTrue(startElement == endElement)
        val methodDec = startElement as? ObjJMethodDeclaration ?: startElement.getParentOfType(ObjJMethodDeclaration::class.java) ?: return
        val formalVariableType = methodDec.methodHeader.methodHeaderReturnTypeElement?.formalVariableType ?: return
        val voidFormalVariableType = ObjJElementFactory.createFormalVariableType(startElement.project, ObjJClassType.VOID_CLASS_NAME)
        formalVariableType.replace(voidFormalVariableType)
    }

}