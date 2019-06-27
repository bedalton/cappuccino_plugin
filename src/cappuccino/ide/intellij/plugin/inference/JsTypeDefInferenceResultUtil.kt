package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsFunction
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*

fun PropertiesMap.toJsTypeDefInterfaceBody() : JsTypeListInterfaceBody {
    val functionProperties = mutableListOf<JsFunction>()
    val namedProperties = mutableListOf<JsTypeDefNamedProperty>()
    forEach {(key, type) ->
        val function = type.functionTypes?.getOrNull(0)
        if (function != null) {
            val typeDefFunction = JsFunction(
                    name = key,
                    parameters = function.parameters,
                    returnType = function.returnType ?: INFERRED_VOID_TYPE,
                    static = false
            )
            functionProperties.add(typeDefFunction)
        } else {
            val property = JsTypeDefNamedProperty(
                    name = key,
                    types = type,
                    readonly = false,
                    static = false
            )
            namedProperties.add(property)
        }
    }
    return JsTypeListInterfaceBody(
            properties = namedProperties.toSet(),
            functions = functionProperties.toSet()
    )
}