package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeListType.*
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

fun StubInputStream.readTypes():JsTypeDefTypesList {
    val types = readTypesList()
    val nullable = readBoolean()
    return JsTypeDefTypesList(types, nullable)
}


private fun StubInputStream.readTypesList():List<JsTypeDefTypeListType> {
    val types : MutableList<JsTypeDefTypeListType> = mutableListOf()
    val numberOfTypes = readInt()
    for(i in 0 until numberOfTypes) {
        val type = readType() ?: continue
        types.add(type)
    }
    return types
}

private fun StubInputStream.readType(): JsTypeDefTypeListType? {
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


private fun StubInputStream.readBasicType() : JsTypeDefTypeListBasicType? {
    val type = readNameString() ?: return null
    return JsTypeDefTypeListBasicType(type)
}

private fun StubInputStream.readKeyOfType() : JsTypeDefTypeListKeyOfType? {
    val key = readNameString()
    val mapName = readNameString()
    if (key == null || mapName == null)
        return null
    return JsTypeDefTypeListKeyOfType(key, mapName)
}

private fun StubInputStream.readValueOfKeyType() : JsTypeDefTypeListValueOfKeyType? {
    val key = readNameString()
    val mapName = readNameString()
    if (key == null || mapName == null)
        return null
    return JsTypeDefTypeListValueOfKeyType(key, mapName)
}


private fun StubInputStream.readInterfaceBodyType() : JsTypeDefTypeListInterfaceBody {
    val properties = readPropertiesList()
    val numFunctions = readInt()
    val functions = mutableListOf<JsTypeDefFunctionType>()
    for (i in 0 until numFunctions) {
        functions.add(readFunction())
    }
    return JsTypeDefTypeListInterfaceBody(properties, functions)
}

private fun StubInputStream.readMapType() : JsTypeDefTypeListMapType {
    val keys = readTypesList()
    val valueTypes = readTypesList()
    return JsTypeDefTypeListMapType(keys, valueTypes)
}

private fun StubInputStream.readAnonymousFunctionType() : JsTypeDefTypeListAnonymousFunctionType {
    val parameters = readPropertiesList()
    val returnType = readTypes()
    return JsTypeDefTypeListAnonymousFunctionType(parameters, returnType)
}

fun StubInputStream.readPropertiesList() : List<JsTypeDefNamedProperty> {
    val numProperties = readInt()
    val properties = mutableListOf<JsTypeDefNamedProperty>()
    for (i in 0 until numProperties) {
        val property = readProperty() ?: continue
        properties.add(property)
    }
    return properties
}


private fun StubInputStream.readProperty() : JsTypeDefNamedProperty? {
    val propertyName = readNameString()
    val types = readTypes()
    val readonly = readBoolean()
    val static = readBoolean()
    if (propertyName == null)
        return null
    return JsTypeDefNamedProperty(propertyName, types, readonly, static)
}

private fun StubInputStream.readFunction() : JsTypeDefFunctionType {
    val functionName = readNameString()
    val parameters = readPropertiesList()
    val returnType = readTypes()
    val static = readBoolean()
    return JsTypeDefFunctionType(functionName, parameters, returnType, static)
}

fun StubOutputStream.writeTypes(type:JsTypeDefTypesList) {
    writeTypeList(type.types)
    writeBoolean(type.nullable)
}

private fun StubOutputStream.writeTypeList(types:List<JsTypeDefTypeListType>) {
    writeInt(types.size)
    for(type in types){
        writeType(type)
    }
}


private fun StubOutputStream.writeType(type:JsTypeDefTypeListType) {
    when (type) {
        is JsTypeDefTypeListBasicType -> writeBasicType(type)
        is JsTypeDefTypeListArrayType -> writeArrayType(type)
        is JsTypeDefTypeListMapType -> writeMapType(type)
        is JsTypeDefTypeListKeyOfType -> writeKeyOfType(type)
        is JsTypeDefTypeListValueOfKeyType -> writeValueOfKeyType(type)
        is JsTypeDefTypeListInterfaceBody -> writeInterfaceBody(type)
        is JsTypeDefTypeListAnonymousFunctionType -> writeAnonymousFunctionType(type)
    }
}

private fun StubOutputStream.writeBasicType(basicType: JsTypeDefTypeListBasicType) {
    writeInt(TypeListType.BASIC.id)
    writeName(basicType.typeName)
}

private fun StubInputStream.readArrayType() : JsTypeDefTypeListArrayType {
    val types = readTypesList()
    val dimensions = readInt()
    return JsTypeDefTypeListArrayType(types, dimensions)
}

private fun StubOutputStream.writeArrayType(type: JsTypeDefTypeListArrayType) {
    writeInt(TypeListType.ARRAY.id)
    writeTypeList(type.types)
    writeInt(type.dimensions)
}

private fun StubOutputStream.writeKeyOfType(type:JsTypeDefTypeListKeyOfType) {
    writeInt(TypeListType.KEYOF.id)
    writeName(type.genericKey)
    writeName(type.mapName)
}

private fun StubOutputStream.writeValueOfKeyType(type:JsTypeDefTypeListValueOfKeyType) {
    writeInt(TypeListType.VALUEOF.id)
    writeName(type.genericKey)
    writeName(type.mapName)
}

private fun StubOutputStream.writeInterfaceBody(body:JsTypeDefTypeListInterfaceBody) {
    writeInt(TypeListType.INTERFACE_BODY.id)
    writePropertiesList(body.properties)
    writeInt(body.functions.size)
    for(function in body.functions)
        writeFunction(function)
}


private fun StubOutputStream.writeAnonymousFunctionType(function:JsTypeDefTypeListAnonymousFunctionType) {
    writePropertiesList(function.parameters)
    writeTypes(function.returnType ?: JsTypeDefTypesList())
}

private fun StubOutputStream.writeFunction(function:JsTypeDefFunctionType) {
    writeName(function.name)
    writePropertiesList(function.parameters)
    writeTypes(function.returnType ?: JsTypeDefTypesList())
    writeBoolean(function.static)
}

fun StubOutputStream.writePropertiesList(properties:List<JsTypeDefNamedProperty>) {
    writeInt(properties.size)
    for (property in properties) {
        writeProperty(property)
    }
}

private fun StubOutputStream.writeProperty(type:JsTypeDefNamedProperty) {
    writeName(type.name)
    writeTypes(type.types)
    writeBoolean(type.readonly)
    writeBoolean(type.static)
}

private fun StubOutputStream.writeMapType(mapType: JsTypeDefTypeListMapType) {
    writeInt(TypeListType.MAP.id)
    writeTypeList(mapType.keyTypes)
    writeTypeList(mapType.valueTypes)
}

fun List<JsTypeDefTypeListInterfaceBody>.collapse(): JsTypeDefTypeListInterfaceBody {
    val propertiesList = this.flatMap { it.properties }.toSet()
    val functionList = this.flatMap { it.functions }.toSet()
    return JsTypeDefTypeListInterfaceBody(propertiesList.toList(), functionList.toList())
}
