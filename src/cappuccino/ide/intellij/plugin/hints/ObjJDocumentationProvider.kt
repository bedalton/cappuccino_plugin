package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.getPossibleClassTypes
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

class ObjJDocumentationProvider : AbstractDocumentationProvider() {

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: Any, element: PsiElement): PsiElement? {
        return null
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val comment = element?.docComment ?: originalElement?.docComment ?: CommentWrapper("")
        return InfoSwitch(element, originalElement)
                .info(ObjJVariableName::class.java, orParent = false) {
                    //// LOGGER.info("QuickInfo for variable name")
                    it.quickInfo(comment)
                }
                .info(ObjJSelector::class.java) {
                    // LOGGER.info("QuickInfo for method selector")
                    it.getParentOfType(ObjJMethodHeaderDeclaration::class.java)?.text ?: it.description?.presentableText
                }
                .info(ObjJMethodHeaderDeclaration::class.java, orParent = true) {
                    // LOGGER.info("QuickInfo for methodHeaderDeclaration")
                    it.text
                }
                .info(ObjJMethodCall::class.java) { methodCall ->
                    // LOGGER.info("QuickInfo for method call")
                    methodCall.referencedHeaders.mapNotNull { it.text }.joinToString { "\n" }
                }
                .info(ObjJFunctionCall::class.java, orParent = true) {
                    // LOGGER.info("QuickInfo for function call")
                    it.functionDescription
                }
                .info(ObjJFunctionName::class.java, orParent = true) {
                    // LOGGER.info("QuickInfo for function name")
                    (it.parent as? ObjJFunctionCall)?.functionDeclarationReference?.description?.presentableText
                            ?: it.functionDescription
                }
                .info(ObjJQualifiedMethodCallSelector::class.java, orParent = true) {
                    // LOGGER.info("QuickInfo for qualified method call selector")
                    it.quickInfo(comment)
                }
                .info(ObjJMethodDeclarationSelector::class.java, orParent = true) {
                    // LOGGER.info("QuickInfo for method declaration selector")
                    val parameterComment = comment.getParameterComment(it.variableName?.text ?: "")
                    val out = StringBuilder(it.text)
                    val containingClassName = it.containingClassName
                    if (parameterComment?.paramCommentFormatted != null) {
                        out.append(" - ").append(parameterComment.paramCommentFormatted)
                    }
                    out.append("[in").append(containingClassName).append("]")
                    out.toString()
                }
                .info(ObjJGlobalVariableDeclaration::class.java, orParent = true) {
                    "Global Variable '${it.variableNameString}" + getLocationString(element)
                }
                .info(ObjJPreprocessorDefineFunction::class.java) { function ->
                    val out = StringBuilder("#define ${function.functionName}(")
                    function.formalParameterList?.formalParameterArgList?.forEach {
                        out.append(it.description.presentableText)
                    }
                    out.append(")")
                    val returnType = function.getReturnType(createTag())
                    if (returnType.isNotNullOrBlank()) {
                        out.append(": ").append(returnType)
                    }
                    out.toString() + getLocationString(element)
                }
                .info(ObjJInstanceVariableDeclaration::class.java, orParent = true) {
                    it.text
                }

                .info(ObjJBodyVariableAssignment::class.java, orParent = true) {
                    null
                }

                /// Only run after all other checks
                .info(ObjJCompositeElement::class.java) {
                    ObjJDescriptionUtil.getDescriptiveText(it) + getLocationString(it)
                }
                .infoString
    }

    /**
     * Callback for asking the doc provider for the complete documentation.
     *
     *
     * Underlying implementation may be time-consuming, that's why this method is expected not to be called from EDT.
     *
     *
     * One can use [DocumentationMarkup] to get proper content layout. Typical sample will look like this:
     * <pre>
     * DEFINITION_START + definition + DEFINITION_END +
     * CONTENT_START + main description + CONTENT_END +
     * SECTIONS_START +
     * SECTION_HEADER_START + section name +
     * SECTION_SEPARATOR + "
     *
     *" + section content + SECTION_END +
     * ... +
     * SECTIONS_END
    </pre> *
     *
     * @param element         the element for which the documentation is requested (for example, if the mouse is over
     * a method reference, this will be the method to which the reference is resolved).
     * @param originalElement the element under the mouse cursor
     * @return                target element's documentation, or `null` if provider is unable to generate documentation
     * for the given element
     */
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        //val doc = StringBuilder()
        val comment = element?.docComment ?: originalElement?.docComment ?: CommentWrapper("")
        //// LOGGER.info("Generating doc comment from comment <${comment.commentText}>")
        return comment.commentText
    }
}

private fun getLocationString(element: PsiElement?): String {
    val container = element?.containerName
    return if (container != null) " [$container]" else ""
}

private fun <PsiT : PsiElement> Class<PsiT>.getFirst(vararg elements: PsiElement?): PsiT? {
    for (element in elements) {
        if (element == null) continue
        if (this.isInstance(element))
            return this.cast(element)
    }
    return null
}

private data class InfoSwitch(internal val element: PsiElement?, internal val originalElement: PsiElement?, internal var infoString: String? = null)

private fun <PsiT : ObjJCompositeElement> InfoSwitch.info(psiClass: Class<PsiT>, orParent: Boolean = false, callback: (elem: PsiT) -> String?): InfoSwitch {
    if (infoString.isNullOrBlank()) {
        val element: PsiT = psiClass.getFirst(element, originalElement) ?: if (orParent)
            element.getSelfOrParentOfType(psiClass) ?: originalElement.getSelfOrParentOfType(psiClass) ?: return this
        else
            return this
        val value = callback(element) ?: return this
        infoString = value
    }
    return this
}

private val PsiElement.containerName: String?
    get () {
        var container = (this as? ObjJHasContainingClass)?.containingClassNameOrNull
        if (container.isNullOrBlank())
            container = this.containingFile?.name
        return container

    }


private fun ObjJVariableName.quickInfo(comment: CommentWrapper? = null): String? {
    if (DumbService.isDumb(project))
        return null
    val out = StringBuilder()
    if (ObjJClassDeclarationsIndex.instance[text, project].isNotEmpty()) {
        out.append("class ").append(text)
        return out.toString()
    }

    if (JsTypeDefClassesByNameIndex.instance.containsKey(text, project)) {
        out.append("Js Class ").append(text)
        return out.toString()
    }

    val parentMethodDeclarationHeader = parent as? ObjJMethodDeclarationSelector
    if (parentMethodDeclarationHeader != null) {
        out.append("parameter ")
        val type = parentMethodDeclarationHeader.formalVariableType?.text
        if (type != null)
            out.append("(").append(type).append(")")
        out.append(text)
        val paramComment = comment?.getParameterComment(text)?.paramCommentClean
        if (paramComment.isNotNullOrBlank()) {
            out.append(" - ").append(paramComment)
        }
        //out.append(" in ").append("[").append(it.containingClassName).append("]")
        return out.toString()
    } else {
        //// LOGGER.info("Check QNR")
        val prevSiblings = previousSiblings
        if (prevSiblings.isEmpty()) {
            LOGGER.info("No prev siblings")
            val inferenceResult = inferQualifiedReferenceType(listOf(this), createTag() + 1)
            val functionType = inferenceResult?.functionTypes.orEmpty().sortedBy { it.parameters.size }.firstOrNull()
            if (functionType != null) {
                out.append(functionType.descriptionWithName(text))
                return out.toString()
            }
            val classNames = inferenceResult?.toClassListString("<Any?>")
            //// LOGGER.info("Tried to infer types. Found: [$inferredTypes]")
            if (this.reference.resolve(true)?.hasParentOfType(ObjJArguments::class.java).orFalse())
                out.append("parameter")
            else
                out.append("var ").append(name)
            if (classNames.isNotNullOrBlank()) {
                out.append(": ").append(classNames)
            }
            out.append(" in ").append(getLocationString(this))
            return out.toString()
        }
        val inferredTypes = inferQualifiedReferenceType(this.previousSiblings + this, createTag() + 1)
        val name = this.text
        val propertyTypes = getVariableNameComponentTypes(this, inferredTypes, false, createTag())?.toClassListString("&lt;Any&gt;")
        if (propertyTypes.isNotNullOrBlank()) {

            val classNames = inferredTypes?.toClassListString(null)
            if (propertyTypes.isNotNullOrBlank() || classNames.isNotNullOrBlank())
                out.append("property ").append(name)
            if (propertyTypes.isNotNullOrBlank()) {
                out.append(": ").append(propertyTypes)
            }
            if (classNames.isNotNullOrBlank())
                out.append(" in ").append(classNames)

            if (propertyTypes.isNotNullOrBlank() || classNames.isNotNullOrBlank()) {
                return out.toString()
            }
        }
    }
    out.append("var '").append(text).append("'")
    val possibleClasses = this.getPossibleClassTypes(createTag() + 2).filterNot { it == "CPObject" }
    if (possibleClasses.isNotEmpty()) {
        out.append(" assumed to be [").append(possibleClasses.joinToString(" or ")).append("]")
    }
    out.append(" in ").append(getLocationString(this))
    return out.toString()
}

private fun ObjJQualifiedMethodCallSelector.quickInfo(comment: CommentWrapper? = null): String? {
    val out = StringBuilder()
    if (comment?.commentText.isNotNullOrBlank()) {
        out.append(comment?.commentText!!)
    }
    val resolved = (parent as? ObjJMethodCall)?.referencedHeaders ?: emptyList()
    val resolvedComments = resolved.mapNotNull {
        it.docComment
    }
    val index = this.index
    val resolvedSelectors: List<ObjJMethodDeclarationSelector> = resolved.mapNotNull { (it.selectorList.getOrNull(index)?.parent as? ObjJMethodDeclarationSelector) }
    val resolvedTypes = resolvedSelectors.mapNotNull { it.formalVariableType?.text }.toSet()
    val resolvedVariableNames = resolvedSelectors.mapNotNull { it.variableName?.text }.filter { it.isNotNullOrBlank() }
    val positionComment = resolvedComments.mapNotNull { it.getParameterComment(index)?.paramCommentClean }.joinToString("|")
    out.append(selector?.text ?: "_").append(":")
    out.append("(").append(resolvedTypes.joinToString("|")).append(")")
    if (resolvedVariableNames.size > 1) {
        out.append("<").append(resolvedVariableNames.joinToString("|")).append(">")
    } else if (resolvedVariableNames.isNotEmpty()) {
        out.append(resolvedVariableNames[0])
    }
    if (positionComment.isNotNullOrEmpty())
        out.append(" - ").append(positionComment)
    return out.toString()
}

private fun JsTypeListFunctionType.descriptionWithName(name: String): String {
    val out = StringBuilder(name)
    out.append(this.toString())
    return out.toString()
}

private val ObjJFunctionName.functionDescription: String?
    get() {
        val basicDescription = (parent as? ObjJFunctionCall)?.functionDeclarationReference?.description?.presentableText
        if (basicDescription.isNotNullOrBlank())
            return basicDescription!!
        return (parent as? ObjJFunctionCall)?.functionDescription
    }

private val ObjJFunctionCall.functionDescription: String?
    get() {
        val polyResolved = this.functionName?.reference?.multiResolve(false)?.mapNotNull { it.element }.orEmpty()
        val parentFunction: ObjJUniversalFunctionElement? = polyResolved.mapNotNull { resolved ->
            resolved.parentFunctionDeclaration ?: resolved.parent as? ObjJUniversalFunctionElement
        }.firstOrNull()

        val basicDescription = parentFunction?.description?.presentableText
        if (basicDescription.isNotNullOrBlank())
            return basicDescription!!
        if (parentFunction == null) {
            return "Function ${functionName?.text}"
        }
        //@todo check why we return description based on current call after checking for parent function null-ness
        val functionNameText = this.functionNameString ?: return null
        val function = inferQualifiedReferenceType(this.previousSiblings + this, createTag())?.functionTypes?.firstOrNull()
        return function?.descriptionWithName(functionNameText)
                ?: "Function ${functionName?.text} defined in ${parentFunction.containingFile?.name}"
    }