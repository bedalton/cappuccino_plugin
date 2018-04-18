package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasBlockStatements;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJBlockPsiUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJBlockPsiUtil.class.getName());

    /**
     * Gets first child of type in block or child blocks, without filter
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param <T>        child element type
     * @return first child of type in block or child blocks
     */
    @Nullable
    public static <T extends PsiElement> T getBlockChildOfType(ObjJBlock firstBlock, Class<T> aClass) {
        List<T> out = getBlockChildrenOfType(firstBlock, aClass, true, null, true, -1);
        return !out.isEmpty() ? out.get(0) : null;
    }

    /**
     * Gets first child of type in block or child blocks, with filter
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param filter     element filter
     * @param <T>        child element type
     * @return first child element matching element class and filter criteria
     */
    @Nullable
    public static <T extends PsiElement> T getBlockChildOfType(ObjJBlock firstBlock, Class<T> aClass,
                                                               @NotNull
                                                                       ArrayUtils.Filter<T> filter) {
        List<T> out = getBlockChildrenOfType(firstBlock, aClass, true, filter, true, -1);
        return !out.isEmpty() ? out.get(0) : null;
    }

    /**
     * Gets all block children of a type, potentially recursively
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param recursive  whether to check child blocks for matching child elements
     * @param <T>        child element type
     * @return list of child elements matching type
     */
    @NotNull
    public static <T extends PsiElement> List<T> getBlockChildrenOfType(
            @Nullable
                    ObjJBlock firstBlock,
            @NotNull
                    Class<T> aClass, boolean recursive) {
        return getBlockChildrenOfType(firstBlock, aClass, recursive, null, false, -1);
    }

    /**
     * Gets all block children of a type, potentially recursively
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param recursive  whether to check child blocks for matching child elements
     * @param <T>        child element type
     * @return list of child elements matching type
     */
    @NotNull
    public static <T extends PsiElement> List<T> getBlockChildrenOfType(
            @Nullable
                    ObjJBlock firstBlock,
            @NotNull
                    Class<T> aClass, boolean recursive,
            int offset) {
        return getBlockChildrenOfType(firstBlock, aClass, recursive, null, false, offset);
    }

    /**
     * Gets list of block children of type using a filter
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param recursive  whether to check child blocks for matching child elements
     * @param filter     element filter
     * @param <T>        type of child element to work on
     */
    @NotNull
    public static <T extends PsiElement> List<T> getBlockChildrenOfType(
            @Nullable
                    ObjJBlock firstBlock,
            @NotNull
                    Class<T> aClass, boolean recursive,
            @NotNull
                    ArrayUtils.Filter<T> filter,
            int offset) {
        return getBlockChildrenOfType(firstBlock, aClass, recursive, filter, false, offset);
    }
    /**
     * Gets list of block children of type using a filter
     *
     * @param firstBlock outermost block to get children from
     * @param aClass     class of items to filter by
     * @param recursive  whether to check child blocks for matching child elements
     * @param filter     element filter
     * @param <T>        type of child element to work on
     */
    @NotNull
    public static <T extends PsiElement> List<T> getBlockChildrenOfType(
            @Nullable
                    ObjJBlock firstBlock,
            @NotNull
                    Class<T> aClass, boolean recursive,
            @NotNull
                    ArrayUtils.Filter<T> filter) {
        return getBlockChildrenOfType(firstBlock, aClass, recursive, filter, false, -1);
    }

    /**
     * Gets all block children of a type
     * Can filter if desired, and can return first matching item as a singleton list
     *
     * @param firstBlock  outermost block to get children from
     * @param aClass      class of items to filter by
     * @param recursive   whether to check child blocks for matching child elements
     * @param filter      element filter
     * @param returnFirst return first item as singleton list
     * @param <T>         type of child element to work on
     * @return list of items matching class type and filter if applicable.
     */
    private static <T extends PsiElement> List<T> getBlockChildrenOfType(
            @Nullable
                    ObjJBlock firstBlock,
            @NotNull
                    Class<T> aClass, boolean recursive,
            @Nullable
                    ArrayUtils.Filter<T> filter,
            boolean returnFirst,
            int offset) {
        if (firstBlock == null) {
            return Collections.emptyList();
        }
        List<ObjJBlock> currentBlocks = new ArrayList<>();
        currentBlocks.add(firstBlock);
        List<T> out = new ArrayList<>();
        List<T> tempElements;
        do {
            List<ObjJBlock> nextBlocks = new ArrayList<>();
            for (ObjJBlock block : currentBlocks) {
                if (offset >= 0 && block.getTextRange().getStartOffset() >= offset) {
                    continue;
                }
                tempElements = ObjJTreeUtil.getChildrenOfTypeAsList(block, aClass);
                if (filter != null) {
                    for (T element : tempElements) {
                        if (filter.check(element)) {
                            if (returnFirst) {
                                return Collections.singletonList(element);
                            }
                            if (offset < 0 || element.getTextRange().getStartOffset() < offset) {
                                out.add(element);
                            }
                        }
                    }
                } else if (returnFirst && tempElements.size() > 0) {
                    for (T element : tempElements) {
                        if (offset < 0 || element.getTextRange().getStartOffset() < offset) {
                            return Collections.singletonList(element);
                        }
                    }
                } else {
                    for (T element : tempElements) {
                        if (offset < 0 || element.getTextRange().getStartOffset() < offset) {
                            out.add(element);
                        }
                    }
                }
                if (recursive) {
                    for (ObjJHasBlockStatements hasBlockStatements : ObjJTreeUtil.getChildrenOfTypeAsList(block, ObjJHasBlockStatements.class)) {
                        //LOGGER.log(Level.INFO, "Looping block recursive with text: <" + hasBlockStatements.getText() + ">");
                        nextBlocks.addAll(hasBlockStatements.getBlockList());
                    }
                }
            }
            currentBlocks = nextBlocks;
        } while (!currentBlocks.isEmpty());
        return out;
    }


    @NotNull
    public static <T extends PsiElement> List<T> getParentBlockChildrenOfType(PsiElement psiElement, Class<T> aClass, boolean recursive) {
        ObjJBlock block = ObjJTreeUtil.getParentOfType(psiElement, ObjJBlock.class);
        if (block == null) {
            return Collections.emptyList();
        }
        List<T> out = new ArrayList<>();
        do {
            out.addAll(ObjJTreeUtil.getChildrenOfTypeAsList(block, aClass));
            block = ObjJTreeUtil.getParentOfType(block, ObjJBlock.class);
        } while (block != null && recursive);
        return out;
    }

    @NotNull
    public static List<ObjJBlock> getIterationBlockList(ObjJIterationStatement iterationStatement) {
        if (iterationStatement.getBlock() != null) {
            return Collections.singletonList(iterationStatement.getBlock());
        }
        return Collections.emptyList();
    }


    @NotNull
    public static List<ObjJBlock> getTryStatementBlockList(ObjJTryStatement tryStatement) {
        List<ObjJBlock> out = new ArrayList<>();
        out.add(tryStatement.getBlock());
        if (tryStatement.getCatchProduction() != null) {
            out.add(tryStatement.getCatchProduction().getBlock());
        }
        if (tryStatement.getFinallyProduction() != null) {
            out.add(tryStatement.getFinallyProduction().getBlock());
        }
        return out;
    }

    @Nullable
    public static ObjJBlock getBlock(ObjJExpr expr) {
        if (expr.getLeftExpr() != null) {
            if (expr.getLeftExpr().getFunctionLiteral() != null) {
                return expr.getLeftExpr().getFunctionLiteral().getBlock();
            }
        }
        return null;
    }
}
