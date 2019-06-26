package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass

import java.util.ArrayList

object ObjJResolveableElementUtil {

    fun <PsiT : PsiElement> onlyResolveableElements(elements: List<PsiT>?): List<PsiT> {
        val out = ArrayList<PsiT>()
        if (elements == null) {
            return out
        }
        for (element in elements) {
            if (shouldResolve(element)) {
                out.add(element)
            }
        }
        return out
    }

    fun shouldResolve(psiElement: PsiElement?): Boolean {
        return if (psiElement == null) {
            false
        } else shouldResolve(psiElement, "Ignoring " + psiElement.node.elementType.toString() + " in file: " + ObjJPsiFileUtil.getContainingFileName(psiElement))
    }

    fun shouldResolve(psiElement: ObjJClassDeclarationElement<*>?): Boolean {
        return if (psiElement == null) {
            false
        } else shouldResolve(psiElement, "Ignoring " + (if (psiElement is ObjJImplementationDeclaration) "class" else "protocol") + " " + psiElement.classNameString)
    }

    @Suppress("UNUSED_PARAMETER")
    fun shouldResolve(psiElement: PsiElement?, shouldNotResolveLoggingStatement: String?): Boolean {
        @Suppress("ConstantConditionIf")
        /*
        if (psiElement == null) {
            return false
        }
        val stubElement = (psiElement as? StubBasedPsiElement<*>)?.stub
        if (stubElement is ObjJResolveableStub<*>) {
            return stubElement.shouldResolve()
        }
        val comment = psiElement.getPreviousNonEmptySibling(true) ?: return true
        val shouldResolveThisElement = !comment.text.contains("@ignore")
        if (!shouldResolveThisElement) {
            if (shouldNotResolveLoggingStatement != null) {
                //LOGGER.log(Level.INFO, shouldNotResolveLoggingStatement)
            }
            return false
        }
        var shouldResolveParent = true
        val parentResolveableElement = psiElement.getParentOfType( ObjJResolveableElement::class.java)
        if (parentResolveableElement != null) {
            shouldResolveParent = parentResolveableElement.shouldResolve()
        }
        return shouldResolveParent
        */
        return true
    }

    fun shouldResolve(hasContainingClass: ObjJHasContainingClass?): Boolean {
        return shouldResolve(hasContainingClass as PsiElement?) && shouldResolve(hasContainingClass!!.containingClass)
    }
}
