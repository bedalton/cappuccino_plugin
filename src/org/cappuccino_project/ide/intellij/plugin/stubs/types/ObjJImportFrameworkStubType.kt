package org.cappuccino_project.ide.intellij.plugin.stubs.types

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFileImpl
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJImportFrameworkStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJImportFrameworkImpl>(debugName, ObjJImportFrameworkImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJImportFrameworkImpl>): ObjJImportFrameworkImpl {
        return ObjJImportFrameworkImpl(stub, this)
    }
}
