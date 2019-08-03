package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

open class JsTypeDefStubBaseImpl<PsiT : StubBasedPsiElement<out StubElement<*>>> protected constructor(parent: StubElement<*>?, elementType: IStubElementType<*, *>) : StubBase<PsiT>(parent, elementType)