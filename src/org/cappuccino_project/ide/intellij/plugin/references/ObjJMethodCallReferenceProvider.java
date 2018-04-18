package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ObjJMethodCallReferenceProvider extends PsiPolyVariantReferenceBase<ObjJHasMethodSelector> {

    private final String selector;
    private final String containingClass;


    public ObjJMethodCallReferenceProvider(ObjJHasMethodSelector psiElement) {
        super(psiElement, TextRange.create(0, psiElement.getTextLength()));
        selector = psiElement.getSelectorString();
        this.containingClass = ObjJPsiImplUtil.getContainingClassName(psiElement);
    }


    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        if (DumbService.isDumb(myElement.getProject())) {
            return ResolveResult.EMPTY_ARRAY;
        }
        List<PsiElement> result = new ArrayList<>(ObjJUnifiedMethodIndex.getInstance().get(selector, myElement.getProject()));
        if (result.size() > 0) {
            return PsiElementResolveResult.createResults(result);
        }
        return PsiElementResolveResult.createResults(ObjJPsiImplUtil.getSelectorLiteralReference(myElement));
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        if (DumbService.isDumb(myElement.getProject())) {
            return new Object[0];
        }
        //final List<ObjJMethodHeaderDeclaration> result = ObjJUnifiedMethodIndex.getInstance().getKeysByPattern(selector+"(.*)", myElement.getProject());
        //return result.toArray();
        return new Object[0];
    }

    @Override
    public PsiElement handleElementRename(String selectorString) {
        return ObjJPsiImplUtil.setName(myElement, selectorString);
    }
}
