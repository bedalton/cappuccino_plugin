package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFileTypeFactory;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObjJSelectorReference extends PsiPolyVariantReferenceBase<ObjJSelector> {

    //private static final Logger LOGGER = Logger.getLogger(ObjJSelectorReference.class.getName());

    private final ObjJMethodHeaderDeclaration methodHeaderParent;
    private List<String> classConstraints;
    private final String fullSelector;
    private final Pair<String, String> accessorMethods;

    public ObjJSelectorReference(ObjJSelector element) {
        super(element, TextRange.create(0, element.getTextLength()));
        accessorMethods = getAccessorMethods();
        methodHeaderParent = accessorMethods == null ? ObjJTreeUtil.getParentOfType(element, ObjJMethodHeaderDeclaration.class) : null;
        ObjJMethodCall methodCallParent = ObjJTreeUtil.getParentOfType(element, ObjJMethodCall.class);
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
        if (DumbService.isDumb(myElement.getProject())) {
            return null;
        }
        classConstraints = ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall.getCallTarget());
        return classConstraints;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
    /*
    @NotNull
    @Override
    public Object[] getVariants() {
        if (DumbService.isDumb(myElement.getProject())) {
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
*/
    @Override
    public boolean isReferenceTo(
            @NotNull
                    PsiElement elementToCheck) {
        //LOGGER.log(Level.INFO, "Checking if selector "+elementToCheck.getText()+" is reference to.");
        if (!(elementToCheck instanceof ObjJSelector)) {
            return false;
        }
        if (Objects.equals(((ObjJSelector)elementToCheck).getContainingClassName(), ObjJElementFactory.PLACEHOLDER_CLASS_NAME)) {
            return false;
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodCall.class);
        final String methodCallString = methodCall != null ? methodCall.getSelectorString() : null;
        if (methodHeaderParent != null) {
            return Objects.equals(methodCallString, fullSelector);
        } else if (accessorMethods != null) {
            //LOGGER.log(Level.INFO, "Accessor Methods <"+accessorMethods.getFirst()+","+accessorMethods.getSecond()+">; Method call selector: " + methodCallString);
            return Objects.equals(accessorMethods.getFirst(), methodCallString) ||
                    Objects.equals(accessorMethods.getSecond(), methodCallString);
        }
        if (methodCall != null) {
            return Objects.equals(methodCallString, fullSelector);
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
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.getResult(), myElement.getContainingFile()));
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(myElement);
        if (!selectorResult.isEmpty()) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.getResult(), myElement.getContainingFile()));
        }
        if (!out.isEmpty()) {
            if (classConstraints != null && classConstraints.size() > 0 && !classConstraints.contains(ObjJClassType.UNDETERMINED) && classConstraints.contains(ObjJClassType.ID)) {
                List<PsiElement> tempOut = ArrayUtils.filter(out, (element) -> element instanceof ObjJCompositeElement && classConstraints.contains(ObjJHasContainingClassPsiUtil.getContainingClassName((ObjJCompositeElement) element)));
                if (tempOut.size() > 0) {
                    out = tempOut;
                }
            }
            return PsiElementResolveResult.createResults(out);
        }
        SelectorResolveResult<PsiElement> result = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(myElement, selectorResult.getPossibleContainingClassNames());
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.getResult(), myElement.getContainingFile()));
        }
        result = ObjJSelectorReferenceResolveUtil.getVariableReferences(myElement, result.getPossibleContainingClassNames());
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.getResult(), myElement.getContainingFile()));
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallPartialReferences(myElement, true);
        if (!result.isEmpty()) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.getResult(), myElement.getContainingFile()));
        }
        //LOGGER.log(Level.INFO, "Selector reference failed to multi resolve selector: <"+myElement.getSelectorString(true)+">");
        return PsiElementResolveResult.EMPTY_ARRAY;
    }

    @Nullable
    private Pair<String,String> getAccessorMethods() {
        ObjJAccessorProperty accessorProperty = myElement.getParentOfType(ObjJAccessorProperty.class);
        if (accessorProperty != null) {
            return new Pair<>(accessorProperty.getGetter(), accessorProperty.getSetter());
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = myElement.getParentOfType(ObjJInstanceVariableDeclaration.class);
        if (instanceVariableDeclaration != null) {
            ObjJMethodHeaderStub getter = instanceVariableDeclaration.getGetter();
            ObjJMethodHeaderStub setter = instanceVariableDeclaration.getSetter();
            return new Pair<>(
                    getter != null ? getter.getSelectorString() : null,
                    setter != null ? setter.getSelectorString() : null);
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(String selectorString) {
        return ObjJPsiImplUtil.setName(myElement, selectorString);
    }

}
