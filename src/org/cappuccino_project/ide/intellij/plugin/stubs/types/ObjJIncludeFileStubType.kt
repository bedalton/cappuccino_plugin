package org.cappuccino_project.ide.intellij.plugin.stubs.types

import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJIncludeFileImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJIncludeFileStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJIncludeFileImpl>(debugName, ObjJIncludeFileImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJIncludeFileImpl>): ObjJIncludeFileImpl {
        return ObjJIncludeFileImpl(stub, this)
    }
}
