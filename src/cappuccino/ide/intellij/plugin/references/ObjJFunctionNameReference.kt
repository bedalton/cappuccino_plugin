package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

import java.util.logging.Logger

class ObjJFunctionNameReference(functionName: ObjJFunctionName) : PsiReferenceBase<ObjJFunctionName>(functionName, TextRange.create(0, functionName.textLength)) {
    private val functionName: String = functionName.text

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element.text != functionName) {
            return false
        }
        if(!(element is ObjJVariableName || element is ObjJFunctionName)) {
            return false
        }
        if (ObjJPsiImplUtil.getIndexInQualifiedReference(element) != 0) {
            return false
        }
        val resolvedElement = ObjJFunctionDeclarationPsiUtil.resolveElementToFunctionDeclarationReference(myElement)
                ?: return false
        return resolvedElement.isEquivalentTo(element)
    }

    override fun resolve(): PsiElement? {
        return ObjJFunctionDeclarationPsiUtil.resolveElementToFunctionDeclarationReference(myElement)
    }

    override fun handleElementRename(newFunctionName: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, newFunctionName)
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    companion object {
        @Suppress("unused")
        private val LOGGER = Logger.getLogger(ObjJFunctionNameReference::class.java.name)
    }

}
