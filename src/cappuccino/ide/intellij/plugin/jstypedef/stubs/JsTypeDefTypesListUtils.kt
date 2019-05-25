package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.toStubParameter
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank


fun List<JsTypeDefType>.toJsTypeDefTypeListTypes() : List<JsTypeDefTypeListType> {
    val out = mutableListOf<JsTypeDefTypeListType>()

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

fun JsTypeDefFunctionReturnType.toTypeListType() : JsTypeDefTypesList? {
    val types = typeList.toJsTypeDefTypeListTypes()
    if (types.isEmpty())
        return null
    val nullable = isNullable
    return JsTypeDefTypesList(types, nullable)
}

fun JsTypeDefAnonymousFunction.toTypeListType() : JsTypeDefTypeListType.JsTypeDefTypeListAnonymousFunctionType {
    val parameters = this.propertiesList?.propertyList?.toTypeListTypes() ?: emptyList()
    val returnType = this.functionReturnType?.toTypeListType()
    return JsTypeDefTypeListType.JsTypeDefTypeListAnonymousFunctionType(parameters, returnType)
}

fun JsTypeDefArrayType.toTypeListType() : JsTypeDefTypeListType.JsTypeDefTypeListArrayType {
    val types = genericTypeTypes?.typeList?.toJsTypeDefTypeListTypes() ?: emptyList()
    val dimensions = if (arrayDimensions?.integer?.text.isNotNullOrBlank()) Integer.parseInt(arrayDimensions?.integer?.text) else 1
    return JsTypeDefTypeListType.JsTypeDefTypeListArrayType(types, dimensions)
}

fun JsTypeDefMapType.toTypeListType() : JsTypeDefTypeListType.JsTypeDefTypeListMapType {
    val keys = this.keyTypes.typeList.toJsTypeDefTypeListTypes()
    val valueTypes = this.valueTypes.typeList.toJsTypeDefTypeListTypes()
    return JsTypeDefTypeListType.JsTypeDefTypeListMapType(keys, valueTypes)
}

fun JsTypeDefKeyOfType.toTypeListType() : JsTypeDefTypeListType.JsTypeDefTypeListKeyOfType {
    val genericKey = this.genericsKey.text
    val mapName = this.typeMapName?.text ?: "???"
    return JsTypeDefTypeListType.JsTypeDefTypeListKeyOfType(genericKey, mapName)
}

fun JsTypeDefValueOfKeyType.toTypeListType() : JsTypeDefTypeListType.JsTypeDefTypeListValueOfKeyType {
    val genericKey = this.genericsKey?.text ?: "???"
    val mapName = this.typeMapName.text
    return JsTypeDefTypeListType.JsTypeDefTypeListValueOfKeyType(genericKey, mapName)
}