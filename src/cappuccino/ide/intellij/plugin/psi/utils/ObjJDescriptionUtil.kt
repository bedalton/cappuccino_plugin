package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement

object ObjJDescriptionUtil {

    fun getDescriptiveText(psiElement: PsiElement): String? {
        return when (psiElement) {
            is ObjJSelector -> getSelectorDescriptiveName(psiElement)
            is ObjJVariableName -> psiElement.text
            is ObjJClassName -> getClassDescriptiveText(psiElement)
            is ObjJFunctionName -> psiElement.getText()
            else -> ""
        }
    }

    private fun getClassDescriptiveText(classNameElement: ObjJClassName): String? {
        val classDeclarationElement = classNameElement.getParentOfType(ObjJClassDeclarationElement::class.java)
        var className = classNameElement.text
        if (classDeclarationElement == null || classDeclarationElement.getClassNameString() != className) {
            return className
        }
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            if (classDeclarationElement.categoryName != null) {
                className += " (" + classDeclarationElement.categoryName!!.className.text + ")"
            }
        }
        return className
    }

    private fun getSelectorDescriptiveName(selector: ObjJSelector): String {
        val selectorLiteral = selector.getParentOfType(ObjJSelectorLiteral::class.java)
        if (selectorLiteral != null) {
            return "@selector(" + selectorLiteral.selectorString + ")"
        }
        val variableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
        if (variableDeclaration != null) {
            val property = selector.getParentOfType(ObjJAccessorProperty::class.java)
            val propertyString = if (property != null) property.accessorPropertyType.text + "=" else ""
            val returnType = if (variableDeclaration.stub != null) variableDeclaration.stub.varType else variableDeclaration.formalVariableType.text
            return "- (" + returnType + ") @accessors(" + propertyString + selector.getSelectorString(false) + ")"
        }
        val methodCall = selector.getParentOfType(ObjJMethodCall::class.java)
        var selectorString: String? = null
        if (methodCall != null) {
            selectorString = methodCall.selectorString
        }
        if (selectorString == null) {
            val methodHeader = selector.getParentOfType(ObjJMethodHeaderDeclaration::class.java)
            if (methodHeader != null) {
                selectorString = if (methodHeader is ObjJMethodHeader) getFormattedSelector((methodHeader as ObjJMethodHeader?)!!) else methodHeader.selectorString
                val methodScopeString = if (methodHeader.isStatic) "+" else "-"
                return methodScopeString + " (" + methodHeader.returnType + ")" + selectorString
            }
        }
        selectorString = selectorString ?: selector.getSelectorString(true)
        return "[* $selectorString]"
    }

    private fun getFormattedSelector(methodHeader: ObjJMethodHeader): String {
        val builder = StringBuilder()
        for (selector in methodHeader.methodDeclarationSelectorList) {
            ProgressIndicatorProvider.checkCanceled()
            if (selector.selector != null) {
                builder.append(selector.selector!!.getSelectorString(false))
            }
            builder.append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            if (selector.formalVariableType != null) {
                builder.append("(").append(selector.formalVariableType!!.text).append(")")
            }
            if (selector.variableName != null) {
                builder.append(selector.variableName!!.text)
            }
            builder.append(" ")
        }
        return builder.substring(0, builder.length - 1)
    }
}