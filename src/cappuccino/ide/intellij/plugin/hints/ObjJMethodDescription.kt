package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclarationSelector
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReference
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.openapi.util.TextRange

data class ObjJMethodDescription(val className:String, private val returnType:String?, val parameters:MutableList<ObjJMethodParameterDescription> = mutableListOf()) {

    fun addParameter(param:ObjJMethodParameterDescription) {
        parameters.add(param)
    }

    val presentableText:String get() {
        val stringBuilder = StringBuilder()
                .append("[")
                .append(className)
                .append(" ")
                .append(parametersListPresentableText)
                .append("]")
        if (returnType.isNotNullOrBlank()) {
            stringBuilder.append(" : ").append(returnType)
        }
        stringBuilder.append("in [").append(className).append("]")
        return stringBuilder.toString()
    }

    val parametersListPresentableText:String get() {
        val stringBuilder = StringBuilder()
        val lastParameterIndex = parameters.size
        parameters.forEachIndexed { i, parameter ->
            if (i < lastParameterIndex)
                stringBuilder.append(" ")
            stringBuilder.append(parameter.presentableText)
        }
        return stringBuilder.toString().trim()
    }

    fun getParameterRange(index: Int): TextRange {
        if (index == -1) {
            return TextRange(0, 0)
        }
        var startOffset = 0
        run {
            var i = 0
            val length = parameters.size
            while (i < length) {
                if (i == index) {
                    val shift = if (i == 0) 0 else ", ".length
                    return TextRange(startOffset + shift, startOffset + shift + parameters[i].selector.length)
                }
                if (i > 0) {
                    startOffset += ", ".length
                }
                startOffset += parameters[i].selector.length
                i++
            }
        }
        return TextRange(0, 0)
    }
}

data class ObjJMethodParameterDescription(internal val selector:String, val containingClass:String, private val type:String?, private val parameterName:String?) {
    val presentableText:String get() {
        val stringBuilder:StringBuilder = java.lang.StringBuilder(selector)
        if (parameterName.isNotNullOrBlank() || type.isNotNullOrBlank()) {
            stringBuilder.append(":")
        }
        if (type.isNotNullOrBlank()) {
            stringBuilder.append("(").append(type).append(")")
        }
        if (parameterName.isNotNullOrBlank()) {
            stringBuilder.append(parameterName)
        }

        if (containingClass.isNotBlank()) {
            stringBuilder.append (" in class ").append(containingClass)
        }

        return stringBuilder.toString()
    }
}

val ObjJMethodHeader.description:ObjJMethodDescription get() {
    val description = ObjJMethodDescription(this.containingClassName, this.getReturnTypes( createTag()).joinToString("|"))
    this.methodDeclarationSelectorList.forEach {
        description.addParameter(it.description)
    }
    return description
}

val ObjJMethodHeaderDeclaration<*>.genericDescription:ObjJMethodDescription get() {
    val description = ObjJMethodDescription(this.containingClassName, this.getReturnTypes( createTag()).joinToString("|"))
    this.selectorStrings.forEach {
        val selectorDescription = ObjJMethodParameterDescription(it, this.containingClassName,null, "_")
        description.addParameter(selectorDescription)
    }
    return description
}

val ObjJMethodDeclarationSelector.description:ObjJMethodParameterDescription get() {
    return ObjJMethodParameterDescription(this.selector?.text ?: "_", this.containingClassName, this.formalVariableType?.text, this.variableName?.text)
}

val ObjJSelector.description:ObjJMethodParameterDescription? get() {
    val parentMethodDeclarationSelector = this.getParentOfType(ObjJMethodDeclarationSelector::class.java)
    if (parentMethodDeclarationSelector != null) {
        return parentMethodDeclarationSelector.description
    }
    val references = ObjJSelectorReference(this).multiResolve(false)
    if (references.isEmpty()) {
        return null
    }
    if (references.size == 1) {
        val declaration = references.getOrNull(0)?.element?.getSelfOrParentOfType(ObjJMethodDeclarationSelector::class.java) ?: return null
        return declaration.description
    }
    val allKinds = mutableListOf<String>()
    val parameterNames = mutableListOf<String>()
    val containingClassNames = mutableListOf<String>()
    for(reference in references) {
        val declaration = reference.element?.getSelfOrParentOfType(ObjJMethodDeclarationSelector::class.java) ?: continue
        val type = declaration.formalVariableType?.varTypeId?.idType ?: declaration.formalVariableType?.text ?: continue
        if (!allKinds.contains(type))
            allKinds.add(type)
        val containingClass = this.containingClassName
        if (!containingClassNames.contains(containingClass)) {
            containingClassNames.add(containingClassName)
        }
        val parameterName = declaration.variableName?.text ?: continue
        if (!parameterNames.contains(parameterName))
            parameterNames.add(parameterName)
    }
    val types = allKinds.joinToString("|")
    val names = parameterNames.joinToString("|")
    return ObjJMethodParameterDescription(selector = this.text, containingClass = containingClassNames.joinToString("|"), type = types, parameterName = names)
}