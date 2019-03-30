package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJIncludeFrameworkImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJIncludeFrameworkStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJIncludeFrameworkImpl>(debugName, ObjJIncludeFrameworkImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJIncludeFrameworkImpl>): ObjJIncludeFrameworkImpl {
        return ObjJIncludeFrameworkImpl(stub, this)
    }
}
