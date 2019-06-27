package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.inference.INFERRED_EMPTY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.JsFunctionType
import cappuccino.ide.intellij.plugin.inference.PropertiesMap
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsFunction
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.TypeListType
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream


private fun StubInputStream.readTypesList(): List<JsTypeListType> {
    val types: MutableList<JsTypeListType> = mutableListOf()
    val numberOfTypes = readInt()
    for (i in 0 until numberOfTypes) {
        val type = readType() ?: continue
        types.add(type)
    }
    return types
}

private fun StubInputStream.readType(): JsTypeListType? {
    return when (TypeListType.forKey(readInt())) {
        TypeListType.BASIC -> readBasicType()
        TypeListType.ARRAY -> readArrayType()
        TypeListType.KEYOF -> readKeyOfType()
        TypeListType.VALUEOF -> readValueOfKeyType()
        TypeListType.MAP -> readMapType()
        TypeListType.ANONYMOUS_FUNCTION -> readAnonymousFunctionType()
        TypeListType.INTERFACE_BODY -> readInterfaceBodyType()
    }
}


private fun StubInputStream.readBasicType(): JsTypeListBasicType? {
    val type = readNameString() ?: return null
    return JsTypeListBasicType(type)
}

private fun StubInputStream.readKeyOfType(): JsTypeListKeyOfType? {
    val key = readNameString()
    val mapName = readNameString()
    if (key == null || mapName == null)
        return null
    return JsTypeListKeyOfType(key, mapName)
}

private fun StubInputStream.readValueOfKeyType(): JsTypeListValueOfKeyType? {
    val key = readNameString()
    val mapName = readNameString()
    if (key == null || mapName == null)
        return null
    return JsTypeListValueOfKeyType(key, mapName)
}


private fun StubInputStream.readInterfaceBodyType(): JsTypeListInterfaceBody {
    val properties = readPropertiesList()
    val numFunctions = readInt()
    val functions = mutableListOf<JsFunction>()
    for (i in 0 until numFunctions) {
        functions.add(readFunction())
    }
    return JsTypeListInterfaceBody(properties = properties.toSet(), functions = functions.toSet())
}

private fun StubInputStream.readMapType(): JsTypeListMapType {
    val keys = readTypesList()
    val valueTypes = readTypesList()
    return JsTypeListMapType(keys.toSet(), valueTypes.toSet())
}

private fun StubInputStream.readAnonymousFunctionType(): JsTypeListFunctionType {
    val name = readNameString()
    val parameters = readPropertiesList()
    val returnType = readInferenceResult()
    return JsTypeListFunctionType(name = name, parameters = parameters, returnType = returnType)
}

fun StubInputStream.readPropertiesList(): List<JsTypeDefNamedProperty> {
    val numProperties = readInt()
    val properties = mutableListOf<JsTypeDefNamedProperty>()
    for (i in 0 until numProperties) {
        val property = readProperty() ?: continue
        properties.add(property)
    }
    return properties
}


private fun StubInputStream.readProperty(): JsTypeDefNamedProperty? {
    val propertyName = readNameString()
    val types = readInferenceResult()
    val readonly = readBoolean()
    val static = readBoolean()
    if (propertyName == null)
        return null
    return JsTypeDefNamedProperty(propertyName, types, readonly, static)
}

private fun StubInputStream.readFunction(): JsFunction {
    val functionName = readNameString()
    val parameters = readPropertiesList()
    val returnType = readInferenceResult()
    val static = readBoolean()
    return JsFunction(name = functionName, parameters = parameters, returnType = returnType, static = static)
}


private fun StubOutputStream.writeTypeList(types: Set<JsTypeListType>) {
    writeInt(types.size)
    for (type in types) {
        writeType(type)
    }
}


private fun StubOutputStream.writeType(type: JsTypeListType) {
    when (type) {
        is JsTypeListBasicType -> writeBasicType(type)
        is JsTypeListArrayType -> writeArrayType(type)
        is JsTypeListMapType -> writeMapType(type)
        is JsTypeListKeyOfType -> writeKeyOfType(type)
        is JsTypeListValueOfKeyType -> writeValueOfKeyType(type)
        is JsTypeListInterfaceBody -> writeInterfaceBody(type)
        is JsTypeListFunctionType -> writeAnonymousFunctionType(type)
    }
}

private fun StubOutputStream.writeBasicType(basicType: JsTypeListBasicType) {
    writeInt(TypeListType.BASIC.id)
    writeName(basicType.typeName)
}

private fun StubInputStream.readArrayType(): JsTypeListArrayType {
    val types = readTypesList()
    val dimensions = readInt()
    return JsTypeListArrayType(types.toSet(), dimensions)
}

private fun StubOutputStream.writeArrayType(type: JsTypeListArrayType) {
    writeInt(TypeListType.ARRAY.id)
    writeTypeList(type.types)
    writeInt(type.dimensions)
}

private fun StubOutputStream.writeKeyOfType(type: JsTypeListKeyOfType) {
    writeInt(TypeListType.KEYOF.id)
    writeName(type.genericKey)
    writeName(type.mapName)
}

private fun StubOutputStream.writeValueOfKeyType(type: JsTypeListValueOfKeyType) {
    writeInt(TypeListType.VALUEOF.id)
    writeName(type.genericKey)
    writeName(type.mapName)
}

private fun StubOutputStream.writeInterfaceBody(body: JsTypeListInterfaceBody) {
    writeInt(TypeListType.INTERFACE_BODY.id)
    writePropertiesList(body.properties.toList())
    writeInt(body.functions.size)
    for (function in body.functions)
        writeFunction(function)
}


private fun StubOutputStream.writeAnonymousFunctionType(function: JsTypeListFunctionType) {
    writePropertiesList(function.parameters)
    writeInferenceResult(function.returnType ?: INFERRED_EMPTY_TYPE)
}

private fun StubOutputStream.writeFunction(function: JsFunction) {
    writeName(function.name)
    writePropertiesList(function.parameters)
    writeInferenceResult(function.returnType)
    writeBoolean(function.static)
}

fun StubOutputStream.writePropertiesList(properties: List<JsTypeDefNamedProperty>) {
    writeInt(properties.size)
    for (property in properties) {
        writeProperty(property)
    }
}

private fun StubOutputStream.writeProperty(type: JsTypeDefNamedProperty) {
    writeName(type.name)
    writeInferenceResult(type.types)
    writeBoolean(type.readonly)
    writeBoolean(type.static)
}

private fun StubOutputStream.writeMapType(mapType: JsTypeListMapType) {
    writeInt(TypeListType.MAP.id)
    writeTypeList(mapType.keyTypes)
    writeTypeList(mapType.valueTypes)
}

fun List<JsTypeListInterfaceBody>.collapse(): JsTypeListInterfaceBody {
    val propertiesList = this.flatMap { it.properties }.toSet()
    val functionList = this.flatMap { it.functions }.toSet()
    return JsTypeListInterfaceBody(propertiesList, functionList)
}


fun StubOutputStream.writeJsFunctionType(function: JsFunctionType?) {
    writeBoolean(function != null)
    if (function == null)
        return
    writePropertiesList(function.parameters)
    writeInferenceResult(function.returnType)
    writeUTFFast(function.comment ?: "")
}

fun StubInputStream.readJsFunctionType(): JsFunctionType? {
    if (!readBoolean())
        return null
    val parameters = readPropertiesList()
    val returnType = readInferenceResult()
    val comment = readUTFFast()
    return JsFunctionType(
            parameters = parameters,
            returnType = returnType,
            comment = if (comment.isNotBlank()) comment else null
    )
}

fun StubOutputStream.writePropertiesMap(map: PropertiesMap) {
    writeInt(map.size)
    map.forEach { (key, value) ->
        writeName(key)
        writeInferenceResult(value)
    }
}

fun StubInputStream.readPropertiesMap(): PropertiesMap {
    val numProperties = readInt()
    val out = mutableMapOf<String, InferenceResult>()
    for (i in 0 until numProperties) {
        val name = readNameString() ?: "?"
        out[name] = readInferenceResult()
    }
    return out
}


fun StubOutputStream.writeInferenceResult(result: InferenceResult) {
    writeTypeList(result.types)
    writeBoolean(result.nullable)
}

fun StubInputStream.readInferenceResult(): InferenceResult {
    val types = readTypesList()
    val nullable = readBoolean()
    return InferenceResult(types = types.toSet(), nullable = nullable)
}

internal fun StubOutputStream.writeJsFunctionList(functions: List<JsFunction>?) {
    writeBoolean(functions != null)
    if (functions == null)
        return
    writeInt(functions.size)
    functions.forEach {
        writeJsFunctionType(it)
    }
}


internal fun StubInputStream.readJsFunctionList(): List<JsFunctionType>? {
    if (!readBoolean())
        return null
    val numFunctions = readInt()
    return (0 until numFunctions).mapNotNull {
        readJsFunctionType()
    }
}