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
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import com.intellij.psi.util.PsiTreeUtil

class ObjJVariableReference(
        element: ObjJVariableName) : PsiReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private var referencedInScope: ReferencedInScope? = null

    private val globalVariableNameElement: PsiElement?
        get() {
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            val file = myElement.containingObjJFile
            val imports = file?.importStrings
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[myElement.text, myElement.project]
            var namedElement:PsiElement? = null
            if (!globalVariableDeclarations.isEmpty()) {
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
            if (namedElement == null && !functionDeclarationElements.isEmpty()) {
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


        val psiElementInZeroIndexInQualifiedReference = psiElement !is ObjJVariableName || psiElement.indexInQualifiedReference == 0
        val thisElementIsZeroIndexedInQualifiedReference = myElement.indexInQualifiedReference == 0
        if (!psiElementInZeroIndexInQualifiedReference || !thisElementIsZeroIndexedInQualifiedReference) {
            return false
        }
        if (thisElementIsZeroIndexedInQualifiedReference && psiElement is ObjJClassName) {
            return true
        }

        val referencedElement = resolve(true)
        if (referencedElement?.isEquivalentTo(psiElement) == true) {
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
        return resolve(true)
    }

    private fun resolve(nullIfSelfReferencing:Boolean) : PsiElement? {
        try {
            if (myElement.containingFile.text.startsWith("@STATIC;")) {
                return null
            }
        } catch (ignored:Exception) {
            //Exception was thrown on failed attempts at adding code to file pragmatically
            return null
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
