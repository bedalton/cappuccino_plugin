package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface JsTypeDefStubBasedElement<StubT:StubElement<*>> : StubBasedPsiElement<StubT>, JsTypeDefElement