package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJVarTypeIdStubImpl(parent: StubElement<*>, override val idType: String, internal val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJVarTypeIdImpl>(parent, ObjJStubTypes.VAR_TYPE_ID), ObjJVarTypeIdStub {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
