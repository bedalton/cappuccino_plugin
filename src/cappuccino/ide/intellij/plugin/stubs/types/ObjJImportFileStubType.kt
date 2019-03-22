package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJImportFileImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJImportFileStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJImportFileImpl>(debugName, ObjJImportFileImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJImportFileImpl>): ObjJImportFileImpl {
        return ObjJImportFileImpl(stub, this)
    }
}
