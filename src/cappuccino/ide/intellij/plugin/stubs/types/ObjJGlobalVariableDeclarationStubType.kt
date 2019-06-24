package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJGlobalVariableDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJGlobalVariableDeclarationStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJGlobalVariableDeclarationStub, ObjJGlobalVariableDeclarationImpl>(debugName, ObjJGlobalVariableDeclarationImpl::class.java) {

    override fun createPsi(
            objJGlobalVariableDeclarationStub: ObjJGlobalVariableDeclarationStub): ObjJGlobalVariableDeclarationImpl {
        return ObjJGlobalVariableDeclarationImpl(objJGlobalVariableDeclarationStub, this)
    }

    override fun createStub(
            variableDeclaration: ObjJGlobalVariableDeclarationImpl, stubParent: StubElement<*>): ObjJGlobalVariableDeclarationStub {
        return ObjJGlobalVariableDeclarationStubImpl(stubParent, ObjJPsiFileUtil.getContainingFileName(variableDeclaration), variableDeclaration.variableNameString, variableDeclaration.variableType)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJGlobalVariableDeclarationStub,
            stream: StubOutputStream) {
        stream.writeName(Strings.notNull(stub.fileName))
        stream.writeName(stub.variableName)
        stream.writeName(Strings.notNull(stub.variableType))
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJGlobalVariableDeclarationStub {
        var fileName: String? = StringRef.toString(stream.readName())
        if (fileName!!.isEmpty()) {
            fileName = null
        }
        val variableName = StringRef.toString(stream.readName())
        var variableType: String? = StringRef.toString(stream.readName())
        if (variableType!!.isEmpty()) {
            variableType = null
        }
        return ObjJGlobalVariableDeclarationStubImpl(stubParent, fileName, variableName, variableType)
    }

    override fun indexStub(stub: ObjJGlobalVariableDeclarationStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexGlobalVariableDeclaration(stub, sink)
    }

    companion object {

        const val VERSION = 3
    }

}
