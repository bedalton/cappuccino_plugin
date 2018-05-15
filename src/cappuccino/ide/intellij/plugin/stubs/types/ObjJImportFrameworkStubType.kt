package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.ObjJImportFramework
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImportFileImpl
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJImportFrameworkStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJImportFrameworkImpl>(debugName, ObjJImportFrameworkImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJImportFrameworkImpl>): ObjJImportFrameworkImpl {
        return ObjJImportFrameworkImpl(stub, this)
    }
}
