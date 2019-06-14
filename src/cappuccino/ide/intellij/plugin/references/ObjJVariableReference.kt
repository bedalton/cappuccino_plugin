package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.PsiTreeUtil

class ObjJVariableReference(
        element: ObjJVariableName,
        private val follow:Boolean = true,
        private val nullIfSelfReferencing: Boolean? = null
) : PsiReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private var referencedInScope: ReferencedInScope? = null

    private val isGlobal: Boolean by lazy {
        variableDeclarationsEnclosedGlobal(myElement, true)
    }

    private val referencedElement:SmartPsiElementPointer<PsiElement>? by lazy {
        val resolved = resolve()
        if (resolved != null)
            SmartPointerManager.createPointer(resolved)
        else
            null
    }

    private val globalVariableNameElement: PsiElement?
        get() {
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            val file = myElement.containingObjJFile
            val imports = file?.importStrings
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[myElement.text, myElement.project]
            var namedElement:PsiElement? = null
            if (globalVariableDeclarations.isNotEmpty()) {
                if (imports == null) {
                    namedElement = globalVariableDeclarations[0].variableName
                } else {
                    for (declaration in globalVariableDeclarations) {
                        if (imports.contains(ObjJFileUtil.getContainingFileName(declaration.containingFile))) {
                            namedElement = declaration.variableName
                        }
                    }
                }
            }
            val functionDeclarationElements = ObjJFunctionsIndex.instance[myElement.text, myElement.project]
            if (namedElement == null && functionDeclarationElements.isNotEmpty()) {
                namedElement = functionDeclarationElements[0].functionNameNode
                if (namedElement == null) {
                    for (declarationElement in functionDeclarationElements) {
                        namedElement = declarationElement.functionNameNode
                        if (namedElement != null) {
                            break
                        }
                    }
                }
            }
            return if (namedElement != null && !namedElement.isEquivalentTo(myElement)) namedElement else null
        }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val parent = element.parent
        val newVariableName = ObjJElementFactory.createVariableName(myElement.project, newElementName)
        parent.node.replaceChild(myElement.node, newVariableName.node)
        return newVariableName
    }

    override fun isReferenceTo(psiElement: PsiElement): Boolean {

        // Element is in compiled objective-j document
        try {
            if (psiElement.containingFile.text.startsWith("@STATIC;") || myElement.containingFile.text.startsWith("@STATIC;")) {
                return false
            }
        // Element is virtual and not in file
        } catch (e:Exception) { return false }

        // Text is not equivalent, ignore
        if (psiElement.text != myElement.text) {
            return false
        }
        //Is Same element, Do not reference self
        if (psiElement.isEquivalentTo(myElement)) {
            return false
        }

        val psiElementIsZeroIndexInQualifiedReference = psiElement !is ObjJVariableName || psiElement.indexInQualifiedReference == 0
        val thisElementIsZeroIndexedInQualifiedReference = myElement.indexInQualifiedReference == 0
        if (!psiElementIsZeroIndexInQualifiedReference || !thisElementIsZeroIndexedInQualifiedReference) {
            return false
        }
        if (thisElementIsZeroIndexedInQualifiedReference && psiElement is ObjJClassName) {
            return true
        }

        val referencedElement = this.referencedElement?.element
        if (referencedElement?.isEquivalentTo(psiElement).orFalse()) {
            return true
        }

        if (referencedInScope == null) {
            referencedInScope = referencedElement?.getContainingScope() ?: myElement.getContainingScope()
        }

        //Finds this elements, and the new elements scope
        val sharedContext:PsiElement? = PsiTreeUtil.findCommonContext(myElement, psiElement)
        val sharedScope:ReferencedInScope = sharedContext?.getContainingScope() ?: UNDETERMINED
        if (sharedScope == UNDETERMINED && referencedInScope != UNDETERMINED) {
            return false
        }
        if (referencedInScope != UNDETERMINED && referencedInScope == sharedScope) {
            return true
        }
        return false
    }

    override fun resolve(): PsiElement? {
        return myElement.resolveFromCache {
            resolve(nullIfSelfReferencing.orFalse())
        }
    }

    fun resolve(nullIfSelfReferencing: Boolean) : PsiElement? {
        try {
            if (myElement.containingFile.text.startsWith("@STATIC;")) {
                return null
            }
        } catch (ignored:Exception) {
            //Exception was thrown on failed attempts at adding code to file pragmatically
            return null
        }

        if (follow && isGlobal) {
            return if (nullIfSelfReferencing) null else myElement
        }

        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement)
        if (myElement.indexInQualifiedReference > 0) {
            return if (nullIfSelfReferencing) {
                variableName
            } else {
                variableName ?: myElement
            }
        }
        if (variableName == null) {
            variableName = globalVariableNameElement
        }
        if (variableName == null) {
            variableName = resolveIfClassName()
            if (variableName != null)
                return variableName
        }
        if (variableName is ObjJVariableName && variableName.indexInQualifiedReference > 0) {
            return if (nullIfSelfReferencing) {
                null
            } else {
                myElement
            }
        }
        if (nullIfSelfReferencing) {
            return variableName
        }

        return variableName ?: myElement
    }

    private fun resolveIfClassName() : PsiElement? {
        val callTarget = myElement.parent?.parent as? ObjJCallTarget ?: return null
        val selector = (callTarget.parent as? ObjJMethodCall)?.selectorString ?: return null
        var classes: List<ObjJClassDeclarationElement<*>> = ObjJClassDeclarationsIndex.instance[myElement.text, element.project]
        if (selector.isEmpty() || classes.isEmpty())
            return null
        val classesTemp = classes.filter {
            it.hasMethod(selector)
        }
        if (classesTemp.isNotEmpty()) {
            classes = classesTemp
        }
        return classes.firstOrNull { it is ObjJImplementationDeclaration && !it.isCategory }
                ?: classes.firstOrNull()
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

}


private fun variableDeclarationsEnclosedGlobal(variableName: ObjJVariableName, @Suppress("SameParameterValue") follow:Boolean = false) : Boolean {
    if(!DumbService.isDumb(variableName.project) && follow) {
        return variableDeclarationsEnclosedGlobalStrict(variableName)
    }
    if (variableName.indexInQualifiedReference != 0)
        return false

    val variableDeclaration = variableName.parent.parent as? ObjJVariableDeclaration ?: return false
    val isBodyVariableAssignmentLocal
            = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
    if (isBodyVariableAssignmentLocal || variableDeclaration.parent.parent.parent !is ObjJBlock) {
        return false
    }
    val variableNameString = variableName.text
    return !variableDeclaration.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true).any { bodyVariableAssignment ->
        bodyVariableAssignment.variableDeclarationList?.variableDeclarationList?.any { varDec ->
            varDec.qualifiedReferenceList.any {
                if (it.qualifiedNameParts.size == 1 && it.qualifiedNameParts[0]?.text == variableNameString)
                    bodyVariableAssignment.varModifier != null
                else
                    false
            }
        }.orFalse()
    }
}

private fun variableDeclarationsEnclosedGlobalStrict(variableName: ObjJVariableName) : Boolean {
    if (variableName.indexInQualifiedReference != 0)
        return false

    val variableDeclaration = variableName.parent.parent as? ObjJVariableDeclaration ?: return false
    val isBodyVariableAssignmentLocal
            = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
    if (isBodyVariableAssignmentLocal || variableDeclaration.parent.parent.parent !is ObjJBlock) {
        return false
    }
    val resolved = ObjJVariableReference(variableName, false).resolve() ?: return true
    return (resolved.parent.parent.parent.parent as? ObjJBodyVariableAssignment)?.varModifier == null
}