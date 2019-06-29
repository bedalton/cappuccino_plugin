package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.toStubParameter
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank


fun Iterable<JsTypeDefType>?.toJsTypeDefTypeListTypes() : Set<JsTypeListType> {
    val out = mutableSetOf<JsTypeListType>()
    if (this == null)
        return emptySet()
    for (type in this) {

        val asAnonymousFunction = type.anonymousFunction?.toTypeListType()
        if (asAnonymousFunction != null) {
            out.add(asAnonymousFunction)
            continue;
        }
        val asArrayType = type.arrayType?.toTypeListType()
        if (asArrayType != null) {
            out.add(asArrayType)
            continue
        }

        val asMapType = type.mapType?.toTypeListType()
        if (asMapType != null) {
            out.add(asMapType)
            continue
        }

        val asBasicType = type.typeName?.toTypeListType()
        if (asBasicType != null) {
            out.add(asBasicType)
            continue
        }
        val asUnionType = type.typeUnion?.toTypeListType()
        if (asUnionType != null) {
            out.add(asUnionType)
            continue
        }
    }
    return out
}

@Suppress("unused")
fun Iterable<JsTypeDefProperty>.toTypeListTypes() : List<JsTypeDefNamedProperty> {
    val properties = mutableListOf<JsTypeDefNamedProperty>()
    for (property in this) {
        properties.add(property.toStubParameter())
    }
    return properties
}


fun Iterable<JsTypeDefArgument>.toFunctionTypeListTypes() : List<JsTypeDefFunctionArgument> {
    val properties = mutableListOf<JsTypeDefFunctionArgument>()
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

fun JsTypeDefAnonymousFunction.toTypeListType() : JsTypeListFunctionType {
    val parameters = this.argumentsList?.arguments?.toFunctionTypeListTypes().orEmpty()
    val returnType = this.functionReturnType?.toTypeListType()
    return JsTypeListFunctionType(parameters= parameters, returnType = returnType, static = false)
}

fun JsTypeDefArrayType.toTypeListType() : JsTypeListArrayType {
    val types = typeList.toJsTypeDefTypeListTypes()
    val dimensions = if (arrayDimensions?.integer?.text.isNotNullOrBlank()) Integer.parseInt(arrayDimensions?.integer?.text) else 1
    return JsTypeListArrayType(types, dimensions)
}

fun JsTypeDefMapType.toTypeListType() : JsTypeListMapType {
    val keys = this.keyTypes?.typeList?.toJsTypeDefTypeListTypes().orEmpty()
    val valueTypes = this.valueTypes?.typeList.toJsTypeDefTypeListTypes()
    return JsTypeListMapType(keys, valueTypes)
}

fun JsTypeDefKeyOfType.toTypeListType() : JsTypeListKeyOfType {
    val genericKey = this.genericsKey.text
    val mapName = this.typeMapName?.text ?: "???"
    return JsTypeListKeyOfType(genericKey, mapName)
}

fun JsTypeDefValueOfKeyType.toTypeListType() : JsTypeListValueOfKeyType {
    val genericKey = this.genericsKey.text ?: "???"
    val mapName = this.typeMapName.text
    return JsTypeListValueOfKeyType(genericKey, mapName)
}

fun JsTypeDefTypeName.toTypeListType() : JsTypeListBasicType {
    return JsTypeListBasicType(this.text)
}

fun JsTypeDefTypeUnion.toTypeListType() : JsTypeListUnionType {
    val typeNames = this.typeNameList.map { it.text }.toSet()
    return JsTypeListUnionType(typeNames)
}