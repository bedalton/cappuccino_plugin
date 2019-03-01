package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBasedPsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJResolveableElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

object ObjJResolveableElementUtil {
    private val LOGGER = Logger.getLogger(ObjJResolveableElementUtil::class.java.canonicalName)

    fun <PsiT : PsiElement> onlyResolveableElements(elements: List<PsiT>?, file: PsiFile): List<PsiT> {
        val out = ArrayList<PsiT>()
        if (elements == null) {
            return out
        }
        for (element in elements) {
            if (true || shouldResolve(element) || file.isEquivalentTo(element.containingFile)) {
                out.add(element)
            }
        }
        return out
    }

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

    fun <PsiT : ObjJResolveableElement<*>> onlyResolveables(elements: List<PsiT>?): List<PsiT> {
        val out = ArrayList<PsiT>()
        if (elements == null) {
            return out
        }
        for (element in elements) {
            val stub:ObjJResolveableStub<*>? = element.stub
            if (stub != null && stub.shouldResolve() || element.shouldResolve()) {
                out.add(element)
            }
        }
        return out
    }

    fun shouldResolve(psiElement: PsiElement?): Boolean {
        return if (psiElement == null) {
            false
        } else shouldResolve(psiElement, "Ignoring " + psiElement.node.elementType.toString() + " in file: " + ObjJFileUtil.getContainingFileName(psiElement))
    }

    fun shouldResolve(psiElement: ObjJClassDeclarationElement<*>?): Boolean {
        return if (psiElement == null) {
            false
        } else shouldResolve(psiElement, "Ignoring " + (if (psiElement is ObjJImplementationDeclaration) "class" else "protocol") + " " + psiElement.getClassNameString())
    }

    fun shouldResolve(psiElement: PsiElement?, shouldNotResolveLoggingStatement: String?): Boolean {
        if (true) {
            return true
        }
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
    }

    fun shouldResolve(hasContainingClass: ObjJHasContainingClass?): Boolean {
        return shouldResolve(hasContainingClass as PsiElement?) && shouldResolve(hasContainingClass!!.containingClass)
    }
}
