package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


object ObjJResolveUtil {
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