package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalStubBasedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJStubBasedElement<StubT:StubElement<*>> : ObjJUniversalStubBasedElement<StubT>
