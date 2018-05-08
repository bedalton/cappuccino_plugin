package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

open class ObjJStubBaseImpl<PsiT : StubBasedPsiElement<out StubElement<*>>> protected constructor(parent: StubElement<*>, elementType: IStubElementType<*, *>) : StubBase<PsiT>(parent, elementType)
