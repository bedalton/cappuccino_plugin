package org.cappuccino_project.ide.intellij.plugin.psi.impl;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.FormatterUtil;
import org.cappuccino_project.ide.intellij.plugin.formatting.ObjJFormatContext;
import org.cappuccino_project.ide.intellij.plugin.formatting.ObjJSpacingBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ObjJFormatBlock implements ASTBlock {
    private final ASTNode node;
    private final Alignment alignment;
    private final Indent indent;
    private final Wrap wrap;
    private final ObjJFormatContext context;
    private List<Block> children;

    public ObjJFormatBlock(@NotNull ASTNode node, @Nullable Alignment alignment,
                           @Nullable Indent indent,
                           @Nullable Wrap wrap,
                           @NotNull ObjJFormatContext context) {
        this.node = node;
        this.alignment = alignment;
        this.indent = indent;
        this.wrap = wrap;
        this.context = context;
    }

    @Override
    public ASTNode getNode() {
        return node;
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return node.getTextRange();
    }

    @NotNull
    @Override
    public List<Block> getSubBlocks() {
        return buildChildren();
    }

    @NotNull
    private List<Block> buildChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        return wrap;
    }

    @Nullable
    @Override
    public Indent getIndent() {
        return indent;
    }

    @Nullable
    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Nullable
    @Override
    public Spacing getSpacing(
            @Nullable
                    Block block,
            @NotNull
                    Block block1) {
        return ObjJSpacingBuilder.computeSpacing(this, block, block1, context);
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return null;
    }

    @Override
    public boolean isIncomplete() {
        return FormatterUtil.isIncomplete(node);
    }

    @Override
    public boolean isLeaf() {
        return node.getFirstChildNode() == null;
    }
}
