package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJStubBasedElement<StubT:StubElement<*>> : StubBasedPsiElement<StubT>
