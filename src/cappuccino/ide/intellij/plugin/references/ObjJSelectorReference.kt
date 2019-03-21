package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*

import java.util.ArrayList

class ObjJSelectorReference(element: ObjJSelector) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    private val thisMethodHeaderParent: ObjJMethodHeaderDeclaration<*>?
    private var _classConstraints: List<String>? = null
    private val fullSelector: String?
    private val accessorMethods: Pair<String, String>?

    init {
        accessorMethods = getAccessorMethods()
        thisMethodHeaderParent = if (accessorMethods == null) element.getParentOfType(ObjJMethodHeaderDeclaration::class.java) else null
        val methodCallParent = element.getParentOfType(ObjJMethodCall::class.java)
        fullSelector = thisMethodHeaderParent?.selectorString ?: methodCallParent?.selectorString
        //logger.log(Level.INFO, "Creating selector resolver for selector <"+fullSelector+">;");
    }

    private val callTargetClassTypesIfMethodCall: List<String> get() {
        var constraints = _classConstraints
        if (constraints != null) {
            return constraints
        }
        val methodCall = myElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //   logger.log(Level.INFO, "Selector is not in method call.");
                return emptyList()
        if (DumbService.isDumb(myElement.project)) {
            return emptyList()
        }
        constraints = methodCall.callTarget.possibleCallTargetTypes
        _classConstraints = constraints
        return constraints
    }

    override fun getVariants(): Array<Any?> {
        return arrayOfNulls(0)
        /*
        val selector = myElement
        val index = myElement.selectorIndex
        val querySelector = ObjJMethodPsiUtils.getSelectorUntil(selector, false)
        val methodHeaders = if (querySelector != null)
            ObjJMethodFragmentIndex.instance[querySelector, myElement.project]
        else
            ObjJUnifiedMethodIndex.instance.getAll(myElement.project)
        val foldingDescriptors:MutableList<String> = ArrayList()
        var i = 0
        methodHeaders.forEach({
            val variantSelector = it.selectorList.getOrNull(index)?.getSelectorString(false) ?: return@forEach
            if (foldingDescriptors.contains(variantSelector)) {
                return@forEach
            }
            i++
            foldingDescriptors.add(variantSelector)
        })

        logger.log(Level.INFO, "Added $i selector variants")
        return foldingDescriptors.toTypedArray()
        */
    }

    /*
    @NotNull
    @Override
    public Object[] getVariants() {
        if (DumbService.isDumb(myElement.getProject())) {
            return new Object[0];
        }
        logger.log(Level.INFO, "GetVariants");
        String selectorUpToSelf = ObjJPsiImplUtil.getSelectorUntil(myElement, false);
        if (selectorUpToSelf == null) {
            return new Object[0];
        }
        List<ObjJMethodHeaderDeclaration> headers = ObjJMethodFragmentIndex.getInstance().get(selectorUpToSelf, myElement.getProject());
        List<Object> results = new ArrayList<>();
        ObjJHasMethodSelector parent = myElement.getParentOfType(ObjJHasMethodSelector.class);
        int selectorIndex = parent != null ? parent.getSelectorList().indexOf(myElement) : 0;
        ObjJMethodHeader parentHeader = myElement.getParentOfType( ObjJMethodHeader.class);
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
        //logger.log(Level.INFO, "Checking if selector "+elementToCheck.getText()+" is reference to.");
        if (elementToCheck !is ObjJSelector) {
            return false
        }
        if (elementToCheck.containingClassName == ObjJElementFactory.PlaceholderClassName) {
            return false
        }
        val elementToCheckAsMethodCall = elementToCheck.getParentOfType( ObjJMethodCall::class.java)
        val elementToCheckMethodCallTargetClasses = elementToCheckAsMethodCall?.callTarget?.possibleCallTargetTypes ?: listOf()
        val elementToCheckMethodCallSelector = elementToCheckAsMethodCall?.selectorString
        if (thisMethodHeaderParent != null) {
            return elementToCheckMethodCallSelector == fullSelector && (elementToCheckMethodCallTargetClasses.isEmpty() || ObjJClassType.UNDETERMINED in elementToCheckMethodCallTargetClasses || myElement.containingClassName in elementToCheckMethodCallTargetClasses)
        } else if (accessorMethods != null) {
            //logger.log(Level.INFO, "Accessor Methods <"+accessorMethods.getFirst()+","+accessorMethods.getSecond()+">; Method call selector: " + methodCallString);
            return (accessorMethods.getFirst() == elementToCheckMethodCallSelector || accessorMethods.getSecond() == elementToCheckMethodCallSelector) && (elementToCheckMethodCallTargetClasses.isEmpty() || myElement.containingClassName in elementToCheckMethodCallTargetClasses)
        }
        if (elementToCheckAsMethodCall != null) {
            return elementToCheckMethodCallSelector == fullSelector
        }
        val callTargetTypes = callTargetClassTypesIfMethodCall
        val elementToCheckAsMethodDeclaration = elementToCheck.getParentOfType( ObjJMethodHeaderDeclaration::class.java)
        return elementToCheckAsMethodDeclaration != null && elementToCheckAsMethodDeclaration.selectorString == fullSelector && (callTargetTypes.isEmpty() || ObjJClassType.UNDETERMINED in callTargetTypes || elementToCheckAsMethodDeclaration.containingClassName in callTargetTypes)
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
        val constraints = callTargetClassTypesIfMethodCall
        if (!out.isEmpty()) {
            if (constraints.isNotEmpty() && !constraints.contains(ObjJClassType.UNDETERMINED) && constraints.contains(ObjJClassType.ID)) {
                val tempOut = out.filter { element -> element is ObjJCompositeElement && constraints.contains(ObjJHasContainingClassPsiUtil.getContainingClassName(element)) }
                if (tempOut.isNotEmpty()) {
                    out = tempOut as MutableList<PsiElement>
                }
            }
            return PsiElementResolveResult.createResults(out)
        }
        val result = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(myElement, selectorResult.possibleContainingClassNames)
        if (!result.isEmpty) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        return PsiElementResolveResult.EMPTY_ARRAY
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

    // Rename is prevented by adding all ObjJSelector element to the Veto extension point
    override fun handleElementRename(selectorString: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, selectorString)
    }

}
