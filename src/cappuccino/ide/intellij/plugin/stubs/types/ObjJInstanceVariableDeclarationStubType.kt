package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.utils.ObjJAccessorPropertyPsiUtil
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJInstanceVariableDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJInstanceVariableDeclarationStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJInstanceVariableDeclarationStub, ObjJInstanceVariableDeclarationImpl>(debugName, ObjJInstanceVariableDeclarationImpl::class.java) {

    override fun createPsi(
            objJInstanceVariableDeclarationStub: ObjJInstanceVariableDeclarationStub): ObjJInstanceVariableDeclarationImpl {
        return ObjJInstanceVariableDeclarationImpl(objJInstanceVariableDeclarationStub, this)
    }

    override fun createStub(
            declaration: ObjJInstanceVariableDeclarationImpl, stubElement: StubElement<*>): ObjJInstanceVariableDeclarationStub {
        var getter: String? = null
        var setter: String? = null
        val variableName = if (declaration.variableName != null) declaration.variableName!!.text else ""
        if (declaration.atAccessors != null && declaration.accessorPropertyList.isEmpty() && !variableName.isEmpty()) {
            getter = ObjJAccessorPropertyPsiUtil.getGetterSelector(variableName, declaration.formalVariableType.text)
            setter = ObjJAccessorPropertyPsiUtil.getSetterSelector(variableName, declaration.formalVariableType.text)
            //Logger.getAnonymousLogger().log(Level.INFO, "Variable: <"+variableName+">; getter: <"+getter+">; setter: <"+setter+">");
        }
        val shouldResolve = declaration.shouldResolve()
        return ObjJInstanceVariableDeclarationStubImpl(stubElement, declaration.containingClassName, declaration.formalVariableType.text, variableName, getter, setter, shouldResolve)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJInstanceVariableDeclarationStub,
            stream: StubOutputStream) {
        stream.writeName(stub.containingClass)
        stream.writeName(stub.varType)
        stream.writeName(stub.variableName)
        stream.writeName(Strings.notNull(stub.getter, ""))
        stream.writeName(Strings.notNull(stub.setter, ""))
        stream.writeBoolean(stub.shouldResolve())
    }

    override fun indexStub(stub: ObjJInstanceVariableDeclarationStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexInstanceVariable(stub, sink)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJInstanceVariableDeclarationStub {
        val containingClass = StringRef.toString(stream.readName())
        val varType = StringRef.toString(stream.readName())!!
        val variableName = StringRef.toString(stream.readName())!!
        val getter = StringRef.toString(stream.readName())
        val setter = StringRef.toString(stream.readName())
        val shouldResolve = stream.readBoolean()
        return ObjJInstanceVariableDeclarationStubImpl(parentStub, containingClass, varType, variableName, getter, setter, shouldResolve)
    }
}
