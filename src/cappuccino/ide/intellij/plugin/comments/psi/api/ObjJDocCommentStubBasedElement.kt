package cappuccino.ide.intellij.plugin.comments.psi.api

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalStubBasedElement
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJDocCommentStubBasedElement<StubT:StubElement<*>> : ObjJUniversalStubBasedElement<StubT>