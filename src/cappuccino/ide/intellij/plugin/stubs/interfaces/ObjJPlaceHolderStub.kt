package cappuccino.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement

internal interface ObjJPlaceHolderStub<T : ObjJCompositeElement> : StubElement<T>