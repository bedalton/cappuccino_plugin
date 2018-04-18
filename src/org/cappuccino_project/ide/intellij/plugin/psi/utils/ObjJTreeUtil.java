package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.velocity.runtime.parser.node.ASTMap;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static PsiElement getPreviousSiblingOfType(@NotNull PsiElement element, @NotNull IElementType siblingElementType) {
        while (element.getPrevSibling() != null) {
            element = element.getPrevSibling();
            if (hasElementType(element, siblingElementType)) {
                return element;
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement getNextSiblingOfType(@NotNull PsiElement element, @NotNull IElementType siblingElementType) {
        while (element.getNextSibling() != null) {
            element = element.getNextSibling();
            if (hasElementType(element, siblingElementType)) {
                return element;
            }
        }
        return null;
    }

    private static boolean hasElementType(PsiElement element, IElementType elementType) {
        return element.getNode().getElementType() == elementType;
    }

    @Nullable
    public static IElementType getNextNodeType(PsiElement compositeElement) {
        ASTNode astNode = getNextNode(compositeElement);
        return astNode != null ? astNode.getElementType() : null;
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

    private static boolean shouldSkipNode(ASTNode out, boolean ignoreLineTerminator) {
        return out != null && ((ignoreLineTerminator && out.getElementType() == ObjJTypes.ObjJ_LINE_TERMINATOR) || out.getElementType() == com.intellij.psi.TokenType.WHITE_SPACE || out.getPsi() instanceof PsiErrorElement);
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

}
