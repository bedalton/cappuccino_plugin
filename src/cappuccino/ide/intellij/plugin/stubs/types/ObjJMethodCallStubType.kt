package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJMethodCallStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.*

class ObjJMethodCallStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJMethodCallStub, ObjJMethodCallImpl>(debugName, ObjJMethodCallImpl::class.java) {

    override fun createPsi(
            objJMethodCallStub: ObjJMethodCallStub): ObjJMethodCallImpl {
        return ObjJMethodCallImpl(objJMethodCallStub, this)
    }

    override fun createStub(
            methodCall: ObjJMethodCallImpl, stubParent: StubElement<*>): ObjJMethodCallStub {
        val className = methodCall.containingClassName
        val callTarget = methodCall.callTargetText
        // @todo calculate call target type and store it
        val callTargetTypes = listOf(ObjJClassType.UNDETERMINED)//methodCall.getPossibleCallTargetTypes();
        val selectorStrings = methodCall.selectorStrings
        val shouldResolve = methodCall.shouldResolve()
        return ObjJMethodCallStubImpl(stubParent, className, callTarget, callTargetTypes, selectorStrings, shouldResolve)
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJMethodCallStub,
            stream: StubOutputStream) {
        stream.writeName(stub.containingClassName)
        stream.writeName(stub.callTarget)
        stream.writeInt(stub.possibleCallTargetTypes.size)
        for (possibleCallTargetType in stub.possibleCallTargetTypes) {
            stream.writeName(possibleCallTargetType)
        }
        stream.writeInt(stub.selectorStrings.size)
        for (selector in stub.selectorStrings) {
            stream.writeName(selector)
        }
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, stubParent: StubElement<*>): ObjJMethodCallStub {
        val containingClassName = StringRef.toString(stream.readName())
        val callTarget = StringRef.toString(stream.readName())
        val numCallTargetTypes = stream.readInt()
        val callTargetTypes = ArrayList<String>()
        for (i in 0 until numCallTargetTypes) {
            callTargetTypes.add(StringRef.toString(stream.readName()))
        }
        val numSelectorStrings = stream.readInt()
        val selectors = ArrayList<String>()
        for (i in 0 until numSelectorStrings) {
            selectors.add(StringRef.toString(stream.readName()))
        }
        val shouldResolve = stream.readBoolean()
        return ObjJMethodCallStubImpl(stubParent, containingClassName, callTarget, callTargetTypes, selectors, shouldResolve)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return false
    }

    override fun indexStub(
            stub: ObjJMethodCallStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexMethodCall(stub, sink)
    }
}
