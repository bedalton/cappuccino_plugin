package org.cappuccino_project.ide.intellij.plugin.stubs.types

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJIncludeFrameworkImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJIncludeFrameworkStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJIncludeFrameworkImpl>(debugName, ObjJIncludeFrameworkImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJIncludeFrameworkImpl>): ObjJIncludeFrameworkImpl {
        return ObjJIncludeFrameworkImpl(stub, this)
    }
}
