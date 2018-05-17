package cappuccino.ide.intellij.plugin.contributor

import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

class ObjJFindUsagesProvider : FindUsagesProvider {


    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
                ObjJLexer(),
                TokenSet.create(ObjJTypes.ObjJ_SELECTOR, ObjJTypes.ObjJ_VARIABLE_NAME, ObjJTypes.ObjJ_CLASS_NAME, ObjJTypes.ObjJ_FUNCTION_NAME),
                TokenSet.create(ObjJTypes.ObjJ_SINGLE_LINE_COMMENT, ObjJTypes.ObjJ_BLOCK_COMMENT),
                TokenSet.create(ObjJTypes.ObjJ_INTEGER, ObjJTypes.ObjJ_STRING_LITERAL, ObjJTypes.ObjJ_DECIMAL_LITERAL, ObjJTypes.ObjJ_BOOLEAN_LITERAL)
        )
    }

    override fun canFindUsagesFor(
            psiElement: PsiElement): Boolean {
        return psiElement is ObjJSelector ||
                psiElement is ObjJVariableName ||
                psiElement is ObjJClassName ||
                psiElement is ObjJFunctionName
    }

    override fun getHelpId(
            psiElement: PsiElement): String? {
        return HelpID.FIND_OTHER_USAGES
    }

    override fun getType(
            psiElement: PsiElement): String {
        if (psiElement is ObjJSelector) {
            return "method Selector"
        } else if (psiElement is ObjJVariableName) {
            return "variable"
        } else if (psiElement is ObjJClassName) {
            return "class"
        } else if (psiElement is ObjJFunctionName) {
            return "function"
        }
        return ""
    }

    override fun getDescriptiveName(
            psiElement: PsiElement): String {
        val containingClassOrFileName = ObjJHasContainingClassPsiUtil.getContainingClassOrFileName(psiElement)
        return ObjJPsiImplUtil.getDescriptiveText(psiElement) + " in " + containingClassOrFileName
    }

    override fun getNodeText(
            psiElement: PsiElement, b: Boolean): String {
        return ObjJPsiImplUtil.getDescriptiveText(psiElement) ?: psiElement.text
    }
}
