package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFileTypeFactory
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList
import java.util.Objects

class ObjJSelectorReference(element: ObjJSelector) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    //private static final Logger LOGGER = Logger.getLogger(ObjJSelectorReference.class.getName());

    private val methodHeaderParent: ObjJMethodHeaderDeclaration<*>?
    private var classConstraints: List<String>? = null
    private val fullSelector: String?
    private val accessorMethods: Pair<String, String>?

    init {
        accessorMethods = getAccessorMethods()
        methodHeaderParent = if (accessorMethods == null) element.getParentOfType(ObjJMethodHeaderDeclaration::class.java) else null
        val methodCallParent = element.getParentOfType(ObjJMethodCall::class.java)
        fullSelector = methodHeaderParent?.selectorString ?: methodCallParent?.selectorString
        classConstraints = getClassConstraints()
        //LOGGER.log(Level.INFO, "Creating selector resolver for selector <"+fullSelector+">;");
    }

    private fun getClassConstraints(): List<String>? {
        if (classConstraints != null) {
            return classConstraints
        }
        val methodCall = myElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //   LOGGER.log(Level.INFO, "Selector is not in method call.");
                return null
        if (DumbService.isDumb(myElement.project)) {
            return null
        }
        classConstraints = ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall.callTarget)
        return classConstraints
    }

    override fun getVariants(): Array<Any> {
        return arrayOfNulls(0)
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
    override fun isReferenceTo(
            elementToCheck: PsiElement): Boolean {
        //LOGGER.log(Level.INFO, "Checking if selector "+elementToCheck.getText()+" is reference to.");
        if (elementToCheck !is ObjJSelector) {
            return false
        }
        if (elementToCheck.containingClassName == ObjJElementFactory.PLACEHOLDER_CLASS_NAME) {
            return false
        }
        val methodCall = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodCall::class.java)
        val methodCallString = methodCall?.selectorString
        if (methodHeaderParent != null) {
            return methodCallString == fullSelector
        } else if (accessorMethods != null) {
            //LOGGER.log(Level.INFO, "Accessor Methods <"+accessorMethods.getFirst()+","+accessorMethods.getSecond()+">; Method call selector: " + methodCallString);
            return accessorMethods.getFirst() == methodCallString || accessorMethods.getSecond() == methodCallString
        }
        if (methodCall != null) {
            return methodCallString == fullSelector
        }
        val declaration = ObjJTreeUtil.getParentOfType(elementToCheck, ObjJMethodHeaderDeclaration<*>::class.java)
        return declaration != null && declaration.selectorString == fullSelector
    }

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        //Get Basic
        var selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(myElement)
        var out: MutableList<PsiElement> = ArrayList()
        if (!selectorResult.isEmpty) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(myElement)
        if (!selectorResult.isEmpty) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        if (!out.isEmpty()) {
            if (classConstraints != null && classConstraints!!.size > 0 && !classConstraints!!.contains(ObjJClassType.UNDETERMINED) && classConstraints!!.contains(ObjJClassType.ID)) {
                val tempOut = out.filter { element -> element is ObjJCompositeElement && classConstraints!!.contains(ObjJHasContainingClassPsiUtil.getContainingClassName(element)) }
                if (tempOut.isNotEmpty()) {
                    out = tempOut as MutableList<PsiElement>
                }
            }
            return PsiElementResolveResult.createResults(out)
        }
        var result = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(myElement, selectorResult.possibleContainingClassNames)
        if (!result.isEmpty) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        result = ObjJSelectorReferenceResolveUtil.getVariableReferences(myElement, result.possibleContainingClassNames)
        if (!result.isEmpty) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallPartialReferences(myElement, true)
        return if (!result.isEmpty) {
            PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        } else PsiElementResolveResult.EMPTY_ARRAY
        //LOGGER.log(Level.INFO, "Selector reference failed to multi resolve selector: <"+myElement.getSelectorString(true)+">");
    }

    private fun getAccessorMethods(): Pair<String, String>? {
        val accessorProperty = myElement.getParentOfType(ObjJAccessorProperty::class.java)
        if (accessorProperty != null) {
            return Pair<String, String>(accessorProperty.getter, accessorProperty.setter)
        }
        val instanceVariableDeclaration = myElement.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
        if (instanceVariableDeclaration != null) {
            val getter = instanceVariableDeclaration.getter
            val setter = instanceVariableDeclaration.setter
            return Pair<String, String>(
                    getter?.selectorString,
                    setter?.selectorString)
        }
        return null
    }

    override fun handleElementRename(selectorString: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, selectorString)
    }

}
