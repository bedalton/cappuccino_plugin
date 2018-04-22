package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjJClassNameReference extends PsiPolyVariantReferenceBase<ObjJClassName> {
    private final String className;
    public ObjJClassNameReference(ObjJClassName element) {
        super(element, TextRange.create(0, element.getTextLength()));
        this.className = element.getText();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        if (className == null) {
            return new ResolveResult[0];
        }
        if (DumbService.isDumb(myElement.getProject())) {
            return ResolveResult.EMPTY_ARRAY;
        }
        List<ObjJClassName> classNames = new ArrayList<>();
        Collection<ObjJClassDeclarationElement> classDeclarations = ObjJClassDeclarationsIndex.getInstance().get(className, myElement.getProject());
        if (classDeclarations.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }

        for (ObjJClassDeclarationElement classDec : classDeclarations) {
            ObjJClassName classDecName = classDec.getClassName();
            if (classDecName != null && !classDecName.getText().isEmpty() && !classDecName.isEquivalentTo(myElement) && classDec.shouldResolve()) {
                classNames.add(classDecName);
            }
        }
        return PsiElementResolveResult.createResults(classNames);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<Object> keys = new ArrayList<>(ObjJClassDeclarationsIndex.getInstance().getAllResolveableKeys(myElement.getProject()));
        return keys.toArray();
    }
}
