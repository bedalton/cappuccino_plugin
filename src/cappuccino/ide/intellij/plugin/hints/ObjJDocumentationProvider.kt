package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.getPossibleClassTypes
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.lang.documentation.DocumentationMarkup

class ObjJDocumentationProvider : AbstractDocumentationProvider() {

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val comment = element?.docComment ?: originalElement?.docComment ?: CommentWrapper("")
        return InfoSwitch(element, originalElement)
                .info(ObjJVariableName::class.java, orParent = false) {
                    LOGGER.info("QuickInfo for variable name")
                    it.quickInfo(comment, originalElement)
                }
                .info(ObjJSelector::class.java) {
                    LOGGER.info("QuickInfo for method selector")
                    it.getParentOfType(ObjJMethodHeaderDeclaration::class.java)?.text ?: it.description?.presentableText
                }
                .info(ObjJMethodHeaderDeclaration::class.java, orParent = true) {
                    LOGGER.info("QuickInfo for methodHeaderDeclaration")
                    it.text
                }
                .info(ObjJMethodCall::class.java) { methodCall ->
                    LOGGER.info("QuickInfo for method call")
                    methodCall.referencedHeaders.mapNotNull { it.text }.joinToString { "\n" }
                }
                .info(ObjJFunctionCall::class.java, orParent = true) {
                    LOGGER.info("QuickInfo for function call")
                    it.functionDeclarationReference?.description?.presentableText
                }
                .info(ObjJFunctionName::class.java, orParent = true) {
                    LOGGER.info("QuickInfo for function name")
                    (it.parent as? ObjJFunctionCall)?.functionDeclarationReference?.description?.presentableText
                }
                .info(ObjJQualifiedMethodCallSelector::class.java, orParent = true) {
                    LOGGER.info("QuickInfo for qualified method call selector")
                    it.quickInfo(comment)
                }
                .info(ObjJMethodDeclarationSelector::class.java, orParent = true) {
                    LOGGER.info("QuickInfo for method declaration selector")
                    val parameterComment = comment.getParameterComment(it.variableName?.text ?: "")
                    val out = StringBuilder(it.text)
                    val containingClassName = it.containingClassName
                    if (parameterComment?.paramComment != null) {
                        out.append(" - ").append(parameterComment.paramComment)
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
                    if (function.returnType.isNotNullOrBlank()) {
                        out.append(" : ").append(function.returnType)
                    }
                    out.toString() + getLocationString(element)
                }
                .info(ObjJInstanceVariableDeclaration::class.java, orParent = true) {
                    it.text
                }

                .info(ObjJBodyVariableAssignment::class.java, orParent = true) {
                    val container = getLocationString(element)
                    StringBuilder("File Scope Variable in")
                            .append(container)
                            .toString()
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
        val doc = StringBuilder()
        val comment = element?.docComment ?: originalElement?.docComment ?: CommentWrapper("")
        LOGGER.info("Generating doc comment from comment <${comment.commentText}>")
        return comment.commentText
    }
}

private fun getLocationString(element: PsiElement?): String {
    val container = element?.containerName
    return if (container != null) " [$container]" else ""
}

private fun <PsiT:PsiElement> Class<PsiT>.getFirst(vararg elements: PsiElement?) : PsiT? {
    for (element in elements) {
        if (element == null) continue
        if (this.isInstance(element))
            return this.cast(element)
    }
    return null
}

private data class InfoSwitch(internal val element:PsiElement?, internal val originalElement: PsiElement?, internal var infoString:String? = null)

private fun <PsiT:ObjJCompositeElement> InfoSwitch.info(psiClass: Class<PsiT>, orParent:Boolean = false, callback:(elem:PsiT) -> String?) : InfoSwitch {
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

private val PsiElement.containerName:String? get () {
    var container = (this as? ObjJHasContainingClass)?.containingClassNameOrNull
    if (container.isNullOrBlank())
        container = this.containingFile?.name
    return container

}


private fun ObjJVariableName.quickInfo(comment: CommentWrapper? = null, originalElement: PsiElement?) : String? {
    val out = StringBuilder()
    if (ObjJClassDeclarationsIndex.instance[text, project].size > 0) {
        out.append("Class ").append(text)
        return out.toString()
    }
    val parentMethodDeclarationHeader = parent as? ObjJMethodDeclarationSelector
    if (parentMethodDeclarationHeader != null) {
        val type = parentMethodDeclarationHeader.formalVariableType?.text
        if (type != null)
            out.append("(").append(type).append(")")
        out.append(text)
        val paramComment = comment?.getParameterComment(text)?.paramComment
        if (paramComment.isNotNullOrBlank()) {
            out.append(" - ").append(paramComment)
        }
        //out.append(" in ").append("[").append(it.containingClassName).append("]")
        return out.toString()
    } else {
        LOGGER.info("Check QNR")
        val prevSiblings = previousSiblings
        if (prevSiblings.isEmpty()) {
            LOGGER.info("No prev siblings")
            val inferredTypes = inferQualifiedReferenceType(listOf(this), createTag())?.toClassList() ?: emptySet()
            LOGGER.info("Tried to infer types. Found: [$inferredTypes]")
            val classNames = inferredTypes.flatMap { it.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) }.toSet().joinToString("|")
            out.append("Variable ").append(name)
            if (classNames.isNotEmpty()) {
                out.append(" : ").append(classNames)
            }
            out.append(" in ").append(getLocationString(this))
            return out.toString()
        }
        val inferredTypes = inferQualifiedReferenceType(prevSiblings, createTag())
        val name = this.text
        /*val classes = inferredTypes?.jsClasses(project)?.filter { jsClass -> jsClass.properties.any { it.name == name } }
                ?: emptyList()
        val classNames = when {
            classes.size == 1 -> classes[0].className
            classes.isEmpty() -> "???"
            else -> classes.joinToString(" or ")
        }
        val infoProperties = classes.flatMap { it.properties }.filter { it.name == name }
        if (infoProperties.isNotEmpty()) {
            val propertyTypes = infoProperties.map { it.type.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) }.toSet().joinToString("|")
            out.append("Variable ").append(name)
            if (propertyTypes.isNotEmpty()) {
                out.append(" : ").append(propertyTypes)
            }
            if (classNames.isNotEmpty())
                out.append(" in ").append(classNames)
            return out.toString()
        }
        */
        val propertyTypes= getVariableNameComponentTypes(this, inferredTypes, createTag())?.toClassList() ?: emptySet()
        if (propertyTypes.isNotEmpty()) {
            out.append("Variable ").append(name)
            if (propertyTypes.isNotEmpty()) {
                out.append(" : ").append(propertyTypes)
            }
            val classNames = inferredTypes?.toClassList().orEmpty().toMutableList()
            if (classNames.contains("?")) {
                classNames.remove("?")
                classNames.add("...Any")
            }
            if (classNames.isNotEmpty())
                out.append(" in ").append(classNames)

            if (propertyTypes.isNotEmpty() && classNames.isNotEmpty()) {
                return out.toString()
            }
        }
    }

    out.append("Variable '").append(text).append("'")
    val possibleClasses = this.getPossibleClassTypes( createTag()).filterNot { it == "CPObject" }
    if (possibleClasses.isNotEmpty()) {
        out.append(" assumed to be [").append(possibleClasses.joinToString(" or ")).append("]")
    }
    out.append(" in").append(getLocationString(this))
    return out.toString()
}

private fun ObjJQualifiedMethodCallSelector.quickInfo(comment:CommentWrapper? = null) : String? {
    val out = StringBuilder()
    val resolved = (parent as? ObjJMethodCall)?.referencedHeaders ?: emptyList()
    val resolvedComments = resolved.mapNotNull {
        it.docComment
    }
    val index = this.index
    val resolvedSelectors = resolved.mapNotNull { (it.selectorList.getOrNull(index)?.parent as? ObjJMethodDeclarationSelector) }
    val resolvedTypes = resolvedSelectors.mapNotNull { it.formalVariableType?.text }.toSet()
    val resolvedVariableNames = resolvedSelectors.mapNotNull { it.variableName?.text }.filter { it.isNotNullOrBlank() }
    val positionComment = resolvedComments.mapNotNull { it.getParameterComment(index)?.paramComment }.joinToString("|")
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