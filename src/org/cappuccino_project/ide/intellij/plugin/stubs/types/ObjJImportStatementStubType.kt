package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJImportStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import org.cappuccino_project.ide.intellij.plugin.utils.Strings

import java.io.IOException

abstract class ObjJImportStatementStubType<PsiT : ObjJImportStatement<out ObjJImportStub<PsiT>>>(
        debugName: String,
        psiClass: Class<PsiT>) : ObjJStubElementType<ObjJImportStub<PsiT>, PsiT>(debugName, psiClass, ObjJImportStub::class.java) {
    override fun createStub(
            statement: PsiT, stubParent: StubElement<*>): ObjJImportStub<PsiT> {
        return ObjJImportStubImpl(stubParent, this, statement.frameworkName, statement.fileName)
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
