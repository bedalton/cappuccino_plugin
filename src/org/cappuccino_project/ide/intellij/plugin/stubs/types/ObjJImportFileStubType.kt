package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFile
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFileImpl
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJImportStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import org.cappuccino_project.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJImportFileStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJImportFileImpl>(debugName, ObjJImportFileImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJImportFileImpl>): ObjJImportFileImpl {
        return ObjJImportFileImpl(stub, this)
    }
}
