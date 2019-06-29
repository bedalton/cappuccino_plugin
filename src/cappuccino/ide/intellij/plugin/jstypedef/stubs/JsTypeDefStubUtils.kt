package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.TypeListType
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream


fun StubInputStream.readTypesList(): List<JsTypeListType> {
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
        TypeListType.FUNCTION -> readJsFunctionType()
        TypeListType.INTERFACE_BODY -> readInterfaceBodyType()
        TypeListType.UNION_TYPE -> readUnionType()
    }
}


private fun StubInputStream.readBasicType(): JsTypeListBasicType? {
    val type = readNameString() ?: return null
    return JsTypeListBasicType(type)
}

private fun StubInputStream.readArrayType(): JsTypeListArrayType {
    val types = readTypesList()
    val dimensions = readInt()
    return JsTypeListArrayType(types.toSet(), dimensions)
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


private fun StubInputStream.readInterfaceBodyType(): JsTypeListClass {
    val properties = readPropertiesList()
    val functions = readJsFunctionList()
    return JsTypeListClass(allProperties = properties.toSet(), allFunctions = functions ?: emptySet())
}

private fun StubInputStream.readMapType(): JsTypeListMapType {
    val keys = readTypesList()
    val valueTypes = readTypesList()
    return JsTypeListMapType(keys.toSet(), valueTypes.toSet())
}

private fun StubInputStream.readUnionType() : JsTypeListUnionType {
    val numTypes = readInt()
    val typeNames = (0 until numTypes).mapNotNull {
        readNameString()
    }.toSet()
    return JsTypeListUnionType(typeNames)
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
    val comment = readUTFFast()
    val default = readNameString()
    if (propertyName == null)
        return null
    return JsTypeDefNamedProperty(
            name = propertyName,
            types = types ?: INFERRED_ANY_TYPE,
            readonly = readonly,
            static = static,
            comment = if (comment.isBlank()) null else comment,
            default = default
    )
}

fun StubInputStream.readFunctionArgumentsList(): List<JsTypeDefFunctionArgument> {
    val numProperties = readInt()
    val properties = mutableListOf<JsTypeDefFunctionArgument>()
    for (i in 0 until numProperties) {
        val property = readFunctionProperty() ?: continue
        properties.add(property)
    }
    return properties
}


private fun StubInputStream.readFunctionProperty(): JsTypeDefFunctionArgument? {
    val propertyName = readNameString()
    val types = readInferenceResult()
    val comment = readUTFFast()
    val default = readNameString()
    val varArgs = readBoolean()
    if (propertyName == null)
        return null
    return JsTypeDefFunctionArgument(
            name = propertyName,
            types = types ?: INFERRED_ANY_TYPE,
            comment = if (comment.isBlank()) null else comment,
            default = default,
            varArgs = varArgs
    )
}

fun StubOutputStream.writeTypeList(types: Set<JsTypeListType>) {
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
        is JsTypeListClass -> writeInterfaceBody(type)
        is JsTypeListFunctionType -> writeJsFunctionType(type)
        is JsTypeListUnionType -> writeUnionType(type)
    }
}

private fun StubOutputStream.writeBasicType(basicType: JsTypeListBasicType) {
    writeInt(TypeListType.BASIC.id)
    writeName(basicType.typeName)
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

private fun StubOutputStream.writeInterfaceBody(body: JsTypeListClass) {
    writeInt(TypeListType.INTERFACE_BODY.id)
    writePropertiesList(body.allProperties)
    writeJsFunctionList(body.allFunctions)
}

private fun StubOutputStream.writeUnionType(type:JsTypeListUnionType) {
    writeInt(type.typeNames.size)
    for(typeName in type.typeNames)
        writeName(typeName)
}

fun StubOutputStream.writePropertiesList(propertiesIn: Iterable<JsTypeDefNamedProperty>) {
    val properties = propertiesIn as? List ?: propertiesIn.toList()
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
    writeUTFFast(type.comment ?: "")
    writeName(type.default)
}

fun StubOutputStream.writeFunctionArgumentsList(propertiesIn: Iterable<JsTypeDefFunctionArgument>) {
    val properties = propertiesIn as? List ?: propertiesIn.toList()
    writeInt(properties.size)
    for (property in properties) {
        writeFunctionProperty(property)
    }
}

private fun StubOutputStream.writeFunctionProperty(type: JsTypeDefFunctionArgument) {
    writeName(type.name)
    writeInferenceResult(type.types)
    writeUTFFast(type.comment ?: "")
    writeName(type.default)
    writeBoolean(type.varArgs)
}

private fun StubOutputStream.writeMapType(mapType: JsTypeListMapType) {
    writeInt(TypeListType.MAP.id)
    writeTypeList(mapType.keyTypes)
    writeTypeList(mapType.valueTypes)
}

fun StubOutputStream.writeJsFunctionType(function: JsTypeListFunctionType?) {
    writeBoolean(function != null)
    if (function == null)
        return
    writeName(function.name)
    writeFunctionArgumentsList(function.parameters)
    writeInferenceResult(function.returnType)
    writeUTFFast(function.comment ?: "")
}

fun StubInputStream.readJsFunctionType(): JsTypeListFunctionType? {
    if (!readBoolean())
        return null
    val name = readNameString()
    val parameters = readFunctionArgumentsList()
    val returnType = readInferenceResult()
    val comment = readUTFFast()
    val isStatic = readBoolean()
    return JsTypeListFunctionType(
            name = name,
            parameters = parameters,
            returnType = returnType,
            comment = if (comment.isNotBlank()) comment else null,
            static = isStatic
    )
}

fun StubOutputStream.writeInferenceResult(result: InferenceResult?) {
    val isNotNull = result != null
    writeBoolean(isNotNull)
    if (result == null)
        return
    writeTypeList(result.types)
    writeBoolean(result.nullable)
}

fun StubInputStream.readInferenceResult(): InferenceResult? {
    if (!readBoolean())
        return null
    val types = readTypesList()
    val nullable = readBoolean()
    return InferenceResult(types = types.toSet(), nullable = nullable)
}

internal fun StubOutputStream.writeJsFunctionList(functionsIn: Iterable<JsTypeListFunctionType>?) {
    writeBoolean(functionsIn != null)
    if (functionsIn == null)
        return
    val functions = functionsIn as? List ?: functionsIn.toList()
    writeInt(functions.size)
    functions.forEach {
        writeJsFunctionType(it)
    }
}


internal fun StubInputStream.readJsFunctionList(): Set<JsTypeListFunctionType>? {
    if (!readBoolean())
        return null
    val numFunctions = readInt()
    return (0 until numFunctions).mapNotNull {
        readJsFunctionType()
    }.toSet()
}