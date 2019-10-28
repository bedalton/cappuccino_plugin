package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.*

abstract class ObjJAbstractFunctionDeclarationStubType<PsiT : ObjJFunctionDeclarationElement<out ObjJFunctionDeclarationElementStub<*>>> internal constructor(
        debugName: String, functionDecClass: Class<PsiT>) : ObjJStubElementType<ObjJFunctionDeclarationElementStub<PsiT>, PsiT>(debugName, functionDecClass) {

    override fun createStub(
            element: PsiT, stubParent: StubElement<*>): ObjJFunctionDeclarationElementStub<PsiT> {
        val fileName = if (element.containingFile != null && element.containingFile.virtualFile != null) element.containingFile.virtualFile.name else "undefined"
        val functionNameString = element.functionNameString
        val paramNames = element.parameterNames
        val returnType = null
        val shouldResolve = element.shouldResolve()
        val scope = element.functionScope
        return createStub(stubParent, fileName, functionNameString, paramNames, returnType, shouldResolve, scope)
    }

    internal abstract fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean,
                                     scope:ObjJFunctionScope): ObjJFunctionDeclarationElementStub<PsiT>

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJFunctionDeclarationElementStub<PsiT>,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.fqName)
        stream.writeInt(stub.numParams)
        for (param in stub.paramNames) {
            stream.writeName(param)
        }
        stream.writeName(stub.returnType)
        stream.writeBoolean(stub.shouldResolve())
        stream.writeInt(stub.scope.intVal)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJFunctionDeclarationElementStub<PsiT> {
        val fileName = StringRef.toString(stream.readName())
        val fqName = StringRef.toString(stream.readName())
        val numParams = stream.readInt()
        val paramNames = ArrayList<String>()
        for (i in 0 until numParams) {
            paramNames.add(StringRef.toString(stream.readName()))
        }
        val returnType = StringRef.toString(stream.readName())
        val shouldResolve = stream.readBoolean()
        val scope = ObjJFunctionScope.fromValue(stream.readInt())
        return createStub(stubParent, fileName, fqName, paramNames, returnType, shouldResolve, scope)
    }

    override fun indexStub(stub: ObjJFunctionDeclarationElementStub<PsiT>, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexFunctionDeclaration(stub, sink)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        val psi = node?.psi as? ObjJFunctionDeclarationElement<*> ?: return false
        val functionScope = ObjJFunctionDeclarationPsiUtil.getFunctionScope(psi, false)
        return functionScope == ObjJFunctionScope.FILE_SCOPE || functionScope == ObjJFunctionScope.GLOBAL_SCOPE
    }

}
