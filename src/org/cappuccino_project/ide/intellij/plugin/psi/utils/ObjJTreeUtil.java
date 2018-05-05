package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.formatting.ObjJSpacingBuilder;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJComment;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.TokenSets.*;

public class ObjJTreeUtil extends PsiTreeUtil{

    private static final Logger LOGGER = Logger.getLogger(ObjJTreeUtil.class.getName());

    @NotNull
    public static <StubT extends StubElement>  List<StubT> filterStubChildren(StubElement<com.intellij.psi.PsiElement> parent, Class<StubT> stubClass) {
        if (parent == null) {
            return Collections.emptyList();
        }
        return filterStubChildren(parent.getChildrenStubs(), stubClass);
    }

    @NotNull
    public static <StubT extends StubElement>  List<StubT> filterStubChildren(List<StubElement> children, Class<StubT> stubClass) {
        if (children == null) {
            return Collections.emptyList();
        }
        return ArrayUtils.filter(children, stubClass);
    }

    @NotNull
    public static List<PsiElement> getChildrenOfType(@NotNull PsiElement element, @NotNull IElementType iElementType) {
        List<PsiElement> out = new ArrayList<>();
        for (PsiElement child : element.getChildren()) {
            //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">, is of type  <"+child.getNode().getElementType().toString()+">");
            if (child.getNode().getElementType() == iElementType) {
                //LOGGER.log(Level.INFO, "Child element <"+child.getText()+">is of token type: <"+iElementType.toString()+">");
                out.add(child);
            }
        }
        return out;
    }

    @Nullable
    public static ASTNode getNextNode(PsiElement compositeElement) {
        return compositeElement.getNode().getTreeNext();
    }

    @Nullable
    public static IElementType getNextNonEmptyNodeType(PsiElement compositeElement, boolean ignoreLineTerminator) {
        ASTNode next = getNextNonEmptyNode(compositeElement,ignoreLineTerminator);
        return next != null ? next.getElementType() : null;
    }

    @Nullable
    public static PsiElement getPreviousNonEmptySibling(PsiElement psiElement, boolean ignoreLineTerminator) {
        ASTNode node = getPreviousNonEmptyNode(psiElement, ignoreLineTerminator);
        return node != null ? node.getPsi() : null;
    }

    @Nullable
    public static PsiElement getNextNonEmptySibling(PsiElement psiElement, boolean ignoreLineTerminator) {
        ASTNode node = getNextNonEmptyNode(psiElement, ignoreLineTerminator);
        return node != null ? node.getPsi() : null;
    }

    @Nullable
    public static ASTNode getPreviousNonEmptyNode(PsiElement compositeElement, boolean ignoreLineTerminator) {
        ASTNode out = compositeElement != null ? compositeElement.getNode().getTreePrev() : null;
        while (shouldSkipNode(out, ignoreLineTerminator)) {
            if (out.getTreePrev() == null) {
                out = TreeUtil.prevLeaf(out);
            } else {
                out = out.getTreePrev();
            }
            if (out == null) {
                return null;
            }
            //LOGGER.log(Level.INFO, "<"+compositeElement.getText()+">NextNode "+out.getText()+" ElementType is <"+out.getElementType().toString()+">");
        }
        return out;
    }

    @Nullable
    public static ASTNode getNextNonEmptyNode(PsiElement compositeElement, boolean ignoreLineTerminator) {
        ASTNode out = compositeElement != null ? compositeElement.getNode().getTreeNext() : null;
        while (shouldSkipNode(out, ignoreLineTerminator)) {
            if (out.getTreeNext() == null) {
                out = TreeUtil.nextLeaf(out);
            } else {
                out = out.getTreeNext();
            }
            if (out == null) {
                return null;
            }
            //LOGGER.log(Level.INFO, "<"+compositeElement.getText()+">NextNode "+out.getText()+" ElementType is <"+out.getElementType().toString()+">");
        }
        return out;
    }

    public static boolean isWhitespaceOrEmpty(@Nullable PsiElement psiElement) {
        return psiElement == null || psiElement.getTextLength() == 0 || psiElement.getNode().getElementType() == TokenType.WHITE_SPACE;
    }

    public static boolean isWhitespaceOrEmpty(@Nullable ASTNode node) {
        return node == null || node.getTextLength() == 0 || node.getElementType() == TokenType.WHITE_SPACE;
    }

    private static boolean shouldSkipNode(ASTNode out, boolean ignoreLineTerminator) {
        return out != null && ((ignoreLineTerminator && out.getElementType() == ObjJTypes.ObjJ_LINE_TERMINATOR) || isWhitespaceOrEmpty(out) || out.getPsi() instanceof PsiErrorElement);
    }


    @Nullable
    public static <PsiT extends PsiElement> PsiT getSharedContextOfType(@Nullable PsiElement psiElement1, @Nullable PsiElement psiElement2, Class<PsiT> sharedClass) {
        if (psiElement1 == null || psiElement2 == null) {
            return null;
        }
        final PsiElement sharedContext = findCommonContext(psiElement1, psiElement2);
        if (sharedContext == null) {
            return null;
        }
        if (sharedClass.isInstance(sharedContext)) {
            return sharedClass.cast(sharedContext);
        }
        return getParentOfType(sharedContext, sharedClass);
    }

    public static <PsiT extends PsiElement> boolean hasSharedContextOfType(@Nullable PsiElement psiElement1, @Nullable PsiElement psiElement2, Class<PsiT> sharedClass) {
        return getSharedContextOfType(psiElement1, psiElement2, sharedClass) != null;
    }

    public static <PsiT extends PsiElement> boolean siblingOfTypeOccursAtLeastOnceBefore(@Nullable PsiElement psiElement, @NotNull Class<PsiT> siblingElementClass) {
        if (psiElement == null) {
            return false;
        }
        while (psiElement.getPrevSibling() != null) {
            psiElement = psiElement.getPrevSibling();
            if (siblingElementClass.isInstance(psiElement)) {
                return true;
            }
        }
        return false;
    }


    public static Spacing getLineBreak(Integer minLineFeeds,
                          boolean keepLineBreaks,
                          Integer keepBlankLines) {
        if (minLineFeeds == null) {
            minLineFeeds = 1;
        }
        if (keepBlankLines == null) {
            keepBlankLines = 1;
        }
        //return ObjJSpacingBuilder.createSpacingBuilder(0, Integer.MAX_VALUE, minLineFeeds, keepLineBreaks, keepBlankLines);

        return Spacing.createSafeSpacing(keepLineBreaks, keepBlankLines);
    }


    public static boolean hasLineBreakAfterInSameParent(@Nullable ASTNode node) {
        node = node != null ? node.getTreeNext() : null;
        return node != null &&  isWhitespaceWithLineBreak(TreeUtil.findLastLeaf(node));
    }

    public static boolean hasLineBreakBreakBeforeInSameParent(@Nullable ASTNode node) {
        node = node != null ? node.getTreePrev() : null;
        return node != null &&  isWhitespaceWithLineBreak(TreeUtil.findLastLeaf(node));
    }

    public static boolean isWhitespaceWithLineBreak(@Nullable ASTNode node) {
        return node != null && node.getElementType() == TokenType.WHITE_SPACE && node.textContains('\n');
    }

    public static boolean needsBlankLineBetweenItems(IElementType elementType1, IElementType elementType2) {
        if (COMMENTS.contains(elementType1) || COMMENTS.contains(elementType2)) {
            return false;
        }

        // Allow to keep consecutive runs of `use`, `const` or other "one line" items without blank lines
        if (elementType1 == elementType2 && ONE_LINE_ITEMS.contains(elementType1)) {
            return false;
        }

        return elementType1 != ObjJTypes.ObjJ_LINE_TERMINATOR;
    }

    @Nullable
    public static PsiElement getThisOrPreviousNonCommentElement(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element instanceof ObjJComment) {
            return PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class, PsiComment.class);
        }
        return element;
    }


    @Nullable
    public static PsiElement getThisOrNextNonCommentElement(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element instanceof ObjJComment) {
            return PsiTreeUtil.skipSiblingsForward(element, PsiWhiteSpace.class, PsiComment.class);
        }
        return element;
    }

}
