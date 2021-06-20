package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl
import cappuccino.ide.intellij.plugin.psi.utils.ObjJAccessorPropertyPsiUtil
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJAccessorPropertyStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub
import cappuccino.ide.intellij.plugin.stubs.stucts.readSelectorStructList
import cappuccino.ide.intellij.plugin.stubs.stucts.writeSelectorStructList
import cappuccino.ide.intellij.plugin.utils.Strings
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException

class ObjJAccessorPropertyStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJAccessorPropertyStub, ObjJAccessorPropertyImpl>(debugName, ObjJAccessorPropertyImpl::class.java) {

    override fun createPsi(
            objJAccessorPropertyStub: ObjJAccessorPropertyStub): ObjJAccessorPropertyImpl {
        return ObjJAccessorPropertyImpl(objJAccessorPropertyStub, this)
    }

    override fun createStub(
            accessorProperty: ObjJAccessorPropertyImpl, parentStub: StubElement<*>): ObjJAccessorPropertyStub {
        val containingClass = accessorProperty.containingClassName
        val variableDeclaration = accessorProperty.getParentOfType( ObjJInstanceVariableDeclaration::class.java)
        val variableName: String?
        val variableType: String?
        if (variableDeclaration != null) {
            val variableDeclarationStub = variableDeclaration.stub
            variableType = variableDeclarationStub?.variableType ?: variableDeclaration.formalVariableType.text
            variableName = variableDeclarationStub?.variableName ?: if (variableDeclaration.variableName != null) variableDeclaration.variableName!!.text else null
        } else {
            variableName = null
            variableType = null
        }
        val getter = if (variableName != null && variableType != null) ObjJAccessorPropertyPsiUtil.getGetterSelector(variableName, variableType, accessorProperty) else null
        val setter = if (variableName != null && variableType != null) ObjJAccessorPropertyPsiUtil.getSetterSelector(variableName, variableType, accessorProperty) else null
        val selectorStructs = accessorProperty.selectorStructs
        return ObjJAccessorPropertyStubImpl(
                parent = parentStub,
                containingClass = containingClass,
                variableType = variableType,
                variableName = variableName,
                getter = getter,
                setter = setter,
                selectorStructs = selectorStructs,
                shouldResolve = shouldResolve(accessorProperty.node)
        )
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJAccessorPropertyStub,
            stream: StubOutputStream) {
        stream.writeName(stub.containingClass)
        stream.writeName(Strings.notNull(stub.variableType))
        stream.writeName(Strings.notNull(stub.variableName))
        stream.writeName(Strings.notNull(stub.getter))
        stream.writeName(Strings.notNull(stub.setter))
        stream.writeSelectorStructList(stub.selectorStructs)
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJAccessorPropertyStub {
        val containingClass = StringRef.toString(stream.readName())
        val variableType = StringRef.toString(stream.readName())
        val variableName = StringRef.toString(stream.readName())
        val getter = StringRef.toString(stream.readName())
        val setter = StringRef.toString(stream.readName())
        val selectorStructs = stream.readSelectorStructList()
        val shouldResolve = stream.readBoolean()
        return ObjJAccessorPropertyStubImpl(
                parent = parentStub,
                containingClass = containingClass,
                variableType = variableType,
                variableName = variableName,
                getter = getter,
                setter = setter,
                selectorStructs = selectorStructs,
                shouldResolve = shouldResolve
        )
    }

    override fun indexStub(stub: ObjJAccessorPropertyStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexAccessorProperty(stub, sink)
    }
}
