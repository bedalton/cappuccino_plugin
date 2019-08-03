package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassAndSelectorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJClassInheritanceIndex
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferCallTargetType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.inference.withoutAnyType
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

@Suppress("MoveVariableDeclarationIntoWhen")
class ObjJSelectorReference(element: ObjJSelector, val  tag: Long = createTag()) : PsiPolyVariantReferenceBase<ObjJSelector>(element, TextRange.create(0, element.textLength)) {

    private var _classConstraints: Set<String>? = null
    private val fullSelector: String = myElement.getParentOfType(ObjJHasMethodSelector::class.java)!!.selectorString
    private val declaredIn: DeclaredIn = getDeclaredIn(element)

    private val callTargetClassTypesIfMethodCall: Set<String>
        get() {
            var constraints: Set<String>? = _classConstraints
            if (constraints != null) {
                return constraints
            }
            val targetClass = getTargetClass(myElement, tag).orEmpty()
            constraints = getAllPossibleClassTypes(myElement.project, targetClass, fullSelector)
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

        if (element.isEquivalentTo(elementToCheck))
            return false

        if (!isSimilar(elementToCheck))
            return false

        if (elementToCheck.containingClassName == ObjJElementFactory.PlaceholderClassName) {
            return false
        }
        val constraints = callTargetClassTypesIfMethodCall
        val parent = elementToCheck.getParentOfType(ObjJHasMethodSelector::class.java)
                ?: return false
        return when (parent) {
            is ObjJAccessorProperty -> declaredIn == DeclaredIn.USAGE && parent.containingClassName in constraints
            is ObjJMethodHeader -> return parent.containingClassName in constraints
            is ObjJSelectorLiteral -> return true
            is ObjJMethodCall -> {
                if (declaredIn == DeclaredIn.USAGE)
                    return false
                val thisTargetClasses = inferCallTargetType(parent.callTarget, tag)?.toClassList(null)?.withoutAnyType()?.ifEmpty{ null } ?: return true
                return constraints.intersect(thisTargetClasses).isNotEmpty()
            }
            else -> return false
        }
    }

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        val project = myElement.project
        val index = myElement.selectorIndex

        val possibleSelectors = getAccessorMethods() ?: listOf(fullSelector)
        val classConstraints = callTargetClassTypesIfMethodCall

        val allMatchingMethods = possibleSelectors.flatMap {
            ObjJUnifiedMethodIndex.instance[fullSelector, project]
                    .mapNotNull { it.selectorList.getOrNull(index) }
                    .filter {
                        isSimilar(it)
                    }
        }
        if (classConstraints.isEmpty()) {
            return PsiElementResolveResult.createResults(allMatchingMethods)
        }

        val matchingSelectorsInClass = allMatchingMethods.filter {
            it.containingClassName in classConstraints
        }
        val selectorLiterals = possibleSelectors.flatMap {
            ObjJSelectorInferredMethodIndex.instance[fullSelector, project]
                    .mapNotNull { it.selectorList.getOrNull(index) }
                    .filter {
                        isSimilar(it)
                    }
        }
        val out = selectorLiterals + matchingSelectorsInClass
        return PsiElementResolveResult.createResults(out)
    }

    private fun getAccessorMethods(): List<String>? {
        val accessorProperty = myElement.getParentOfType(ObjJAccessorProperty::class.java)
        if (accessorProperty != null) {
            return listOfNotNull(accessorProperty.getter, accessorProperty.setter).ifEmpty { null }
        }
        val instanceVariableDeclaration = myElement.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
        if (instanceVariableDeclaration != null) {
            val getter = instanceVariableDeclaration.getter
            val setter = instanceVariableDeclaration.setter
            return listOfNotNull(
                    getter?.selectorString,
                    setter?.selectorString
            ).ifEmpty { null }
        }
        return null
    }

    // Rename is prevented by adding all ObjJSelector element to the Veto extension point
    override fun handleElementRename(selectorString: String): PsiElement {
        return ObjJPsiImplUtil.setName(myElement, selectorString)
    }


    private fun getTargetClass(selector: ObjJSelector, tag: Long): Set<String>? {
        val parent = selector.getParentOfType(ObjJHasMethodSelector::class.java)!!
        if (parent is ObjJSelectorLiteral)
            return null
        if (parent is ObjJMethodCall) {
            return inferCallTargetType(parent.callTarget, tag)?.toClassList(null)?.withoutAnyType()
        }
        return setOf(parent.containingClassName)
    }

    private fun getAllPossibleClassTypes(project: Project, baseClasses: Set<String>, selectorString: String): Set<String> {
        val allSuperClasses = baseClasses.flatMap { className ->
            ObjJInheritanceUtil.getAllInheritedClasses(className, project, true).filter {
                val key = ObjJClassAndSelectorMethodIndex.getClassMethodKey(it, selectorString)
                ObjJClassAndSelectorMethodIndex.instance.containsKey(key, project)
            }
        }
        val allChildClasses = baseClasses.flatMap {
            ObjJClassInheritanceIndex.instance.getChildClassesAsStrings(it, project)
        }
        return (baseClasses + allSuperClasses + allChildClasses).toSet()
    }


    private fun isSimilar(otherSelector: ObjJSelector?): Boolean {
        val otherParent = otherSelector?.getParentOfType(ObjJHasMethodSelector::class.java) ?: return false
        if (fullSelector != otherParent.selectorString)
            return false
        return myElement.selectorIndex == otherSelector.selectorIndex
    }


    private fun getDeclaredIn(selector: ObjJSelector): DeclaredIn {
        val parent = selector.getParentOfType(ObjJHasMethodSelector::class.java)!!
        if (parent is ObjJMethodCall)
            return DeclaredIn.USAGE
        if (parent is ObjJSelector)
            return DeclaredIn.SELECTOR_LITERAL
        if (parent is ObjJPropertyAssignment)
            return DeclaredIn.ACCESSOR
        val containingClass = parent.containingClass ?: return DeclaredIn.UNKNOWN
        return if (containingClass is ObjJProtocolDeclaration)
            DeclaredIn.PROTOCOL
        else
            DeclaredIn.IMPLEMENTATION
    }


    internal enum class DeclaredIn {
        IMPLEMENTATION,
        PROTOCOL,
        SELECTOR_LITERAL,
        USAGE,
        ACCESSOR,
        UNKNOWN
    }


}
