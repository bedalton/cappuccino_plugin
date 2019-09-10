package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.stubs.stucts.readSelectorStructList
import cappuccino.ide.intellij.plugin.stubs.stucts.writeSelectorStructList
import cappuccino.ide.intellij.plugin.utils.Strings
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.*

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
        val required = methodHeader.isRequired
        val selectorStructs = methodHeader.selectorStructs
        val shouldResolve = ObjJPsiImplUtil.shouldResolve(methodHeader)
        val ignored = ObjJCommentEvaluatorUtil.isIgnored(methodHeader.parent, ObjJSuppressInspectionFlags.IGNORE_METHOD)
        return ObjJMethodHeaderStubImpl(
                parent = parentStub,
                className = containingClassName,
                isStatic = methodHeader.isStatic,
                selectorStrings = selectors,
                paramTypes = params,
                explicitReturnType = returnType,
                isRequired = required,
                selectorStructs = selectorStructs,
                shouldResolve = shouldResolve,
                ignored = ignored
        )
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
        stubOutputStream.writeBoolean(stub.isRequired)
        stubOutputStream.writeSelectorStructList(stub.selectorStructs)
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
        val explicitReturnType = stream.readName()?.string ?: ""
        val required = stream.readBoolean()
        val selectorStructs = stream.readSelectorStructList()
        val shouldResolve = stream.readBoolean()
        val ignored = stream.readBoolean()
        return ObjJMethodHeaderStubImpl(
                parent = parentStub,
                className = containingClassName,
                isStatic = isStatic,
                selectorStrings = selectors,
                paramTypes = params,
                explicitReturnType = explicitReturnType,
                isRequired = required,
                selectorStructs = selectorStructs,
                shouldResolve = shouldResolve,
                ignored = ignored
        )
    }


    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return (node!!.psi as ObjJMethodHeaderDeclaration<*>).containingClassName != ObjJElementFactory.PlaceholderClassName
    }


    override fun indexStub(stub: ObjJMethodHeaderStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexMethod(stub, sink)
    }
}
