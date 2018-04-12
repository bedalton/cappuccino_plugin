package org.cappuccino_project.ide.intellij.plugin.psi.impl;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.usageView.UsageViewUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.logging.Level;

public class ObjJCompositeElementImpl extends ASTWrapperPsiElement implements ObjJCompositeElement {
    public ObjJCompositeElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getNode().getElementType().toString();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return processDeclarations(this, processor, state, lastParent, place);
    }

    private static boolean processDeclarations(
            @NotNull
                    PsiElement element,
            @NotNull
                    PsiScopeProcessor processor,
            @NotNull
                    ResolveState state, PsiElement lastParent,
            @NotNull
                    PsiElement place) {
        return processor.execute(element, state) && ObjJResolveUtil.processChildren(element, processor, state, lastParent, place);
    }

    @Override
    public ItemPresentation getPresentation() {
        LOGGER.log(Level.INFO, "Get Presentation <"+this.getNode().getElementType().toString()+">");
        final String text = UsageViewUtil.createNodeText(this);
        if (text != null) {
            return new ItemPresentation() {
                @NotNull
                @Override
                public String getPresentableText() {
                    return text;
                }

                @NotNull
                @Override
                public String getLocationString() {
                    return getContainingFile().getName();
                }

                @Nullable
                @Override
                public Icon getIcon(boolean b) {
                    return ObjJCompositeElementImpl.this.getIcon(0);
                }
            };
        }
        return super.getPresentation();
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getChildOfType(Class<PsiT> childClass) {
        return ObjJTreeUtil.getChildOfType(this, childClass);
    }

    @Override
    @NotNull
    public <PsiT extends PsiElement> List<PsiT> getChildrenOfType(Class<PsiT> childClass) {
        return ObjJTreeUtil.getChildrenOfTypeAsList(this, childClass);
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getParentOfType(Class<PsiT> parentClass) {
        return ObjJTreeUtil.getParentOfType(this, parentClass);
    }

    @Override
    public ObjJFile getContainingObjJFile() {
        PsiFile file = getContainingFile();
        if (file instanceof ObjJFile) {
            return (ObjJFile)file;
        }
        return null;
    }
}