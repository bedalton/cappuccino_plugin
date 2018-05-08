package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement

internal interface ObjJPlaceHolderStub<T : ObjJCompositeElement> : StubElement<T>