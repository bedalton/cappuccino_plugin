package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

import java.io.IOException
import java.util.ArrayList

abstract class ObjJAbstractFunctionDeclarationStubType<PsiT : ObjJFunctionDeclarationElement<out ObjJFunctionDeclarationElementStub<*>>, Stub : ObjJFunctionDeclarationElementStub<PsiT>> internal constructor(
        debugName: String, functionDecClass: Class<PsiT>, stubClass: Class<Stub>) : ObjJStubElementType<ObjJFunctionDeclarationElementStub<PsiT>, PsiT>(debugName, functionDecClass, stubClass) {

    override fun createStub(
            element: PsiT, stubParent: StubElement<*>): ObjJFunctionDeclarationElementStub<PsiT> {
        val fileName = if (element.containingFile != null && element.containingFile.virtualFile != null) element.containingFile.virtualFile.name else "undefined"
        val functionNameString = element.functionNameAsString
        val paramNames = element.paramNames
        val returnType = element.returnType
        val shouldResolve = element.shouldResolve()
        return createStub(stubParent, fileName, functionNameString, paramNames, returnType, shouldResolve)
    }

    internal abstract fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean): ObjJFunctionDeclarationElementStub<PsiT>

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJFunctionDeclarationElementStub<*>,
            stream: StubOutputStream) {

        stream.writeName(stub.fileName)
        stream.writeName(stub.fqName)
        stream.writeInt(stub.numParams)
        for (param in stub.paramNames) {
            stream.writeName(param as String)
        }
        stream.writeName(stub.returnType)
        stream.writeBoolean(stub.shouldResolve())
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
        return createStub(stubParent, fileName, fqName, paramNames, returnType, shouldResolve)
    }

    override fun indexStub(stub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexFunctionDeclaration(stub, indexSink)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return node!!.psi is ObjJFunctionDeclarationElement<*>
    }
}
