package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl

interface ObjJVarTypeIdStub : StubElement<ObjJVarTypeIdImpl>, ObjJResolveableStub<ObjJVarTypeIdImpl> {
    val idType: String
}
