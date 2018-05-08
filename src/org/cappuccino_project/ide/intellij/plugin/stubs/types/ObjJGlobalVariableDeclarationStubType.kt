package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJGlobalVariableDeclarationStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import org.cappuccino_project.ide.intellij.plugin.utils.Strings
import org.codehaus.groovy.ast.ASTNode

import java.io.IOException

class ObjJGlobalVariableDeclarationStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJGlobalVariableDeclarationStub, ObjJGlobalVariableDeclarationImpl>(debugName, ObjJGlobalVariableDeclarationImpl::class.java, ObjJGlobalVariableDeclarationStub::class.java) {

    override fun createPsi(
            objJGlobalVariableDeclarationStub: ObjJGlobalVariableDeclarationStub): ObjJGlobalVariableDeclarationImpl {
        return ObjJGlobalVariableDeclarationImpl(objJGlobalVariableDeclarationStub, this)
    }

    override fun createStub(
            variableDeclaration: ObjJGlobalVariableDeclarationImpl, stubParent: StubElement<*>): ObjJGlobalVariableDeclarationStub {
        return ObjJGlobalVariableDeclarationStubImpl(stubParent, ObjJFileUtil.getContainingFileName(variableDeclaration), variableDeclaration.variableNameString, variableDeclaration.variableType)
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

    override fun indexStub(stub: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexGlobalVariableDeclaration(stub, indexSink)
    }

    companion object {

        val VERSION = 3
    }

}
