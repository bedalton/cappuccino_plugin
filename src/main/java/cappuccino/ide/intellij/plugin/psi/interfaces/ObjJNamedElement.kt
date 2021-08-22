package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import com.intellij.psi.PsiNamedElement

interface ObjJUniversalNamedElement : ObjJUniversalPsiElement, PsiNamedElement
interface ObjJNamedElement : ObjJUniversalNamedElement, PsiNamedElement, ObjJCompositeElement
