package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalStubBasedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface JsTypeDefStubBasedElement<StubT:StubElement<*>> : ObjJUniversalStubBasedElement<JsTypeDefElement, StubT>, JsTypeDefElement