package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import com.intellij.psi.PsiNamedElement

interface ObjJNamedElement : ObjJUniversalPsiElement, ObjJCompositeElement, PsiNamedElement
