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
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameResolveUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

class ObjJFunctionNameReference(functionName: ObjJFunctionName) : PsiReferenceBase<ObjJFunctionName>(functionName, TextRange.create(0, functionName.textLength)) {
    private val functionName: String = functionName.text
    private val file: PsiFile = functionName.containingFile;
    private val isFunctionCall:Boolean get () {
        return myElement.parent is ObjJFunctionCall
    }
    private val isFunctionDeclaration:Boolean get() {
        return myElement.parent is ObjJFunctionDeclaration
    }

    init {
        LOGGER.log(Level.INFO, "Created function name resolver with text: <"+this.functionName+"> and canonText: <"+ this.canonicalText +">");
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        LOGGER.info("Is Reference to <${element.text}>")
        if (element.text != functionName) {
            LOGGER.info("Function name and element text do not match")
            return false
        }
        val elementIsFunctionCall = element.parent is ObjJFunctionCall
        val elementIsFunctionDeclaration = !elementIsFunctionCall && element.parent is ObjJFunctionDeclaration
        if (isFunctionDeclaration && elementIsFunctionDeclaration) {
            LOGGER.info("Function name and element are both function declarations")
            return false
        }
        if (isFunctionCall && elementIsFunctionCall) {
            LOGGER.info("Function name and element are both function calls")
            return false
        }
        if (resolve()?.isEquivalentTo(element) == true) {
            LOGGER.info("This element.resolve() == element")
            return true
        }
        val resolved = ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement) ?: return {
            LOGGER.info("Failed to resolve function name to element")
            false
        }()
        LOGGER.info("Resolved element for ${resolved.text} is ${resolved.tokenType()} in file ${resolved.containingFile?.name?:"UNDEF"}")
        return resolved == element
    }

    override fun resolve(): PsiElement? {
        if (DumbServiceImpl.isDumb(myElement.project)) {
            return null
        }
        val allOut = ArrayList<PsiElement>()
        //LOGGER.log(Level.INFO, "There are <"+ObjJFunctionsIndex.getInstance().getAllKeys(myElement.getProject()).size()+"> function in index");
        for (functionDeclaration in ObjJFunctionsIndex.instance[functionName, myElement.project]) {
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
        return if (!allOut.isEmpty()) allOut[0] else ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement)
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
