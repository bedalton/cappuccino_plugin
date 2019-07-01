package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.indices.ObjJVariableDeclarationsByNameIndex
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.jstypedef.contributor.withAllSuperClassNames
import cappuccino.ide.intellij.plugin.jstypedef.indices.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamespacedComponent
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.*

import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class ObjJVariableReference(
        element: ObjJVariableName,
        private val follow:Boolean = true,
        private val nullIfSelfReferencing: Boolean? = null,
        private val tag:Long? = null
) : PsiPolyVariantReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private var referencedInScope: ReferencedInScope? = null

    private val isGlobal: Boolean by lazy {
        variableDeclarationsEnclosedGlobal(myElement, true)
    }

    private val referencedElement:SmartPsiElementPointer<PsiElement>? by lazy {
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
            val file = myElement.containingObjJFile
            val imports = file?.cachedImportFileList
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[myElement.text, myElement.project]
            var namedElement:PsiElement? = null
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

        // Element is in compiled objective-j document
        try {
            if (otherElement.containingFile.text.startsWith("@STATIC;") || myElement.containingFile.text.startsWith("@STATIC;")) {
                return false
            }
        // Element is virtual and not in file
        } catch (e:Exception) { return false }

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
            val parentClass = inferQualifiedReferenceType(myElement.previousSiblings, tag ?: createTag())?.toClassList(null) ?: return false
            return enclosingClass in parentClass
        }

        if (referencedInScope == null) {
            referencedInScope = referencedElement?.getContainingScope() ?: myElement.getContainingScope()
        }

        //Finds this elements, and the new elements scope
        val sharedContext:PsiElement? = PsiTreeUtil.findCommonContext(myElement, otherElement)
        val sharedScope:ReferencedInScope = sharedContext?.getContainingScope() ?: UNDETERMINED
        if (sharedScope == UNDETERMINED && referencedInScope != UNDETERMINED) {
            return false
        }
        if (referencedInScope != UNDETERMINED && referencedInScope == sharedScope) {
            return true
        }
        return false
    }

    override fun resolve(): PsiElement? {
        return myElement.resolveFromCache {
            val result = multiResolve(tag ?: createTag(), nullIfSelfReferencing.orFalse()).mapNotNull { it.element }
            if (nullIfSelfReferencing.orFalse()) {
                return@resolveFromCache  result.filterNot { it == myElement }.firstOrNull()
            }
            return@resolveFromCache result.filterNot { it == myElement }.firstOrNull() ?: result.firstOrNull()
        }
    }

    private fun multiResolve(tag:Long? = null, nullIfSelfReferencing: Boolean) : Array<ResolveResult> {
        val element = resolveInternal(tag)
        if (element != null && !element.isEquivalentTo(myElement)) {
            return PsiElementResolveResult.createResults(listOf(element))
        }
        val out = mutableListOf<ObjJCompositeElement>()
        if (myElement.indexInQualifiedReference == 0) {
            out.addAll(getGlobalAssignments(true).orEmpty())
        }

        if (out.isNotEmpty()) {
            return PsiElementResolveResult.createResults(out)
        }

        val project = myElement.project
        val variableName = myElement.text
        // Get simple if no previous siblings
        val prevSiblings = myElement.previousSiblings
        if (prevSiblings.isEmpty()) {
            val outSimple: List<JsTypeDefElement> = JsTypeDefPropertiesByNameIndex.instance[variableName, project].filter {
                it.enclosingNamespace.isEmpty()
            }.mapNotNull {
                it.propertyName ?: it.propertyAccess?.propertyName ?: it.stringLiteral
            }
            return PsiElementResolveResult.createResults(outSimple)
        }
        val className = prevSiblings.joinToString("\\.") { Regex.escape(it.text) }
        val isStatic = JsTypeDefClassesByNamespaceIndex.instance[className, project].isNotEmpty()

        // Get types if qualified
        val classTypes = inferQualifiedReferenceType(prevSiblings, createTag())
        if (classTypes == null) {
            return PsiElementResolveResult.EMPTY_ARRAY
        }
        if (classTypes.classes.isNotEmpty()) {
            val allClassNames = classTypes.classes.flatMap {
                JsTypeDefClassesByNameIndex.instance[it, project].map {
                    it.toJsClassDefinition()
                }
            }.withAllSuperClassNames(project)
            val searchString = "(" + allClassNames.joinToString("|") { Regex.escapeReplacement(it)} + ")\\." + variableName
            val found = JsTypeDefPropertiesByNamespaceIndex.instance.getByPatternFlat(searchString,project).filter {
                it.isStatic == isStatic
            }.mapNotNull {
                it.propertyName ?: it.propertyAccess?.propertyName ?: it.stringLiteral
            }
            return PsiElementResolveResult.createResults(found)
        }
        return if (nullIfSelfReferencing)
            PsiElementResolveResult.EMPTY_ARRAY
        else
            PsiElementResolveResult.createResults(listOf(myElement))
    }

    override fun multiResolve(partial:Boolean) : Array<ResolveResult> {
        return multiResolve(tag, nullIfSelfReferencing.orFalse())
    }

    fun resolve(nullIfSelfReferencing: Boolean? = null, tag:Long? = null) : PsiElement? {
        return myElement.resolveFromCache {
            val result = multiResolve(tag ?: createTag(), nullIfSelfReferencing.orFalse()).mapNotNull { it.element }
            if (nullIfSelfReferencing.orFalse()) {
                return@resolveFromCache  result.filterNot { it == myElement }.firstOrNull()
            }
            return@resolveFromCache result.firstOrNull()
        }
    }

    private fun resolveInternal(tag:Long? = null) : PsiElement? {
        if (tag != null && this.tag == tag)
            return null
        try {
            if (myElement.containingFile.text.startsWith("@STATIC;")) {
                return null
            }
        } catch (ignored:Exception) {
            //Exception was thrown on failed attempts at adding code to file pragmatically
            return null
        }

        val variableDeclaration = myElement.parent?.parent as? ObjJVariableDeclaration
        if (variableDeclaration?.hasVarKeyword().orFalse())
            return null

        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement)
        if (myElement.indexInQualifiedReference > 0) {
            return variableName
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
            null
        }
        return variableName
    }

    private fun resolveIfClassName() : PsiElement? {
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

    private fun getGlobalAssignments(nullIfSelfReferencing: Boolean) : List<ObjJCompositeElement>? {
        val variableNameString = myElement.text
        val allWithName = ObjJVariableDeclarationsByNameIndex.instance[variableNameString, myElement.project]
        if (allWithName.isNullOrEmpty()) {
            return null
        }
        val allBodyDeclarations = allWithName.filter {
            it.hasVarKeyword()
        }
        val allCandidates = allWithName.filterNot {variableDeclaration ->
            variableDeclaration in allBodyDeclarations && allBodyDeclarations.any {
                variableDeclaration.commonScope(it) != UNDETERMINED
            }
        }
        val allCandidatesInFile = allCandidates.filter {
            myElement.commonScope(it) != UNDETERMINED && (nullIfSelfReferencing && !myElement.isEquivalentTo(it))
        }.sortedByDescending {
            it.textRange.startOffset
        }
        if (nullIfSelfReferencing.orFalse() && allCandidatesInFile.size == 1) {
            val onlyCandidate = allCandidatesInFile.firstOrNull() ?: return null
            if(onlyCandidate.commonContext(myElement) == onlyCandidate) {
                return null
            }
        }
        if (allCandidatesInFile.isNotEmpty())
            return allCandidatesInFile
        return allCandidates
    }
}

private fun variableDeclarationsEnclosedGlobal(variableName: ObjJVariableName, @Suppress("SameParameterValue") follow:Boolean = false) : Boolean {
    if(!DumbService.isDumb(variableName.project) && follow) {
        return variableDeclarationsEnclosedGlobalStrict(variableName)
    }
    if (variableName.indexInQualifiedReference != 0)
        return false
    val variableDeclaration = variableName.parent.parent as? ObjJVariableDeclaration
            ?: return false
    val isBodyVariableAssignmentLocal
            = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
    if (isBodyVariableAssignmentLocal || variableDeclaration.parent.parent.parent !is ObjJBlock) {
        return false
    }
    val variableNameString = variableName.text
    val isNotGlobal = variableDeclaration.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true).any { bodyVariableAssignment ->
        bodyVariableAssignment.varModifier != null &&
                bodyVariableAssignment.variableDeclarationList?.variableDeclarationList?.any { varDec ->
                    varDec.qualifiedReferenceList.any {
                        LOGGER.info("${it.qualifiedNameParts[0]?.text} ==? $variableNameString")
                        it.qualifiedNameParts.size == 1 && it.qualifiedNameParts[0]?.text == variableNameString
                    }
                }.orFalse()
    }
    return !isNotGlobal
}

private fun variableDeclarationsEnclosedGlobalStrict(variableName: ObjJVariableName) : Boolean {
    if (variableName.indexInQualifiedReference != 0)
        return false

    val variableDeclaration = variableName.parent.parent as? ObjJVariableDeclaration ?: return false
    val isBodyVariableAssignmentLocal
            = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
    if (isBodyVariableAssignmentLocal || variableDeclaration.parent.parent.parent !is ObjJBlock) {
        return false
    }
    val resolved = ObjJVariableReference(variableName, false).resolve() ?: return true
    return (resolved.parent.parent.parent.parent as? ObjJBodyVariableAssignment)?.varModifier == null
}