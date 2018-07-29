package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Level

class ObjJVariableReference(
        element: ObjJVariableName) : PsiReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private val referencedElement:PsiElement = resolve(true) ?: myElement
    private val referencedInScope: ReferencedInScope = referencedElement.getContainingScope()

    private val globalVariableNameElement: PsiElement?
        get() {
            if (myElement.indexInQualifiedReference > 0) {
                return null
            }
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            return ObjJVariableNameResolveUtil.getGlobalElement(myElement) ?: ObjJFunctionDeclarationPsiUtil.resolveElementToFunctionDeclarationReference(myElement)
        }



    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val parent = element.parent
        val newVariableName = ObjJElementFactory.createVariableName(myElement.project, newElementName)
        parent.node.replaceChild(myElement.node, newVariableName.node)
        return newVariableName
    }

    override fun isReferenceTo(psiElement: PsiElement): Boolean {
        if (element.containingFile.text.startsWith("@STATIC;")) {
            return false
        }
        if (psiElement.isEquivalentTo(myElement)) {
            return false
        }
        if (psiElement.text != myElement.text) {
            return false
        }
        if (psiElement is ObjJClassName) {
            return true
        }

        if (referencedElement.isEquivalentTo(psiElement)) {
            //LOGGER.log(Level.INFO, "Is reference to self in file: ${psiElement.containingFile.name} to item in file ${referencedElement.containingFile.name}")
            return true
        }

        if (psiElement is ObjJVariableName) {
            if (psiElement.indexInQualifiedReference > 0) {
                return false
            }
        }

        //Finds this elements, and the new elements scope
        val sharedContext:PsiElement? = PsiTreeUtil.findCommonContext(myElement, psiElement)
        val sharedScope:ReferencedInScope = sharedContext?.getContainingScope() ?: UNDETERMINED
        LOGGER.log(Level.INFO, "Shared context is ${sharedContext.getElementType().toString()}; scope is: $sharedScope for var: ${myElement.text}")
        if (referencedInScope != UNDETERMINED) {
            if (sharedScope == UNDETERMINED) {
                return false
            } else if (referencedInScope == sharedScope) {
                return true
            }
        }
        //If
        if (sharedScope != UNDETERMINED) {
            //LOGGER.log(Level.INFO, "Mismatched Shared scope: SharedIn: " + sharedScope.toString() + "; VariableScope: <" + referencedInScope?.toString() + ">")
        }

        return false//referencedInScope == ReferencedInScope.UNDETERMINED && sharedScope != ReferencedInScope.UNDETERMINED
    }

    override fun resolve(): PsiElement? {
        return resolve(false)
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
        val variableName:PsiElement? = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement) ?: globalVariableNameElement
        if (nullIfSelfReferencing) {
            return variableName
        }
        return variableName ?: myElement
    }

    override fun isSoft(): Boolean {
        return false
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    companion object {
        @Suppress("unused")
        private val LOGGER = Logger.getLogger(ObjJVariableReference::class.java.name)
    }

}
