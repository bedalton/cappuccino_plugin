package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl
import cappuccino.ide.intellij.plugin.psi.utils.getScopeBlockRanges
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJVariableNameStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.Pair
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import java.io.IOException
import java.util.*

class ObjJVariableNameStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJVariableNameStub, ObjJVariableNameImpl>(debugName, ObjJVariableNameImpl::class.java) {

    override fun createPsi(
            stub: ObjJVariableNameStub): ObjJVariableNameImpl {
        return ObjJVariableNameImpl(stub, this)
    }

    override fun createStub(
            variableName: ObjJVariableNameImpl, stubElement: StubElement<*>): ObjJVariableNameStub {
        val blockRanges = getBlockRanges(variableName)
        val greatestBlockRange = getGreatestBlockRange(blockRanges)
        val isAssignedTo = variableName.isAssignedTo
        val indexInQualifiedReference = variableName.indexInQualifiedReference
        return ObjJVariableNameStubImpl(
                parent = stubElement,
                variableName = variableName.name,
                isAssignedTo = isAssignedTo,
                indexInQualifiedReference = indexInQualifiedReference,
                containingBlockRanges = blockRanges,
                greatestContainingBlockRange = greatestBlockRange,
                shouldResolve = shouldResolve(variableName.node),
                hasContainingClass = variableName.hasContainingClass
        )
    }

    private fun getBlockRanges(variableName: ObjJVariableName): List<Pair<Int, Int>> {
        return variableName.getScopeBlockRanges()
    }

    private fun getGreatestBlockRange(blockRanges: List<Pair<Int, Int>>): Pair<Int, Int>? {
        if (blockRanges.isEmpty()) {
            return null
        }
        var out: Pair<Int, Int>? = null
        for (blockRange in blockRanges) {
            if (out == null) {
                out = blockRange
                continue
            }
            if (out.getFirst() > blockRange.getFirst()) {
                out = blockRange
            }
        }
        return out
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJVariableNameStub,
            stream: StubOutputStream) {

        stream.writeName(stub.variableName)
        stream.writeInt(stub.containingBlockRanges.size)
        for (pair in stub.containingBlockRanges) {
            stream.writeInt(pair.getFirst())
            stream.writeInt(pair.getSecond())
        }
        val greatestBlock = stub.greatestContainingBlockRange
        if (greatestBlock == null) {
            stream.writeBoolean(false)
        } else {
            stream.writeBoolean(true)
            stream.writeInt(greatestBlock.getFirst())
            stream.writeInt(greatestBlock.getSecond())
        }
        stream.writeBoolean(stub.isAssignedTo)
        stream.writeInt(stub.indexInQualifiedReference)
        stream.writeBoolean(stub.shouldResolve())
        stream.writeBoolean(stub.hasContainingClass)
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parent: StubElement<*>): ObjJVariableNameStub {
        val name = StringRef.toString(stream.readName())!!
        val numBlocks = stream.readInt()
        val blockRanges = ArrayList<Pair<Int, Int>>()
        for (i in 0 until numBlocks) {
            blockRanges.add(Pair(stream.readInt(), stream.readInt()))
        }
        var greatestRange: Pair<Int, Int>? = null
        if (stream.readBoolean()) {
            greatestRange = Pair(stream.readInt(), stream.readInt())
        }
        val isAssignedTo = stream.readBoolean()
        val indexInQualifiedReference = stream.readInt()
        val shouldResolve = stream.readBoolean()
        val hasContainingClass = stream.readBoolean()
        return ObjJVariableNameStubImpl(
                parent = parent,
                variableName = name,
                isAssignedTo = isAssignedTo,
                indexInQualifiedReference = indexInQualifiedReference,
                containingBlockRanges = blockRanges,
                greatestContainingBlockRange = greatestRange,
                shouldResolve = shouldResolve,
                hasContainingClass = hasContainingClass
        )
    }

    override fun indexStub(stub: ObjJVariableNameStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexVariableName(stub, sink)
    }
}