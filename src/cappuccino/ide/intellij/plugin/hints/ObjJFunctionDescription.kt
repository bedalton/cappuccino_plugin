package cappuccino.ide.intellij.plugin.hints

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.anyTypes
import cappuccino.ide.intellij.plugin.inference.toClassListString
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.utils.nullable
import cappuccino.ide.intellij.plugin.psi.utils.parameterType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.util.TextRange


class ObjJFunctionDescription(val name:String, val returnType:InferenceResult?) {

    var description:String? = null

    val parameters:List<ObjJFunctionParameterDescription> get() { return  _parameters }

    private val _parameters:MutableList<ObjJFunctionParameterDescription> = mutableListOf()

    fun addParameter(parameter:ObjJFunctionParameterDescription) {
        _parameters.add(parameter)
    }

    val presentableText:String get() {
        val stringBuilder = StringBuilder(name)
        stringBuilder
                .append("(")
                .append(parametersListPresentableText)
                .append(")")
        val returnTypeString = returnType?.toClassListString(null)
        if (returnTypeString.isNotNullOrBlank()) {
            stringBuilder
                    .append(" => ")
                    .append(returnTypeString)
        }
        return stringBuilder.toString()

    }

    val parametersListPresentableText:String get() {
        val stringBuilder:StringBuilder = StringBuilder()
        val parameters = mutableListOf<ObjJFunctionParameterDescription>()
        parameters.addAll(_parameters)
        parameters.forEachIndexed{ i, parameter ->
            stringBuilder.append(parameter.presentableText)
            if (i+1 != parameters.size)
                stringBuilder.append(", ")
        }
        return stringBuilder.toString()
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
                    return TextRange(startOffset + shift, startOffset + shift + parameters[i].name.length)
                }
                if (i > 0) {
                    startOffset += ", ".length
                }
                startOffset += parameters[i].name.length
                i++
            }
        }
        return TextRange(0, 0)
    }

}

class ObjJFunctionParameterDescription(val name:String, val type:String?, val nullable:Boolean) {

    var description:String? = null

    val presentableText:String get() {
        val stringBuilder = StringBuilder(name)
        if (type.isNotNullOrBlank() && type !in anyTypes) {
            stringBuilder.append(":").append(type)
        }
        if (nullable.orFalse()) {
            stringBuilder.append("?")
        }
        return stringBuilder.toString()
    }
}

val ObjJFormalParameterArg.description:ObjJFunctionParameterDescription get() {
    val parameterName = this.variableName?.text ?: "_"
    val parameterType =  this.parameterType
    val nullable = this.nullable
    return ObjJFunctionParameterDescription(
            name = parameterName,
            type = parameterType,
            nullable = nullable)
}

val JsTypeListType.JsTypeListFunctionType.description:ObjJFunctionDescription get() {
    val name = this.name ?: "_"
    val returnType = this.returnType
    val description = ObjJFunctionDescription(name, returnType)
    this.parameters.forEach {
        description.addParameter(it.description)
    }
    return description
}

val JsTypeDefFunctionArgument.description:ObjJFunctionParameterDescription get() {
    val descriptionOut = ObjJFunctionParameterDescription(name, types.toString(), nullable)
    descriptionOut.description = comment
    return descriptionOut
}