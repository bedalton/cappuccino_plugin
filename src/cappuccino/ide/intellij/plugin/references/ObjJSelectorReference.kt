package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassAndSelectorMethodIndex
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferCallTargetType
import cappuccino.ide.intellij.plugin.inference.toClassList
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper

import java.util.ArrayList
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil.getContainingClassName as getContainingClassName

class ObjJSelectorReference(element: ObjJSelector) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    private val thisMethodHeaderParent: ObjJMethodHeaderDeclaration<*>?
    private var _classConstraints: Set<String>? = null
    private val fullSelector: String?
    private val accessorMethods: Pair<String, String>?
    private val tag:Long = createTag()

    init {
        accessorMethods = getAccessorMethods()
        thisMethodHeaderParent = if (accessorMethods == null) element.getParentOfType(ObjJMethodHeaderDeclaration::class.java) else null
        val methodCallParent = element.getParentOfType(ObjJMethodCall::class.java)
        fullSelector = thisMethodHeaderParent?.selectorString ?: methodCallParent?.selectorString
    }

    private val callTargetClassTypesIfMethodCall: List<String> get() {
        var constraints:MutableList<String>? = _classConstraints?.toMutableList()
        if (constraints != null) {
            return constraints
        }
        val methodCall = myElement.getParentOfType(ObjJMethodCall::class.java)
                ?: return emptyList()
        if (DumbService.isDumb(myElement.project)) {
            return emptyList()
        }
        val project = myElement.project
        constraints = inferCallTargetType(methodCall.callTarget, tag)?.toClassList(null).orEmpty().toMutableList()
        constraints.addAll(constraints.flatMap{
            ObjJInheritanceUtil.getAllInheritedClasses(it, project)
        })
        _classConstraints = constraints.toSet()
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
        val project = myElement.project
        val index = myElement.selectorIndex
        val selector = myElement.getParentOfType(ObjJHasMethodSelector::class.java)?.selectorString ?: return emptyArray()
        val classConstraints = callTargetClassTypesIfMethodCall

        // Get quick result from class constraints
        val quickSelectorResult = classConstraints.flatMap { className ->
            ObjJClassAndSelectorMethodIndex.instance.getByClassAndSelector(className, selector, project)
        }.mapNotNull { it.selectorList.getOrNull(index) }.toSet()
        if (quickSelectorResult.isNotEmpty()) {
            return PsiElementResolveResult.createResults(quickSelectorResult)
        }
        // Define out array
        var out: MutableList<PsiElement> = ArrayList()

        // Get selector result from either method call or selector literal
        var selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(myElement, tag, classConstraints)
        if (selectorResult.isEmpty) {
            selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(myElement)
        }
        if (selectorResult.isNotEmpty) {
            out.addAll(ObjJResolveableElementUtil.onlyResolveableElements(selectorResult.result))
        }

        // If methods found, return them as result
        if (out.isNotEmpty()) {
            if (classConstraints.isNotEmpty() && ObjJClassType.UNDETERMINED !in classConstraints && ObjJClassType.ID !in classConstraints && classConstraints.contains(ObjJClassType.ID)) {
                val tempOut = out.filter { element ->
                    element is ObjJCompositeElement &&
                            getContainingClassName(element) in classConstraints }
                if (tempOut.isNotEmpty()) {
                    out = tempOut.toMutableList()
                }
            }
            return PsiElementResolveResult.createResults(out.toSet())
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
