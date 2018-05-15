package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

class ObjJFunctionNameReference(functionName: ObjJFunctionName) : PsiReferenceBase<ObjJFunctionName>(functionName, TextRange.create(0, functionName.textLength)) {
    private val functionName: String
    private val file: PsiFile

    init {
        this.functionName = functionName.text
        //LOGGER.log(Level.INFO, "Created function name resolver with text: <"+this.functionName+"> and canonText: <"+getCanonicalText()+">");
        file = functionName.containingFile
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        var isCorrectReference = element is ObjJVariableName || element is ObjJFunctionName
        if (element.getParentOfType( ObjJFunctionCall::class.java) != null) {
            isCorrectReference = isCorrectReference && element.getParentOfType( ObjJFunctionDeclarationElement::class.java) != null
        }
        return isCorrectReference && element.text == functionName
    }

    override fun resolve(): PsiElement? {
        if (DumbServiceImpl.isDumb(myElement.project)) {
            return null
        }
        val allOut = ArrayList<PsiElement>()
        //LOGGER.log(Level.INFO, "There are <"+ObjJFunctionsIndex.getInstance().getAllKeys(myElement.getProject()).size()+"> function in index");
        for (functionDeclaration in ObjJFunctionsIndex.instance.get(functionName, myElement.project)) {
            ProgressIndicatorProvider.checkCanceled()
            allOut.add(functionDeclaration.functionNameNode!!)
            if (functionDeclaration.getContainingFile().isEquivalentTo(file)) {
                return functionDeclaration.functionNameNode
            }
        }
        for (function in PsiTreeUtil.getChildrenOfTypeAsList(myElement.containingFile, ObjJPreprocessorDefineFunction::class.java)) {
            if (function.functionName != null && function.functionName!!.text == myElement.text) {
                return function.functionName
            }
        }
        return if (!allOut.isEmpty()) allOut[0] else null
    }

    override fun handleElementRename(newFunctionName: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, newFunctionName)
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    companion object {

        private val LOGGER = Logger.getLogger(ObjJFunctionNameReference::class.java.name)
    }

}
