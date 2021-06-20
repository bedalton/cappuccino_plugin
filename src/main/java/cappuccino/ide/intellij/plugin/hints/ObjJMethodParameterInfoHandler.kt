package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReference
import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger


class ObjJMethodParameterInfoHandler : ParameterInfoHandler<ObjJMethodCall, ObjJMethodDescription> {

    override fun couldShowInLookup(): Boolean {
        return true
    }

    override fun getParametersForLookup(lookupElement: LookupElement?, context: ParameterInfoContext?): Array<Any>? {
        val element = lookupElement?.`object` as? ObjJCompositeElement
        if (element == null) {
            return emptyArray()
        }
        if (element is ObjJMethodHeaderDeclaration<*>) {
            return arrayOf(element)
        }
        return emptyArray()
    }

    override fun getParametersForDocumentation(parameter: ObjJMethodDescription, context: ParameterInfoContext): Array<Any>? {
        return null
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ObjJMethodCall? {
        return getCallExpression(context.offset, context.file)
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): ObjJMethodCall? {
        return getCallExpression(context.offset, context.file)
    }

    private fun findElementForParameterInfo(element:PsiElement): ObjJMethodCall? {
        if (element is ObjJMethodCall)
            return element
        val arguments = element.getSelfOrParentOfType(ObjJQualifiedMethodCallSelector::class.java) ?: element.getSelfOrParentOfType(ObjJCallTarget::class.java)
        return arguments?.parent as? ObjJMethodCall
    }

    private fun getCallExpression(offset:Int, fileIn:PsiFile) : ObjJMethodCall? {
        val file = fileIn as? ObjJFile ?: return null
        val methodCallSelector =
                PsiTreeUtil.findElementOfClassAtOffset(file, offset, ObjJQualifiedMethodCallSelector::class.java, false) ?: PsiTreeUtil.findElementOfClassAtOffset(file, offset, ObjJCallTarget::class.java, false)
                ?: return null
        return methodCallSelector.parent as? ObjJMethodCall
    }

    override fun getParameterCloseChars(): String? {
        return ",){}"
    }

    override fun tracksParameterIndex(): Boolean {
        return true
    }


    override fun showParameterInfo(element: ObjJMethodCall, context: CreateParameterInfoContext) {
        val descriptions = getMethodHeaderDescriptions(element)
        if (descriptions.isNotEmpty()) {
            context.itemsToShow = descriptions
            context.showHint(element, element.textRange.startOffset, this)
        }
    }

    private fun getMethodHeaderDescriptions(element:ObjJMethodCall) : Array<ObjJMethodDescription> {
        val firstSelector = element.selectorList.getOrNull(0) ?: return emptyArray()
        val references = ObjJSelectorReference(firstSelector).multiResolve(false)
        val out = mutableListOf<ObjJMethodDescription>()
        references.forEach {
            val referencedSelector = it.element as? ObjJSelector ?: return@forEach
            val methodHeader = referencedSelector.getParentOfType(ObjJMethodHeaderDeclaration::class.java) ?: return@forEach
            val description = if (methodHeader is ObjJMethodHeader) {
                methodHeader.description
            } else {
                methodHeader.genericDescription
            }
            //if (!out.contains(description))
                out.add(description)
        }
        return out.toTypedArray()
    }

    override fun updateParameterInfo(place:ObjJMethodCall, context: UpdateParameterInfoContext) {
        if (context.parameterOwner == null) {
            context.parameterOwner = place
        } else if (context.parameterOwner != findElementForParameterInfo(place)) {
            context.removeHint()
            LOGGER.warning("Parameter owner is not same element. ${context.parameterOwner?.tokenType()}(${context.parameterOwner.text}) != ${findElementForParameterInfo(place)?.tokenType()}}(${findElementForParameterInfo(place)?.text}})")
            return
        }
        val objects = context.objectsToView

        for (i in objects.indices) {
            context.setUIComponentEnabled(i, true)
        }
    }

    override fun updateUI(description: ObjJMethodDescription?, context: ParameterInfoUIContext) {
        if (description == null) {
            context.isUIComponentEnabled = false
            return
        }
        var parametersPresentableText:String? = description.parametersListPresentableText
        if (parametersPresentableText.isNullOrBlank()) {
            parametersPresentableText = CodeInsightBundle.message("parameter.info.no.parameters")
        }

        val parameterRange = description.getParameterRange(context.currentParameterIndex)
        context.setupUIComponentPresentation(
                parametersPresentableText,
                parameterRange.startOffset,
                parameterRange.endOffset,
                !context.isUIComponentEnabled,
                false,
                false,
                context.defaultParameterColor
        )
    }

    companion object {
        val LOGGER:Logger by lazy {
            Logger.getLogger("#"+ObjJFunctionParameterInfoHandler::class.java.canonicalName)
        }
    }

}