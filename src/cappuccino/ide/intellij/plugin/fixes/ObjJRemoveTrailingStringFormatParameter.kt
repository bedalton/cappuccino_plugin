package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.isType
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import org.jetbrains.annotations.Nls

/**
 * Used when a string with format uses more string elements than params in format string.
 */
class ObjJRemoveTrailingStringFormatParameter(element: PsiElement) : LocalQuickFixOnPsiElement(element) {

    override fun getText(): String {
        return ObjJBundle.message("objective-j.intentions.remove-trailing-string-format-parameter.prompt")
    }

    override fun invoke(
            project: Project,
            psiFile: PsiFile,
            element: PsiElement,
            endElement: PsiElement) {
        Logger.getInstance(ObjJRemoveTrailingStringFormatParameter::class.java).assertTrue(element == endElement)
        var previousSibling:PsiElement? = element.prevSibling
        while(previousSibling != null && (previousSibling.isType(TokenType.WHITE_SPACE) || previousSibling.isType(ObjJTypes.ObjJ_COMMA))) {
            previousSibling.parent.node.removeChild(previousSibling.node)
            previousSibling = element.prevSibling
        }
        element.parent.node.removeChild(element.node)
    }

    @Nls
    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }
}
