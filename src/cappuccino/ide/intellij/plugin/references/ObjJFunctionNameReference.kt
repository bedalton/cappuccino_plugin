package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.orFalse

import java.util.logging.Logger

class ObjJFunctionNameReference(functionName: ObjJFunctionName) : PsiReferenceBase<ObjJFunctionName>(functionName, TextRange.create(0, functionName.textLength)) {
    private val functionName: String = functionName.text
    private val file: PsiFile = functionName.containingFile
    private val isFunctionCall:Boolean get () {
        return myElement.parent is ObjJFunctionCall
    }
    private val isFunctionDeclaration:Boolean get() {
        return myElement.parent is ObjJFunctionDeclaration
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element.text != functionName) {
            return false
        }

        if (myElement?.indexInQualifiedReference != 0) {
            return false;
        }

        val elementIsFunctionCall = element.parent is ObjJFunctionCall
        val elementIsFunctionDeclaration = !elementIsFunctionCall && element.parent is ObjJFunctionDeclaration
        if (isFunctionDeclaration && elementIsFunctionDeclaration) {
            return false
        }
        if (isFunctionCall && elementIsFunctionCall) {
            return false
        }
        if (resolve()?.isEquivalentTo(element).orFalse()) {
            return true
        }
        val resolved = ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement) ?: return false
        return resolved == element
    }

    override fun resolve() : PsiElement? {
        return myElement.resolveFromCache {
            resolveInternal()
        }
    }

    fun resolveInternal(): PsiElement? {
        if (DumbServiceImpl.isDumb(myElement.project)) {
            return null
        }

        if (myElement?.indexInQualifiedReference != 0) {
            return null
        }

        val localFunctions = element.getParentBlockChildrenOfType(ObjJFunctionDeclarationElement::class.java, true).toMutableList()
        localFunctions.addAll(element.containingFile.getChildrenOfType(ObjJFunctionDeclarationElement::class.java))

        val allOut = localFunctions.map { it.functionNameNode }.filter {
            it != null && it.text == functionName
        }.toMutableList()

        for (functionDeclaration in ObjJFunctionsIndex.instance[functionName, myElement.project]) {
            ProgressIndicatorProvider.checkCanceled()
            allOut.add(functionDeclaration.functionNameNode!!)
            if (functionDeclaration.containingFile.isEquivalentTo(file)) {
                return functionDeclaration.functionNameNode
            }
        }
        for (function in PsiTreeUtil.getChildrenOfTypeAsList(myElement.containingFile, ObjJPreprocessorDefineFunction::class.java)) {
            if (function.functionName?.text == functionName) {
                return function.functionName
            }
        }
        return if (allOut.isNotEmpty()) allOut[0] else ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement) ?: myElement
    }

    override fun handleElementRename(newFunctionName: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, newFunctionName)
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    companion object {
        @Suppress("unused")
        private val LOGGER by lazy {
            Logger.getLogger(ObjJFunctionNameReference::class.java.name)
        }
    }

}
