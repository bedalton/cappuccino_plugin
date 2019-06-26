package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.toStubParameter
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank


fun List<JsTypeDefType>.toJsTypeDefTypeListTypes() : List<JsTypeListType> {
    val out = mutableListOf<JsTypeListType>()

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

fun List<JsTypeDefProperty>.toTypeListTypes() : List<JsTypeDefNamedProperty> {
    val properties = mutableListOf<JsTypeDefNamedProperty>()
    for (property in this) {
        properties.add(property.toStubParameter())
    }
    return properties
}

fun JsTypeDefFunctionReturnType.toTypeListType() : JsTypesList? {
    val types = typeList.toJsTypeDefTypeListTypes()
    if (types.isEmpty())
        return null
    val nullable = isNullable
    return JsTypesList(types, nullable)
}

fun JsTypeDefAnonymousFunction.toTypeListType() : JsTypeListType.JsTypeListAnonymousFunctionType {
    val parameters = this.propertiesList?.propertyList?.toTypeListTypes() ?: emptyList()
    val returnType = this.functionReturnType?.toTypeListType()
    return JsTypeListType.JsTypeListAnonymousFunctionType(parameters, returnType)
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