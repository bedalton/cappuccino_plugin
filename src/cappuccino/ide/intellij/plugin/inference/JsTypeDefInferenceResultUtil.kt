package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*

fun PropertiesMap.toJsTypeDefInterfaceBody() : JsTypeListClass {
    val functionProperties = mutableListOf<JsTypeListFunctionType>()
    val namedProperties = mutableListOf<JsTypeDefNamedProperty>()
    forEach {(key, type) ->
        val function = type.functionTypes.getOrNull(0)
        if (function != null) {
            val typeDefFunction = JsTypeListFunctionType(
                    name = key,
                    parameters = function.parameters,
                    returnType = function.returnType ?: INFERRED_VOID_TYPE,
                    isStatic = false
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
    return JsTypeListClass(
            allProperties = namedProperties.toSet(),
            allFunctions = functionProperties.toSet()
    )
}