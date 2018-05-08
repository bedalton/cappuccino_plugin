package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import org.cappuccino_project.ide.intellij.plugin.utils.Strings

import java.io.IOException
import java.util.ArrayList
import java.util.Objects

class ObjJMethodHeaderStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJMethodHeaderStub, ObjJMethodHeaderImpl>(debugName, ObjJMethodHeaderImpl::class.java, ObjJMethodHeaderStub::class.java) {

    override fun createPsi(
            objJMethodHeaderStub: ObjJMethodHeaderStub): ObjJMethodHeaderImpl {
        return ObjJMethodHeaderImpl(objJMethodHeaderStub, this)
    }

    override fun createStub(
            objJMethodHeader: ObjJMethodHeaderImpl, parentStub: StubElement<*>): ObjJMethodHeaderStub {
        val containingClassName = objJMethodHeader.containingClassName
        val selectors = objJMethodHeader.selectorStrings
        val params = objJMethodHeader.paramTypesAsStrings
        val returnType: String? = null//objJMethodHeader.getReturnType();
        val required = ObjJMethodPsiUtils.methodRequired(objJMethodHeader)
        val shouldResolve = ObjJPsiImplUtil.shouldResolve(objJMethodHeader)
        return ObjJMethodHeaderStubImpl(parentStub, containingClassName, objJMethodHeader.isStatic, selectors, params, returnType, required, shouldResolve)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJMethodHeaderStub,
            stubOutputStream: StubOutputStream) {
        val containingClassName = stub.containingClassName
        stubOutputStream.writeName(containingClassName)
        stubOutputStream.writeBoolean(stub.isStatic)
        val numSelectors = stub.selectorStrings.size
        stubOutputStream.writeInt(numSelectors)
        for (selector in stub.selectorStrings) {
            stubOutputStream.writeName(Strings.notNull(selector))
        }
        val numParams = stub.paramTypes.size
        stubOutputStream.writeInt(numParams)
        for (param in stub.paramTypes) {
            stubOutputStream.writeName(Strings.notNull(param))
        }
        stubOutputStream.writeName(stub.returnType.className)
        stubOutputStream.writeBoolean(stub.isRequired)
        stubOutputStream.writeBoolean(stub.shouldResolve())

    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJMethodHeaderStubImpl {
        val containingClassName = StringRef.toString(stream.readName())
        val isStatic = stream.readBoolean()
        val numSelectors = stream.readInt()
        val selectors = ArrayList<String>()
        for (i in 0 until numSelectors) {
            selectors.add(StringRef.toString(stream.readName()))
        }
        val numParams = stream.readInt()
        val params = ArrayList<String>()
        for (i in 0 until numParams) {
            params.add(StringRef.toString(stream.readName()))
        }
        val returnType = StringRef.toString(stream.readName())
        val required = stream.readBoolean()
        val shouldResolve = stream.readBoolean()
        return ObjJMethodHeaderStubImpl(parentStub, containingClassName, isStatic, selectors, params, returnType, required, shouldResolve)
    }


    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node!!.psi as ObjJMethodHeaderDeclaration<*>).containingClassName != ObjJElementFactory.PLACEHOLDER_CLASS_NAME
    }


    override fun indexStub(stub: ObjJMethodHeaderStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexMethod(stub, sink)
    }
}
