package cappuccino.ide.intellij.plugin.jstypedef.references

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModuleName
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class JsTypeDefModuleNameReference(element:JsTypeDefModuleName)  : PsiReferenceBase<JsTypeDefModuleName>(element, TextRange(0, element.text.length - 1)) {

    private val moduleName:String = myElement.text
    private val namespaceComponents:List<String> by lazy {
        JsTypeDefPsiImplUtil.getPrecedingNamespaceComponents(myElement)
    }
    private val namespaceString:String by lazy {
        namespaceComponents.joinToString(".")
    }

        override fun isReferenceTo(elementToCheck: PsiElement): Boolean {
            if (elementToCheck !is JsTypeDefModuleName)
                return false
            if (elementToCheck.text != moduleName)
                return false
            return namespaceString == elementToCheck.precedingNamespace
        }

        override fun resolve(): PsiElement? {

        }

        override fun getCanonicalText(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun handleElementRename(p0: String): PsiElement {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isSoft(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}