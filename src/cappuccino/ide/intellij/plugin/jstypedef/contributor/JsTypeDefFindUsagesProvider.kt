package cappuccino.ide.intellij.plugin.jstypedef.contributor

import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import cappuccino.ide.intellij.plugin.jstypedef.lexer.JsTypeDefLexer
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsFunctionName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsPropertyName
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeName
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

class JsTypeDefFindUsagesProvider : FindUsagesProvider {


    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
                JsTypeDefLexer(),
                TokenSet.create(JsTypeDefTypes.JS_PROPERTY_NAME, JsTypeDefTypes.JS_TYPE_NAME, JsTypeDefTypes.JS_FUNCTION_NAME),
                TokenSet.create(JsTypeDefTypes.JS_SINGLE_LINE_COMMENT, JsTypeDefTypes.JS_BLOCK_COMMENT),
                TokenSet.create(JsTypeDefTypes.JS_INTEGER_LITERAL, JsTypeDefTypes.JS_STRING_LITERAL)
        )
    }

    override fun canFindUsagesFor(
            psiElement: PsiElement): Boolean {
        return psiElement is JsFunctionName ||
                psiElement is JsPropertyName ||
                psiElement is JsTypeName
    }

    override fun getHelpId(
            psiElement: PsiElement): String? {
        return HelpID.FIND_OTHER_USAGES
    }

    override fun getType(
            psiElement: PsiElement): String {
        return when (psiElement) {
            is JsFunctionName -> "function"
            is JsPropertyName -> "property"
            is JsTypeName -> "class"
            else -> ""
        }
    }

    override fun getDescriptiveName(
            psiElement: PsiElement): String {
        val element = psiElement as? JsTypeDefElement ?: return ""
        return JsTypeDefPsiImplUtil.getDescriptiveText(psiElement) + " in " + psiElement
    }

    override fun getNodeText(
            psiElement: PsiElement, b: Boolean): String {
        return ObjJPsiImplUtil.getDescriptiveText(psiElement) ?: psiElement.text
    }
}
