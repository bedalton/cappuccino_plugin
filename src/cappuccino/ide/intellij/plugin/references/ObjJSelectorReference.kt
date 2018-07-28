package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJMethodFragmentIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import com.intellij.openapi.progress.ProgressIndicatorProvider

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

class ObjJSelectorReference(element: ObjJSelector) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    private val logger:Logger = Logger.getLogger(ObjJSelectorReference::class.java.canonicalName)

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
        //logger.log(Level.INFO, "Creating selector resolver for selector <"+fullSelector+">;");
    }

    private fun getClassConstraints(): List<String>? {
        if (classConstraints != null) {
            return classConstraints
        }
        val methodCall = myElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //   logger.log(Level.INFO, "Selector is not in method call.");
                return null
        if (DumbService.isDumb(myElement.project)) {
            return null
        }
        classConstraints = methodCall.callTarget.possibleCallTargetTypes
        return classConstraints
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
        if (elementToCheck.containingClassName == ObjJElementFactory.PLACEHOLDER_CLASS_NAME) {
            return false
        }
        val methodCall = elementToCheck.getParentOfType( ObjJMethodCall::class.java)
        val methodCallString = methodCall?.selectorString
        if (methodHeaderParent != null) {
            return methodCallString == fullSelector
        } else if (accessorMethods != null) {
            //logger.log(Level.INFO, "Accessor Methods <"+accessorMethods.getFirst()+","+accessorMethods.getSecond()+">; Method call selector: " + methodCallString);
            return accessorMethods.getFirst() == methodCallString || accessorMethods.getSecond() == methodCallString
        }
        if (methodCall != null) {
            return methodCallString == fullSelector
        }
        val declaration = elementToCheck.getParentOfType( ObjJMethodHeaderDeclaration::class.java)
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
            if (classConstraints != null && classConstraints!!.isNotEmpty() && !classConstraints!!.contains(ObjJClassType.UNDETERMINED) && classConstraints!!.contains(ObjJClassType.ID)) {
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
        /*
        result = ObjJSelectorReferenceResolveUtil.getVariableReferences(myElement, result.possibleContainingClassNames)
        if (!result.isEmpty) {
            return PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallPartialReferences(myElement, true)
        return if (!result.isEmpty) {
            PsiElementResolveResult.createResults(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        } else PsiElementResolveResult.EMPTY_ARRAY
        //logger.log(Level.INFO, "Selector reference failed to multi resolve selector: <"+myElement.getSelectorString(true)+">");
        */
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
