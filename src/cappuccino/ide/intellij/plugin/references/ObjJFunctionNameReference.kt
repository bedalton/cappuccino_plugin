package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJPreprocessorDefineFunction
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameResolveUtil
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger

class ObjJFunctionNameReference(functionName: ObjJFunctionName, val tag: Tag = createTag()) : PsiPolyVariantReferenceBase<ObjJFunctionName>(functionName, TextRange.create(0, functionName.textLength)) {
    private val functionName: String = functionName.text
    private val file: PsiFile = functionName.containingFile
    private val isFunctionCall: Boolean
        get () {
            return myElement.parent is ObjJFunctionCall
        }
    private val isFunctionDeclaration: Boolean
        get() {
            return myElement.parent is ObjJFunctionDeclaration
        }

    private val previousSiblingTypes: Set<String>? by lazy {
        val prevSiblings = myElement.getParentOfType(ObjJFunctionCall::class.java)?.previousSiblings
                ?: return@lazy null
        inferQualifiedReferenceType(prevSiblings, tag)?.toClassList(null)?.toSet().orEmpty()
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element.text != functionName) {
            return false
        }

        if (myElement?.indexInQualifiedReference != 0) {
            if (myElement is ObjJCompositeElement && element is ObjJCompositeElement)
                return false
        }

        val elementIsFunctionCall = element.parent is ObjJFunctionCall
        val elementIsFunctionDeclaration = !elementIsFunctionCall && element.parent is ObjJFunctionDeclaration
        if (isFunctionDeclaration && elementIsFunctionDeclaration) {
            return false
        }
        if (isFunctionCall && elementIsFunctionCall) {
            return false
        }

        if (isFunctionCall) {
            val enclosingClassName = ((element as? JsTypeDefFunctionName)?.parent as? JsTypeDefFunction)?.enclosingNamespaceComponents?.firstOrNull()
            if (enclosingClassName.isNotNullOrBlank() && enclosingClassName!! in previousSiblingTypes.orEmpty())
                return true
            if (previousSiblingTypes == null && enclosingClassName == null)
                return true
        }

        val found = multiResolve(false).mapNotNull { it.element }
        if (element in found)
            return true
        found.forEach {
            if (it.isEquivalentTo(element).orFalse())
                return true
        }
        val resolved = ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement)
                ?: return false
        return resolved == element
    }


    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val resolved = resolveInternal()
        if (resolved != null && !resolved.isEquivalentTo(myElement)) {
            return PsiElementResolveResult.createResults(resolved)
        }
        // Continue if function call
        val functionCall = myElement.parent as? ObjJFunctionCall
                ?: return PsiElementResolveResult.EMPTY_ARRAY
        // Get Base variables
        val functionName = functionCall.functionNameString
                ?: return PsiElementResolveResult.EMPTY_ARRAY
        val properties = getJsNamedElementsForReferencedElement(functionCall, functionName, tag)
        return PsiElementResolveResult.createResults(properties)
    }


    override fun resolve(): PsiElement? {
        return myElement.resolveFromCache {
            multiResolve(false).firstOrNull()?.element
        }
    }

    private fun resolveInternal(): PsiElement? {
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
        return if (allOut.isNotEmpty()) allOut[0] else ObjJVariableNameResolveUtil.getVariableDeclarationElementForFunctionName(myElement)
                ?: myElement
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
