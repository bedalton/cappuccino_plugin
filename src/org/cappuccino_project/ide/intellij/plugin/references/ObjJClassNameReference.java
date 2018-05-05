package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInheritedProtocolList;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        boolean mustBeMain = false;
        if (myElement.getParent() instanceof ObjJInheritedProtocolList) {
            List<ObjJProtocolDeclaration>  protocolDeclarations = new ArrayList<>(ObjJProtocolDeclarationsIndex.getInstance().get(className, myElement.getProject()));
            for (ObjJProtocolDeclaration protocolDeclaration : protocolDeclarations) {

            }
        } else if (myElement.getParent() instanceof ObjJImplementationDeclaration ){
            Collection<ObjJImplementationDeclaration> implementationDeclarations = ObjJImplementationDeclarationsIndex.getInstance().get(className, myElement.getProject());
            mustBeMain = ((ObjJImplementationDeclaration) myElement.getParent()).isCategory();
        }
        List<ObjJClassDeclarationElement> classDeclarations = ObjJClassDeclarationsIndex.getInstance().get(className, myElement.getProject());
        if (classDeclarations.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }
        for (ObjJClassDeclarationElement classDeclarationElement : classDeclarations) {
            ObjJClassName classDeclarationName = classDeclarationElement.getClassName();
            if (shouldAdd(classDeclarationElement)) {
                if (mustBeMain) {
                    if (classDeclarationElement instanceof ObjJImplementationDeclaration && ((ObjJImplementationDeclaration) classDeclarationElement).isCategory()) {
                        return PsiElementResolveResult.createResults(Collections.singleton(classDeclarationName));
                    }
                } else {
                    classNames.add(classDeclarationName);
                }
            }
        }
        return PsiElementResolveResult.createResults(classNames);
    }

    private boolean shouldAdd(ObjJClassDeclarationElement classDeclarationElement) {
        ObjJClassName classDecName = classDeclarationElement.getClassName();
        return (classDecName != null && !classDecName.getText().isEmpty() && !classDecName.isEquivalentTo(myElement) && classDeclarationElement.shouldResolve());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<Object> keys = new ArrayList<>(ObjJClassDeclarationsIndex.getInstance().getAllResolveableKeys(myElement.getProject()));
        return keys.toArray();
    }
}
