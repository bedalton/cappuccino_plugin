package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.jstypedef.lexer.JsTypeDefLexer
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet.create

class JsTypeDefFindUsagesProvider : FindUsagesProvider {


    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
                JsTypeDefLexer(),
                create(
                        JS_PROPERTY_NAME,
                        JS_TYPE_NAME,
                        JS_FUNCTION_NAME,
                        JS_MODULE_NAME,
                        JS_TYPE_MAP_NAME,
                        JS_GENERICS_KEY
                ),
                create(JS_SINGLE_LINE_COMMENT, JS_BLOCK_COMMENT),
                create(JS_INTEGER_LITERAL, JS_STRING_LITERAL)
        )
    }

    override fun canFindUsagesFor(
            psiElement: PsiElement): Boolean {
        return psiElement is JsTypeDefFunctionName ||
                psiElement is JsTypeDefPropertyName ||
                psiElement is JsTypeDefTypeName ||
                psiElement is JsTypeDefModuleName ||
                psiElement is JsTypeDefTypeMapName
    }

    override fun getHelpId(
            psiElement: PsiElement): String? {
        return HelpID.FIND_OTHER_USAGES
    }

    override fun getType(
            psiElement: PsiElement): String {
        return when (psiElement) {
            is JsTypeDefFunctionName -> "function"
            is JsTypeDefPropertyName -> "property"
            is JsTypeDefTypeMapName -> "typemap"
            is JsTypeDefModuleName -> "module"
            is JsTypeDefTypeName -> {
                when {
                    psiElement.parent is JsTypeDefInterfaceElement -> "interface"
                    else -> "class"
                }
            }
            else -> ""
        }
    }

    override fun getDescriptiveName(
            psiElement: PsiElement): String {
        val element = psiElement as? JsTypeDefElement ?: return ""
        return JsTypeDefPsiImplUtil.getDescriptiveText(element) + " in " + psiElement
    }

    override fun getNodeText(
            psiElement: PsiElement, b: Boolean): String {
        val typeDefElement = psiElement as? JsTypeDefElement ?: return psiElement.text
        return JsTypeDefPsiImplUtil.getDescriptiveText(typeDefElement)
    }
}
