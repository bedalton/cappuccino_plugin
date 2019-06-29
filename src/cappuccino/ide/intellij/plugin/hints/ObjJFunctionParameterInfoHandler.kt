package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJArguments
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.functionDeclarationReference
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger


class ObjJFunctionParameterInfoHandler : ParameterInfoHandler<ObjJFunctionCall, ObjJFunctionDescription> {

    override fun couldShowInLookup(): Boolean {
        return true
    }

    override fun getParametersForLookup(lookupElement: LookupElement?, context: ParameterInfoContext?): Array<Any>? {
        val element = lookupElement?.`object` as? ObjJCompositeElement ?: return emptyArray()
        if (element is ObjJFunctionDeclarationElement<*>) {
            return arrayOf(element)
        }
        return emptyArray()
    }

    override fun getParametersForDocumentation(parameter: ObjJFunctionDescription, context: ParameterInfoContext): Array<Any>? {
        return null
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ObjJFunctionCall? {
        return getCallExpression(context.offset, context.file)
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): ObjJFunctionCall? {
        return getCallExpression(context.offset, context.file)
    }

    private fun findElementForParameterInfo(element:PsiElement): ObjJFunctionCall? {
        if (element is ObjJFunctionCall)
            return element
        val arguments = element.getSelfOrParentOfType(ObjJArguments::class.java)
        if (arguments == null) {
            //LOGGER.info("Failed to find parent or self of argument type")
        }
        return arguments?.parent as? ObjJFunctionCall
    }

    private fun getCallExpression(offset:Int, fileIn:PsiFile) : ObjJFunctionCall? {
        val file = fileIn as? ObjJFile ?: return null
        val arguments = PsiTreeUtil.findElementOfClassAtOffset(file, offset, ObjJArguments::class.java, false)
                ?: return null
        return arguments.parent as? ObjJFunctionCall
    }

    override fun getParameterCloseChars(): String? {
        return ",){}"
    }

    override fun tracksParameterIndex(): Boolean {
        return true
    }


    override fun showParameterInfo(element: ObjJFunctionCall, context: CreateParameterInfoContext) {
        val functionDeclaration = element.functionDeclarationReference
        val description = (if (functionDeclaration != null) {
            functionDeclaration.description
        } else {
            val functionName = element.functionName?.text
            val jsFunction = if(functionName != null) JsTypeDefFunctionsByNameIndex.instance[functionName, element.project].firstOrNull()?.toJsTypeListType() else null
            jsFunction?.toString()
        }) ?: return
        context.itemsToShow = arrayOf(description)
        context.showHint(element, element.textRange.startOffset, this)
    }

    override fun updateParameterInfo(place:ObjJFunctionCall, context: UpdateParameterInfoContext) {
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

    override fun updateUI(description: ObjJFunctionDescription?, context: ParameterInfoUIContext) {
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