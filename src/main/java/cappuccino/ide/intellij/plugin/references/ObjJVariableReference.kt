package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamespacedComponent
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

class ObjJVariableReference(
        element: ObjJQualifiedReferenceComponent,
        private val nullIfSelfReferencing: Boolean? = null,
        private val tag: Tag? = null
) : PsiPolyVariantReferenceBase<ObjJQualifiedReferenceComponent>(element, TextRange.create(0, element.textLength)) {
    private var referencedInScope: ReferencedInScope? = null

    private val referencedElement: SmartPsiElementPointer<PsiElement>? by lazy {
        val resolved = resolve()
        if (resolved != null)
            SmartPointerManager.createPointer(resolved)
        else
            null
    }

    private val globalVariableNameElement: PsiElement?
        get() {
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            val file = myElement.containingFile as? ObjJFile
            val imports = file?.cachedImportFileList
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[myElement.text, myElement.project]
            var namedElement: PsiElement? = null
            if (globalVariableDeclarations.isNotEmpty()) {
                if (imports == null) {
                    namedElement = globalVariableDeclarations[0].variableName
                } else {
                    for (declaration in globalVariableDeclarations) {
                        if (declaration.containingFile in imports) {
                            namedElement = declaration.variableName
                        }
                    }
                }
            }
            val functionDeclarationElements = ObjJFunctionsIndex.instance[myElement.text, myElement.project]
            if (namedElement == null && functionDeclarationElements.isNotEmpty()) {
                namedElement = functionDeclarationElements[0].functionNameNode
                if (namedElement == null) {
                    for (declarationElement in functionDeclarationElements) {
                        namedElement = declarationElement.functionNameNode
                        if (namedElement != null) {
                            break
                        }
                    }
                }
            }
            return if (namedElement != null && !namedElement.isEquivalentTo(myElement)) namedElement else null
        }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val parent = element.parent
        val newVariableName = ObjJElementFactory.createVariableName(myElement.project, newElementName)
        parent.node.replaceChild(myElement.node, newVariableName.node)
        return newVariableName
    }

    override fun isReferenceTo(otherElement: PsiElement): Boolean {

        if (DumbService.isDumb(myElement.project)) {
            return false
        }
        // Element is in compiled objective-j document
        try {
            if (otherElement.containingFile.text.startsWith("@STATIC;") || myElement.containingFile.text.startsWith("@STATIC;")) {
                return false
            }
            // Element is virtual and not in file
        } catch (e: Exception) {
            return false
        }

        // Text is not equivalent, ignore
        if (otherElement.text != myElement.text) {
            return false
        }
        //Is Same element, Do not reference self
        if (otherElement.isEquivalentTo(myElement)) {
            return false
        }

        val psiElementIsZeroIndexInQualifiedReference = otherElement !is ObjJVariableName || otherElement.indexInQualifiedReference == 0
        val thisElementIsZeroIndexedInQualifiedReference = myElement.indexInQualifiedReference == 0
        if (!psiElementIsZeroIndexInQualifiedReference || (!thisElementIsZeroIndexedInQualifiedReference && otherElement is ObjJCompositeElement)) {
            return false
        }
        if (thisElementIsZeroIndexedInQualifiedReference && otherElement is ObjJClassName) {
            return true
        }

        val referencedElement = this.referencedElement?.element
        if (referencedElement?.isEquivalentTo(otherElement).orFalse()) {
            return true
        }

        if (otherElement is JsTypeDefNamespacedComponent) {
            val otherIndex = otherElement.namespaceComponents.size
            if (thisElementIsZeroIndexedInQualifiedReference && otherIndex > 0)
                return false
            val enclosingClass = otherElement.namespaceComponents.getOrNull(0) ?: return false
            val parentClass = inferQualifiedReferenceType(myElement.previousSiblings, tag
                    ?: createTag())?.toClassList(null) ?: return false
            return enclosingClass in parentClass
        }

        if (referencedInScope == null) {
            referencedInScope = referencedElement?.getContainingScope() ?: myElement.getContainingScope()
        }

        //Finds this elements, and the new elements scope
        val sharedContext: PsiElement? = PsiTreeUtil.findCommonContext(myElement, otherElement)
        val sharedScope: ReferencedInScope = sharedContext?.getContainingScope() ?: UNDETERMINED
        if (sharedScope == UNDETERMINED && referencedInScope != UNDETERMINED) {
            return false
        }
        if (referencedInScope != UNDETERMINED && referencedInScope == sharedScope) {
            return true
        }
        return false
    }

    override fun resolve(): PsiElement? {
        if (DumbService.isDumb(myElement.project)) {
            return null
        }
        val result = multiResolve(tag ?: createTag(), nullIfSelfReferencing.orFalse()).mapNotNull { it.element }
        if (nullIfSelfReferencing.orFalse()) {
            return result.filterNot { it == myElement }.firstOrNull()
        }
        return result.filterNot { it == myElement }.firstOrNull() ?: result.firstOrNull()
    }

    private fun multiResolve(tag: Tag, nullIfSelfReferencing: Boolean): Array<ResolveResult> {
        val classes = ObjJClassDeclarationsIndex.instance[myElement.text,myElement.project]
                .mapNotNull { it.className }
        if (classes.isNotEmpty())
            return PsiElementResolveResult.createResults(classes)
        val element = resolveInternal(tag)
        if (element != null) {
            if (!nullIfSelfReferencing)
                return PsiElementResolveResult.createResults(listOf(element))
        }
        if (ObjJVariablePsiUtil.isNewVariableDec(myElement))
            return PsiElementResolveResult.createResults(listOf(myElement))


        val project = myElement.project
        val properties =
                getJsNamedElementsForReferencedElement(myElement, myElement.text, tag) +
                ObjJFunctionsIndex.instance[myElement.text, project]
        return PsiElementResolveResult.createResults(properties)
    }

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        return multiResolve(tag ?: createTag(), nullIfSelfReferencing.orFalse())
    }

    fun resolve(nullIfSelfReferencing: Boolean? = null, tag: Tag? = null): PsiElement? {
        return myElement.resolveFromCache {
            if (DumbService.isDumb(myElement.project)) {
                return@resolveFromCache null
            }
            val result = multiResolve(tag ?: createTag(), nullIfSelfReferencing.orFalse()).mapNotNull { it.element }
            if (nullIfSelfReferencing.orFalse()) {
                return@resolveFromCache result.filterNot { it == myElement }.firstOrNull()
            }
            return@resolveFromCache result.firstOrNull()
        }
    }

    private fun resolveInternal(tag: Tag? = null): PsiElement? {
        if (tag != null && this.tag == tag)
            return null
        try {
            if (myElement.containingFile.text.startsWith("@STATIC;")) {
                return null
            }
        } catch (ignored: Exception) {
            //Exception was thrown on failed attempts at adding code to file pragmatically
            return null
        }

        val variableDeclaration = myElement.parent?.parent as? ObjJVariableDeclaration
        if (variableDeclaration?.hasVarKeyword().orFalse())
            return myElement

        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement)
        if (myElement.indexInQualifiedReference > 0) {
            return null
        }
        if (variableName == null) {
            variableName = globalVariableNameElement
        }
        if (variableName == null) {
            variableName = resolveIfClassName()
            if (variableName != null)
                return variableName
        }
        if (variableName is ObjJVariableName && variableName.indexInQualifiedReference > 0) {
            return null
        }
        return variableName
    }

    private fun resolveIfClassName(): PsiElement? {
        val callTarget = myElement.parent?.parent as? ObjJCallTarget ?: return null
        val selector = (callTarget.parent as? ObjJMethodCall)?.selectorString ?: return null
        var classes: List<ObjJClassDeclarationElement<*>> = ObjJClassDeclarationsIndex.instance[myElement.text, element.project]
        if (selector.isEmpty() || classes.isEmpty())
            return null
        val classesTemp = classes.filter {
            it.hasMethod(selector)
        }
        if (classesTemp.isNotEmpty()) {
            classes = classesTemp
        }
        return classes.firstOrNull { it is ObjJImplementationDeclaration && !it.isCategory }
                ?: classes.firstOrNull()
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }
}