package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException
import java.util.ArrayList

class ObjJMethodHeaderStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJMethodHeaderStub, ObjJMethodHeaderImpl>(debugName, ObjJMethodHeaderImpl::class.java) {

    override fun createPsi(
            objJMethodHeaderStub: ObjJMethodHeaderStub): ObjJMethodHeaderImpl {
        return ObjJMethodHeaderImpl(objJMethodHeaderStub, this)
    }

    override fun createStub(
            methodHeader: ObjJMethodHeaderImpl, parentStub: StubElement<*>): ObjJMethodHeaderStub {
        val containingClassName = methodHeader.containingClassName
        val selectors = methodHeader.selectorStrings
        val params = methodHeader.paramTypesAsStrings
        val returnType = methodHeader.explicitReturnType
        val returnTypes: Set<String> = methodHeader.returnTypes
        val required = methodHeader.isRequired
        val shouldResolve = ObjJPsiImplUtil.shouldResolve(methodHeader)
        val ignored = ObjJIgnoreEvaluatorUtil.isIgnored(methodHeader.parent, ObjJSuppressInspectionFlags.IGNORE_METHOD)
        return ObjJMethodHeaderStubImpl(parentStub, containingClassName, methodHeader.isStatic, selectors, params, returnType, returnTypes, required, shouldResolve, ignored)
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
        stubOutputStream.writeName(stub.explicitReturnType)
        stubOutputStream.writeUTFFast(stub.returnTypes.joinToString(TYPES_DELIM))
        stubOutputStream.writeBoolean(stub.isRequired)
        stubOutputStream.writeBoolean(stub.shouldResolve())
        stubOutputStream.writeBoolean(stub.ignored)

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
        val explicitReturnType = stream.readNameString() ?: ""
        val returnTypes = stream.readUTFFast().split(TYPES_DELIM).toSet()
        val required = stream.readBoolean()
        val shouldResolve = stream.readBoolean()
        val ignored = stream.readBoolean()
        return ObjJMethodHeaderStubImpl(parentStub, containingClassName, isStatic, selectors, params, explicitReturnType, returnTypes, required, shouldResolve, ignored)
    }


    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node!!.psi as ObjJMethodHeaderDeclaration<*>).containingClassName != ObjJElementFactory.PlaceholderClassName
    }


    override fun indexStub(stub: ObjJMethodHeaderStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexMethod(stub, sink)
    }
}
