package org.cappuccino_project.ide.intellij.plugin.references;

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
import java.util.logging.Level;
import java.util.logging.Logger;

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
        List<ObjJClassName> classNames = new ArrayList<>();
        Collection<ObjJClassDeclarationElement> classDeclarations = ObjJClassDeclarationsIndex.getInstance().get(className, myElement.getProject());
        if (classDeclarations.isEmpty()) {
         //   LOGGER.log(Level.INFO, "Failed to find resolve class declaration for class: <"+className+">");
            return new ResolveResult[0];
        }

        for (ObjJClassDeclarationElement classDec : classDeclarations) {
            ObjJClassName classDecName = classDec.getClassName();
            if (classDecName != null && !classDec.isEquivalentTo(myElement) && !classDecName.getText().isEmpty()) {
                classNames.add(classDecName);
            }
        }
        return PsiElementResolveResult.createResults(classNames);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
