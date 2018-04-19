package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.cappuccino_project.ide.intellij.plugin.indices.*;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJSelectorReference extends PsiPolyVariantReferenceBase<ObjJSelector> {

    private static final Logger LOGGER = Logger.getLogger(ObjJSelectorReference.class.getName());

    private final ObjJMethodHeaderDeclaration methodHeaderParent;
    private final ObjJMethodCall methodCallParent;
    private List<String> classConstraints;
    private final String fullSelector;

    public ObjJSelectorReference(ObjJSelector element) {
        super(element, TextRange.create(0, element.getTextLength()));
        methodHeaderParent = ObjJTreeUtil.getParentOfType(element, ObjJMethodHeaderDeclaration.class);
        methodCallParent = ObjJTreeUtil.getParentOfType(element, ObjJMethodCall.class);
        fullSelector = methodHeaderParent != null ? methodHeaderParent.getSelectorString() : methodCallParent != null ? methodCallParent.getSelectorString() : null;
        classConstraints = getClassConstraints();
        //LOGGER.log(Level.INFO, "Creating selector resolver for selector <"+fullSelector+">;");
    }

    @Nullable
    private List<String> getClassConstraints() {
        if (classConstraints != null) {
            return classConstraints;
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(myElement, ObjJMethodCall.class);
        if (methodCall == null) {
         //   LOGGER.log(Level.INFO, "Selector is not in method call.");
            return null;
        }

        classConstraints = ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall.getCallTarget());
        if (!classConstraints.isEmpty()) {
            LOGGER.log(Level.INFO, "Call target: <"+methodCallParent.getCallTarget().getText()+"> is possibly of type: ["+ ArrayUtils.join(classConstraints)+"]");
        } else {
         //   LOGGER.log(Level.INFO, "Failed to infer call target type for target named <"+methodCallParent.getCallTarget().getText()+">.");
        }
        return classConstraints;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        if (true || DumbService.isDumb(myElement.getProject())) {
            return new Object[0];
        }
        LOGGER.log(Level.INFO, "GetVariants");
        String selectorUpToSelf = ObjJPsiImplUtil.getSelectorUntil(myElement, false);
        if (selectorUpToSelf == null) {
            return new Object[0];
        }
        List<ObjJMethodHeaderDeclaration> headers = ObjJMethodFragmentIndex.getInstance().get(selectorUpToSelf, myElement.getProject());
        List<Object> results = new ArrayList<>();
        ObjJHasMethodSelector parent = myElement.getParentOfType(ObjJHasMethodSelector.class);
        int selectorIndex = parent != null ? parent.getSelectorList().indexOf(myElement) : 0;
        ObjJMethodHeader parentHeader = ObjJTreeUtil.getParentOfType(myElement, ObjJMethodHeader.class);
        if (parentHeader != null) {
            ObjJClassDeclarationElement classDeclarationElement = myElement.getContainingClass();
            if (classDeclarationElement == null || classDeclarationElement.getInheritedProtocolList() == null) {
                return new Object[0];
            }
            for (ObjJClassName className : classDeclarationElement.getInheritedProtocolList().getClassNameList()) {
                for (ObjJMethodHeader header : ObjJClassMethodIndex.getInstance().get(className.getText(), className.getProject())) {
                    if (!(header.getContainingClass() instanceof ObjJProtocolDeclaration)) {
                        continue;
                    }
                    if (header.getSelectorString().startsWith(selectorUpToSelf)) {
                        continue;
                    }
                    results.add(header.getSelectorStrings().get(selectorIndex));
                }
            }
        }
        if (selectorIndex == 0) {
            results.addAll(ObjJInstanceVariablesByNameIndex.getInstance().getAll(myElement.getProject()));
        }
        while (headers.size() > 0) {
            for (ObjJMethodHeaderDeclaration header : headers) {
                ProgressIndicatorProvider.checkCanceled();
                final List<ObjJSelector> selectors = header.getSelectorList();
                if (selectors.size() > selectorIndex) {
                    results.add(header.getSelectorList().get(selectorIndex));
                }
            }
        }
        return results.toArray();
    }

    /*
        @Nullable
        @Override
        public PsiElement resolve() {
         //   LOGGER.log(Level.INFO, "Resolving Selector");
            ResolveResult[] result = multiResolve(false);
            return result.length > 0 ? result[0].getElement() : null;
        }
    */
    @Override
    public boolean isReferenceTo(
            @NotNull
                    PsiElement elementToCheck) {
        //LOGGER.log(Level.INFO, "Checking if selector is reference to.");
        if (!(elementToCheck instanceof ObjJSelector)) {
            return false;
        }
        if (methodHeaderParent != null) {
            ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodCall.class);
            if (methodCall != null) {
                String callTargets = methodCall.getCallTargetText();
            }
            return methodCall != null && methodCall.getSelectorString().equals(fullSelector);
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodCall.class);
        if (methodCall != null) {
            return methodCall.getSelectorString().equals(fullSelector);
        }
        ObjJMethodHeaderDeclaration declaration = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodHeaderDeclaration.class);
        return declaration != null && declaration.getSelectorString().equals(fullSelector);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        //Get Basic
        SelectorResolveResult<ObjJSelector> selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(myElement);
        List<PsiElement> out = new ArrayList<>();
        if (!selectorResult.isEmpty()) {
            out.addAll(selectorResult.getResult());
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(myElement);
        if (!selectorResult.isEmpty()) {
            out.addAll(selectorResult.getResult());
        }
        if (!out.isEmpty()) {
            if (classConstraints != null && classConstraints.size() > 0 && !classConstraints.contains(ObjJClassType.UNDETERMINED)) {
                List<PsiElement> tempOut = ArrayUtils.filter(out, (element) -> element instanceof ObjJCompositeElement && classConstraints.contains(ObjJHasContainingClassPsiUtil.getContainingClassName((ObjJCompositeElement) element)));
                if (tempOut.size() > 0) {
                    out = tempOut;
                }
            }
            return PsiElementResolveResult.createResults(out);
        }
        SelectorResolveResult<PsiElement> result = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(myElement, selectorResult.getPossibleContainingClassNames());
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(result.getResult());
        }
        result = ObjJSelectorReferenceResolveUtil.getVariableReferences(myElement, result.getPossibleContainingClassNames());
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(result.getResult());
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallPartialReferences(myElement, true);
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(selectorResult.getResult());
        }
        //LOGGER.log(Level.INFO, "Selector reference failed to multi resolve selector: <"+myElement.getSelectorString(true)+">");
        return PsiElementResolveResult.EMPTY_ARRAY;
    }


    @Override
    public PsiElement handleElementRename(String selectorString) {
        return ObjJPsiImplUtil.setName(myElement, selectorString);
    }

}
