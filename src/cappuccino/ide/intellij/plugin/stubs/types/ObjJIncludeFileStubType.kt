package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJIncludeFileImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJIncludeFileStubType(
        debugName: String) : ObjJImportElementStubType<ObjJIncludeFileImpl>(debugName, ObjJIncludeFileImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJIncludeFileImpl>): ObjJIncludeFileImpl {
        return ObjJIncludeFileImpl(stub, this)
    }
}
