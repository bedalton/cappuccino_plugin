package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import cappuccino.ide.intellij.plugin.utils.Strings
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException

abstract class ObjJImportElementStubType<PsiT : ObjJImportElement<out ObjJImportStub<PsiT>>>(
        debugName: String,
        psiClass: Class<PsiT>) : ObjJStubElementType<ObjJImportStub<PsiT>, PsiT>(debugName, psiClass) {
    override fun createStub(
            statement: PsiT, stubParent: StubElement<*>): ObjJImportStub<PsiT> {
        return ObjJImportStubImpl(stubParent, this, statement.frameworkNameString, statement.fileNameString.orEmpty())
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJImportStub<PsiT>,
            stream: StubOutputStream) {
        stream.writeName(Strings.notNull(stub.framework))
        stream.writeName(stub.fileName)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJImportStub<PsiT> {
        var frameworkName: String? = StringRef.toString(stream.readName())
        if (frameworkName!!.isEmpty()) {
            frameworkName = null
        }
        val fileName = StringRef.toString(stream.readName())
        return ObjJImportStubImpl(stubParent, this, frameworkName, fileName)
    }
}
