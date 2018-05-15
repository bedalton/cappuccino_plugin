package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.psi.ObjJImportFile
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImportFileImpl
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJImportFileStubType(
        debugName: String) : ObjJImportStatementStubType<ObjJImportFileImpl>(debugName, ObjJImportFileImpl::class.java) {

    override fun createPsi(
            stub: ObjJImportStub<ObjJImportFileImpl>): ObjJImportFileImpl {
        return ObjJImportFileImpl(stub, this)
    }
}
