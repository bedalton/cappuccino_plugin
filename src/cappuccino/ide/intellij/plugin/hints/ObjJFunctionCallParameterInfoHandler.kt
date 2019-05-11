package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiElement

class ObjJFunctionCallParameterInfoHandler : ParameterInfoHandler<ObjJFunctionCall, ObjJFunctionDeclaration> {

    override fun couldShowInLookup(): Boolean {
        return true;
    }

    override fun showParameterInfo(element: ObjJFunctionCall, context: CreateParameterInfoContext) {
        context.showHint(element, element.textRange.startOffset, this);
    }

    override fun updateParameterInfo(functionCall: ObjJFunctionCall, context: UpdateParameterInfoContext) {}

    override fun updateUI(functionDeclaration: ObjJFunctionDeclaration, context: ParameterInfoUIContext) {
        val sb = StringBuilder()
        var hlStart = -1;
        var hlEnd = -1;
        val names = functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
        for (i in  0 until names.size) {
            if (sb.isNotEmpty()) {
                sb.append("; ");
            }
            if (i == context.currentParameterIndex) {
                hlStart = sb.length;
            }
            val parameterType = ObjJFunctionDeclarationPsiUtil.getParameterType(names[i])
            if (parameterType != null) {
                sb.append(":").append(parameterType)
            }
            sb.append(names[i].text)

        }
    }

    override fun getParametersForLookup(lookupElement: LookupElement?, context: ParameterInfoContext?): Array<Any>? {
        return null
    }

    override fun getParametersForDocumentation(parameter: ObjJFunctionDeclaration, context: ParameterInfoContext): Array<Any>? {
        return null
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ObjJFunctionCall? {
        val functionCall = getCallExpr(context) ?: return null
        val index = ParameterInfoUtils.getCurrentParameterIndex(functionCall.arguments.node, context.offset, ObjJTypes.ObjJ_COMMA)
        context.setCurrentParameter(index);
        return functionCall
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): ObjJFunctionCall? {

    }

    private fun getCallExpr(context: ParameterInfoContext?) : ObjJFunctionCall? {
        val file = context?.file ?: return null
        val element = file.findElementAt(context.offset) ?: return null
        return getCallExpr(element)
    }

    private fun getCallExpr(elementIn:PsiElement) : ObjJFunctionCall? {
        val element = elementIn as? ObjJCompositeElement ?: return null
        var functionCall = element.getSelfOrParentOfType(ObjJFunctionCall::class.java)
        if (functionCall == null) {
            val prevSibling = element.getPreviousNonEmptySibling(true)
            functionCall = prevSibling?.getSelfOrParentOfType(ObjJFunctionCall::class.java)
        }
        return functionCall
    }

}