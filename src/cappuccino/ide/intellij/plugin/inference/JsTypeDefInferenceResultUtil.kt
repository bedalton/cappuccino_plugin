package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsFunction
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypesList


fun InferenceResult.toJsTypeDefTypesList() : JsTypesList {
    val out = mutableListOf<JsTypeListType>()
    val basicTypes = toClassList("?").map {
        JsTypeListBasicType(it)
    }
    out.addAll(basicTypes)
    if (arrayTypes != null) {
        val types = arrayTypes.map {
            JsTypeListBasicType(it)
        }
        val dimensions = 1
        val arrayType = JsTypeListArrayType(types, dimensions)
        out.add(arrayType)
    }

    val interfaceBody = jsObjectKeys?.toJsTypeDefInterfaceBody()
    if (interfaceBody != null)
        out.add(interfaceBody)
    return JsTypesList(out, true)
}

fun PropertiesMap.toJsTypeDefInterfaceBody() : JsTypeListInterfaceBody {
    val functionProperties = mutableListOf< JsFunction>()
    val namedProperties = mutableListOf<JsTypeDefNamedProperty>()
    forEach {(key, type) ->
        val function = type.functionTypes?.getOrNull(0)
        if (function != null) {
            val typeDefFunction = JsFunction(
                    name = key,
                    parameters = function.parameters,
                    returnType = function.returnType.toJsTypeDefTypesList(),
                    static = false
            )
            functionProperties.add(typeDefFunction)
        } else {
            val property = JsTypeDefNamedProperty(
                    name = key,
                    types = type.toJsTypeDefTypesList(),
                    readonly = false,
                    static = false
            )
            namedProperties.add(property)
        }
    }
    return JsTypeListInterfaceBody(
            properties = namedProperties,
            functions = functionProperties
    )
}

fun JsTypesList.toInferenceResult() : InferenceResult {
    val classes = basicTypes.map {
        it.typeName
    }
    var functions: List<JsFunctionType>? = anonymousFunctionTypes.map {
        it.toJsFunctionType()
    }
    if (functions.isNullOrEmpty())
        functions = null
    var arrayTypes:List<String>? = arrayTypes.types.map { it.typeName }
    if (arrayTypes.isNullOrEmpty())
        arrayTypes = null
    var jsObjectKeys:MutableMap<String, InferenceResult>? = mutableMapOf()
    interfaceTypes.forEach {body ->
        body.properties.forEach{ property ->
            if (jsObjectKeys!!.containsKey(property.name)) {
                jsObjectKeys!![property.name] = jsObjectKeys!![property.name]!! + property.types.toInferenceResult()
            } else {
                jsObjectKeys!![property.name] = property.types.toInferenceResult()
            }
        }
    }
    if (jsObjectKeys.isNullOrEmpty())
        jsObjectKeys = null
    return InferenceResult(
            classes = classes.toSet(),
            jsObjectKeys = jsObjectKeys,
            arrayTypes = arrayTypes?.toSet(),
            functionTypes = functions
    )
}

fun JsTypeListAnonymousFunctionType.toJsFunctionType() : JsFunctionType {
    val parameters = parameters.map { parameter ->
        JsTypeDefNamedProperty(
                name = parameter.name,
                types = parameter.types,
                readonly = false,
                static = false
        )
    }
    return JsFunctionType (
        parameters = parameters,
        returnType = returnType?.toInferenceResult() ?: INFERRED_VOID_TYPE,
        comment = null
    )
}



fun List<String>.toJsTypeList() : JsTypesList {
    val types = this.map {
        JsTypeListBasicType(it)
    }
    return JsTypesList(types, true)
}