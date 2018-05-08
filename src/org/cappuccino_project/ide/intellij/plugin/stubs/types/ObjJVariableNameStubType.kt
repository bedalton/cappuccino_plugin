package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.intellij.openapi.util.Pair
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJVariableName
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJVariableNameStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub

import java.io.IOException
import java.util.ArrayList
import java.util.Collections

class ObjJVariableNameStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJVariableNameStub, ObjJVariableNameImpl>(debugName, ObjJVariableNameImpl::class.java, ObjJVariableNameStub::class.java) {

    override fun createPsi(
            stub: ObjJVariableNameStub): ObjJVariableNameImpl {
        return ObjJVariableNameImpl(stub, this)
    }

    override fun createStub(
            variableName: ObjJVariableNameImpl, stubElement: StubElement<*>): ObjJVariableNameStub {
        val blockRanges = getBlockRanges(variableName)
        val greatestBlockRange = getGreatestBlockRange(blockRanges)
        return ObjJVariableNameStubImpl(stubElement, variableName.name, blockRanges, greatestBlockRange, shouldResolve(variableName.node))
    }

    private fun getBlockRanges(variableName: ObjJVariableName): List<Pair<Int, Int>> {
        val scopeBlock = ObjJBlockPsiUtil.getScopeBlock(variableName) ?: return emptyList()
        val scopeTextRange = scopeBlock.textRange
        return listOf(Pair(scopeTextRange.startOffset, scopeTextRange.endOffset))
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
        stream.writeBoolean(stub.shouldResolve())
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
        val shouldResolve = stream.readBoolean()
        return ObjJVariableNameStubImpl(parent, name, blockRanges, greatestRange, shouldResolve)
    }

    override fun indexStub(stub: ObjJVariableNameStub, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexVariableName(stub, indexSink)
    }
}
