package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.toStubParameter
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank


fun List<JsTypeDefType>.toJsTypeDefTypeListTypes() : Set<JsTypeListType> {
    val out = mutableSetOf<JsTypeListType>()

    for (type in this) {
        val asAnonymousFunction = type.anonymousFunction?.toTypeListType()
        if (asAnonymousFunction != null) {
            out.add(asAnonymousFunction)
        }
        val asArrayType = type.arrayType?.toTypeListType()
        if (asArrayType != null)
            out.add(asArrayType)

        val asMapType = type.mapType?.toTypeListType()
        if (asMapType != null)
            out.add(asMapType)

        val asKeyOfType = type.keyOfType?.toTypeListType()
        if (asKeyOfType != null)
            out.add(asKeyOfType)

        val asValueOfType = type.valueOfKeyType?.toTypeListType()
        if (asValueOfType != null)
            out.add(asValueOfType)
    }
    return out
}

fun Iterable<JsTypeDefProperty>.toTypeListTypes() : List<JsTypeDefNamedProperty> {
    val properties = mutableListOf<JsTypeDefNamedProperty>()
    for (property in this) {
        properties.add(property.toStubParameter())
    }
    return properties
}

fun JsTypeDefFunctionReturnType.toTypeListType() : InferenceResult? {
    val types = typeList.toJsTypeDefTypeListTypes()
    if (types.isEmpty())
        return null
    val nullable = isNullable
    return InferenceResult(types, nullable)
}

fun JsTypeDefAnonymousFunction.toTypeListType() : JsTypeListType.JsTypeListFunctionType {
    val parameters = this.propertiesList?.propertyList?.toTypeListTypes() ?: emptyList()
    val returnType = this.functionReturnType?.toTypeListType()
    return JsTypeListType.JsTypeListFunctionType(parameters, returnType)
}

fun JsTypeDefArrayType.toTypeListType() : JsTypeListType.JsTypeListArrayType {
    val types = genericTypeTypes?.typeList?.toJsTypeDefTypeListTypes() ?: emptyList()
    val dimensions = if (arrayDimensions?.integer?.text.isNotNullOrBlank()) Integer.parseInt(arrayDimensions?.integer?.text) else 1
    return JsTypeListType.JsTypeListArrayType(types, dimensions)
}

fun JsTypeDefMapType.toTypeListType() : JsTypeListType.JsTypeListMapType {
    val keys = this.keyTypes.typeList.toJsTypeDefTypeListTypes()
    val valueTypes = this.valueTypes.typeList.toJsTypeDefTypeListTypes()
    return JsTypeListType.JsTypeListMapType(keys, valueTypes)
}

fun JsTypeDefKeyOfType.toTypeListType() : JsTypeListType.JsTypeListKeyOfType {
    val genericKey = this.genericsKey.text
    val mapName = this.typeMapName?.text ?: "???"
    return JsTypeListType.JsTypeListKeyOfType(genericKey, mapName)
}

fun JsTypeDefValueOfKeyType.toTypeListType() : JsTypeListType.JsTypeListValueOfKeyType {
    val genericKey = this.genericsKey?.text ?: "???"
    val mapName = this.typeMapName.text
    return JsTypeListType.JsTypeListValueOfKeyType(genericKey, mapName)
}