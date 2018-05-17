package cappuccino.ide.intellij.plugin.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement


object ObjJResolveUtil {


    fun treeWalkUp(place: PsiElement?, processor: PsiScopeProcessor): Boolean {
        var lastParent: PsiElement? = null
        var run = place
        while (run != null) {
            if (place !== run && !run.processDeclarations(processor, ResolveState.initial(), lastParent, place!!)) return false
            lastParent = run
            run = run.parent
        }
        return true
    }

    fun processChildren(element: PsiElement,
                        processor: PsiScopeProcessor,
                        substitutor: ResolveState,
                        lastParent: PsiElement?,
                        place: PsiElement): Boolean {
        var run: PsiElement? = if (lastParent == null) element.lastChild else lastParent.prevSibling
        while (run != null) {
            if (run is ObjJCompositeElement && !run.processDeclarations(processor, substitutor, null, place)) return false
            run = run.prevSibling
        }
        return true
    }
}