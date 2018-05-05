package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.intellij.openapi.util.Pair;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJVariableName;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJVariableNameStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJVariableNameStubType extends ObjJStubElementType<ObjJVariableNameStub, ObjJVariableNameImpl> {

    ObjJVariableNameStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJVariableNameImpl.class, ObjJVariableNameStub.class);
    }

    @Override
    public ObjJVariableNameImpl createPsi(
            @NotNull
                    ObjJVariableNameStub stub) {
        return new ObjJVariableNameImpl(stub, this);
    }

    @NotNull
    @Override
    public ObjJVariableNameStub createStub(
            @NotNull
                    ObjJVariableNameImpl variableName, StubElement stubElement) {
        List<Pair<Integer,Integer>> blockRanges = getBlockRanges(variableName);
        Pair<Integer, Integer> greatestBlockRange = getGreatestBlockRange(blockRanges);
        return new ObjJVariableNameStubImpl(stubElement, variableName.getName(), blockRanges, greatestBlockRange, shouldResolve(variableName.getNode()));
    }

    @NotNull
    private List<Pair<Integer,Integer>> getBlockRanges(@NotNull ObjJVariableName variableName) {
        ObjJBlock scopeBlock = ObjJBlockPsiUtil.getScopeBlock(variableName);
        if (scopeBlock == null) {
            return Collections.emptyList();
        }
        TextRange scopeTextRange = scopeBlock.getTextRange();
        return Collections.singletonList(new Pair<>(scopeTextRange.getStartOffset(), scopeTextRange.getEndOffset()));
    }

    @Nullable
    private Pair<Integer,Integer> getGreatestBlockRange(@NotNull List<Pair<Integer,Integer>> blockRanges) {
        if (blockRanges.isEmpty()) {
            return null;
        }
        Pair<Integer, Integer> out = null;
        for (Pair<Integer,Integer> blockRange : blockRanges) {
            if (out == null) {
                out = blockRange;
                continue;
            }
            if (out.getFirst() > blockRange.getFirst()) {
                out = blockRange;
            }
        }
        return out;
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJVariableNameStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {

        stream.writeName(stub.getVariableName());
        stream.writeInt(stub.getContainingBlockRanges().size());
        for (Pair<Integer, Integer> pair : stub.getContainingBlockRanges()) {
            stream.writeInt(pair.getFirst());
            stream.writeInt(pair.getSecond());
        }
        Pair<Integer, Integer> greatestBlock = stub.getGreatestContainingBlockRange();
        if (greatestBlock == null) {
            stream.writeBoolean(false);
        } else {
            stream.writeBoolean(true);
            stream.writeInt(greatestBlock.getFirst());
            stream.writeInt(greatestBlock.getSecond());
        }
        stream.writeBoolean(stub.shouldResolve());
    }

    @NotNull
    @Override
    public ObjJVariableNameStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement parent) throws IOException {
        final String name = StringRef.toString(stream.readName());
        assert name != null;
        int numBlocks = stream.readInt();
        List<Pair<Integer, Integer>> blockRanges = new ArrayList<>();
        for (int i=0;i<numBlocks;i++) {
            blockRanges.add(new Pair<>(stream.readInt(), stream.readInt()));
        }
        Pair<Integer,Integer> greatestRange = null;
        if (stream.readBoolean()) {
            greatestRange = new Pair<>(stream.readInt(), stream.readInt());
        }
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJVariableNameStubImpl(parent, name, blockRanges, greatestRange, shouldResolve);
    }

    @Override
    public void indexStub(@NotNull ObjJVariableNameStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexVariableName(stub, indexSink);
    }
}
