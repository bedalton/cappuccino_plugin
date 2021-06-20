package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.getPossibleClassTypes
import cappuccino.ide.intellij.plugin.utils.ifEmptyNull
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

class ObjJDocumentationProvider : AbstractDocumentationProvider() {

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: Any, element: PsiElement): PsiElement? {
        return null
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val comment = element?.docComment ?: originalElement?.docComment
        ?: CommentWrapper("", emptyList(), INFERRED_ANY_TYPE)
        return InfoSwitch(element, originalElement)
                .info(ObjJVariableName::class.java, orParent = false) {
                    it.quickInfo(comment)
                }
                .info(ObjJSelector::class.java) {
                    //LOGGER.warning(.info("QuickInfo for method selector")
                    it.getParentOfType(ObjJMethodHeaderDeclaration::class.java)?.text ?: it.description?.presentableText
                }
                .info(ObjJMethodHeaderDeclaration::class.java, orParent = true) {
                    //LOGGER.warning(.info("QuickInfo for methodHeaderDeclaration")
                    it.text
                }
                .info(ObjJMethodCall::class.java) { methodCall ->
                    //LOGGER.warning(.info("QuickInfo for method call")
                    methodCall.referencedHeaders.mapNotNull { it.text }.joinToString { "\n" }
                }
                .info(ObjJFunctionCall::class.java, orParent = true) {
                    //LOGGER.warning(.info("QuickInfo for function call")
                    it.functionDescription
                }
                .info(ObjJFunctionName::class.java, orParent = true) {
                    //LOGGER.warning(.info("QuickInfo for function name")
                    (it.parent as? ObjJFunctionCall)?.functionDeclarationReference?.description?.presentableText
                            ?: it.functionDescription
                }
                .info(ObjJQualifiedMethodCallSelector::class.java, orParent = true) {
                    //LOGGER.warning(.info("QuickInfo for qualified method call selector")
                    it.quickInfo(comment)
                }
                .info(ObjJMethodDeclarationSelector::class.java, orParent = true) {
                    //LOGGER.warning(.info("QuickInfo for method declaration selector")
                    val parameterComment = comment.getParameterComment(it.variableName?.text ?: "")
                    val out = StringBuilder(it.text)
                    val containingClassName = it.containingClassName
                    val commentText = parameterComment?.text?.replace("""\s*\\c\s*""".toRegex(), " ")
                    if (commentText.isNotNullOrBlank()) {
                        out.append(" - ").append(commentText)
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
                    val returnType = function.getReturnTypes(createTag())?.toClassListString(null)
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
        val comment = element?.docComment ?: originalElement?.docComment
        ?: CommentWrapper("", emptyList(), INFERRED_ANY_TYPE)
        ////LOGGER.warning(.info("Generating doc comment from comment <${comment.commentText}>")
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
    get() {
        var container = (this as? ObjJHasContainingClass)?.containingClassNameOrNull
        if (container.isNullOrBlank())
            container = this.containingFile?.name ?: this.originalElement?.containingFileName
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
        val parameterComment = comment?.getParameterComment(text)?.text?.replace("""\s*\\c\s*""".toRegex(), " ")
        if (parameterComment.isNotNullOrBlank()) {
            out.append(" - ").append(parameterComment)
        }
        //out.append(" in ").append("[").append(it.containingClassName).append("]")
        return out.toString()
    }
    ////LOGGER.warning(.info("Check QNR")
    val prevSiblings = previousSiblings
    val forwardTag = createTag() // Drop the + 1 as it was causing stack overflow
    if (prevSiblings.isEmpty() && !ObjJVariablePsiUtil.isNewVariableDec(this)) {
        val jsTypeDefFunctionResult = JsTypeDefFunctionsByNameIndex.instance[this.text, project].map {
            it.toJsFunctionType()
        }.minBy { it.parameters.size }
        if (jsTypeDefFunctionResult != null)
            return jsTypeDefFunctionResult.description.presentableText
        val inferenceResult = inferQualifiedReferenceType(listOf(this), forwardTag)
        val functionType = inferenceResult?.functionTypes.orEmpty().minBy { it.parameters.size }
        if (functionType != null) {
            out.append(functionType.descriptionWithName(text))
            return out.toString()
        }
        val classNames = stripCPObject(inferenceResult, project) ?: "<Any?>"
        val resolved = this.reference.resolve(true);
        ////LOGGER.warning(.info("Tried to infer types. Found: [$inferredTypes]")
        if (resolved?.hasParentOfType(ObjJInstanceVariableDeclaration::class.java).orFalse()) {
            out.append("instance var")
        } else if (resolved?.hasParentOfType(ObjJArguments::class.java).orFalse())
            out.append("parameter ")
        else
            out.append("var ").append(name)
        if (classNames.isNotNullOrBlank()) {
            out.append(": ").append(classNames)
        }
        getLocationString(this).trim().ifEmptyNull()?.let {
            out.append(" in ").append(it)
        }
        return out.toString()
    }
    val inferredTypes = inferQualifiedReferenceType(previousSiblings, forwardTag)
    val name = this.text
    var propertyTypes = getVariableNameComponentTypes(this, inferredTypes, false, createTag())?.toClassListString("&lt;Any&gt;")
    if (propertyTypes.isNotNullOrBlank()) {
        val classNames = stripCPObject(inferredTypes, project)?.trim()
        if (propertyTypes?.startsWith("$name(").orFalse())
            propertyTypes = propertyTypes?.substring(name.length)
        if (propertyTypes.isNotNullOrBlank() || classNames.isNotNullOrBlank()) {
            if (ObjJVariablePsiUtil.isNewVariableDec(this)) {
                out.append("var ")
            } else {
                out.append("property ")
            }
            out.append(name)
        }
        if (propertyTypes.isNotNullOrBlank() && propertyTypes !in anyTypes) {
            out.append(": ").append(propertyTypes)
        }
        if (classNames.isNotNullOrBlank()) {
            out.append(" in ").append(classNames)
        }

        if (propertyTypes.isNotNullOrBlank() || classNames.isNotNullOrBlank()) {
            return out.toString()
        }
    }
    out.append("var '").append(text).append("'")
    val possibleClasses = this.getPossibleClassTypes(createTag()).filterNot { it == "CPObject" }
    if (possibleClasses.isNotEmpty()) {
        out.append(" assumed to be [").append(possibleClasses.joinToString(" or ")).append("]")
    }
    getLocationString(this).trim().ifEmptyNull()?.let {
        out.append(" in ").append(it)
    }
    return out.toString()
}

private fun stripCPObject(inferredTypes: InferenceResult?, project: Project): String? {
    var classNamesTemp = inferredTypes?.toClassList(null).orEmpty()
    classNamesTemp = if (classNamesTemp.any { ObjJClassDeclarationsIndex.instance.containsKey(it, project) })
        classNamesTemp.filter { it == "CPObject" }.toSet()
    else
        classNamesTemp
    return classNamesTemp.joinToString("|").ifEmptyNull()
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
    val positionComment = resolvedComments.mapNotNull { it.getParameterComment(index)?.text?.replace("""\s*\\c\s*""".toRegex(), " ") }.joinToString("|")
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
    return this.copy(name = name).description.presentableText
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
        val resolved = functionName?.reference?.resolve()

        // Check if Function call was actually a constructor call
        // Return Name of class if it was
        if (resolved != null) {
            if (resolved.parent is JsTypeDefClassElement) {
                return "JsClass $functionNameString"
            } else if (resolved.parent is JsTypeDefInterfaceElement) {
                return "JsInterface $functionNameString"
            }
        }
        val parentFunction = resolved?.parentFunctionDeclaration ?: functionDeclarationReference
        ?: parentFunctionDeclaration
        ?: return "Function ${functionName?.text}(...)"
        return parentFunction.description.presentableText
    }