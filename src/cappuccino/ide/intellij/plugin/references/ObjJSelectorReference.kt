package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.inference.createTag
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult

import java.util.ArrayList

class ObjJSelectorReference(element: ObjJSelector) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    private val thisMethodHeaderParent: ObjJMethodHeaderDeclaration<*>?
    private var _classConstraints: List<String>? = null
    private val fullSelector: String?
    private val accessorMethods: Pair<String, String>?
    private val tag = createTag()

    init {
        accessorMethods = getAccessorMethods()
        thisMethodHeaderParent = if (accessorMethods == null) element.getParentOfType(ObjJMethodHeaderDeclaration::class.java) else null
        val methodCallParent = element.getParentOfType(ObjJMethodCall::class.java)
        fullSelector = thisMethodHeaderParent?.selectorString ?: methodCallParent?.selectorString
    }

    private val callTargetClassTypesIfMethodCall: List<String> get() {
        var constraints = _classConstraints
        if (constraints != null) {
            return constraints
        }
        val methodCall = myElement.getParentOfType(ObjJMethodCall::class.java)
                ?: return emptyList()
        if (DumbService.isDumb(myElement.project)) {
            return emptyList()
        }
        constraints = methodCall.callTarget.getPossibleCallTargetTypes(tag)
        _classConstraints = constraints
        return constraints
    }

    override fun getVariants(): Array<Any?> {
        return arrayOfNulls(0)
    }

    override fun isReferenceTo(
            elementToCheck: PsiElement): Boolean {
        if (elementToCheck !is ObjJSelector) {
            return false
        }
        if (elementToCheck.containingClassName == ObjJElementFactory.PlaceholderClassName) {
            return false
        }
        val elementToCheckAsMethodCall = elementToCheck.getParentOfType( ObjJMethodCall::class.java)
        val elementToCheckMethodCallTargetClasses = elementToCheckAsMethodCall?.callTarget?.getPossibleCallTargetTypes(tag)?: listOf()
        val elementToCheckMethodCallSelector = elementToCheckAsMethodCall?.selectorString
        if (thisMethodHeaderParent != null) {
            return elementToCheckMethodCallSelector == fullSelector && (elementToCheckMethodCallTargetClasses.isEmpty() || ObjJClassType.UNDETERMINED in elementToCheckMethodCallTargetClasses || myElement.containingClassName in elementToCheckMethodCallTargetClasses)
        } else if (accessorMethods != null) {
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
        var selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(myElement, tag)
        var out: MutableList<PsiElement> = ArrayList()
        if (!selectorResult.isEmpty) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(myElement)
        if (!selectorResult.isEmpty) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }
        val constraints = callTargetClassTypesIfMethodCall
        if (out.isNotEmpty()) {
            if (constraints.isNotEmpty() && ObjJClassType.UNDETERMINED !in constraints && ObjJClassType.ID !in constraints && constraints.contains(ObjJClassType.ID)) {
                val tempOut = out.filter { element -> element is ObjJCompositeElement && constraints.contains(ObjJHasContainingClassPsiUtil.getContainingClassName(element)) }
                if (tempOut.isNotEmpty()) {
                    out = tempOut as MutableList<PsiElement>
                }
            }
            return PsiElementResolveResult.createResults(out)
        }
        val result: SelectorResolveResult<PsiElement> = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(myElement, selectorResult.possibleContainingClassNames, tag)
        if (result.isNotEmpty) {
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
