package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.inference.INFERRED_EMPTY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.JsFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceBodyProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import com.intellij.openapi.project.Project


data class JsClassDefinition (
        val className: String,
        val extends:List<JsTypeListType>,
        val enclosingNameSpaceComponents: List<String> = listOf(),
        val properties: Set<JsTypeDefNamedProperty> = setOf(),
        val functions: Set<JsFunction> = setOf(),
        val staticProperties: Set<JsTypeDefNamedProperty> = setOf(),
        val staticFunctions: Set<JsFunction> = setOf(),
        val isObjJ:Boolean = false,
        val isStruct:Boolean = true,
        val static:Boolean = false
)

fun getClassDefinition(project:Project, className: String) : JsClassDefinition? {
    val objjClass = objJClassAsJsClass(project, className)
    return objjClass
}


fun List<String>.toInferenceResult() : InferenceResult {
    val types = this.map {
        JsTypeListBasicType(it)
    }.toSet()
    return InferenceResult(types = types, nullable = true)
}

fun JsTypeDefFunction.toJsFunctionType() : JsFunction {
    return JsFunction(
            name = functionNameString,
            comment = null, // @todo implement comment parsing
            parameters = propertiesList?.propertyList?.toNamedPropertiesList() ?: emptyList(),
            returnType = functionReturnType?.toTypeListType() ?: INFERRED_EMPTY_TYPE
    )
}


fun JsTypeDefFunction.toJsTypeListType() : JsTypeListFunctionType {
    return JsTypeListFunctionType(
            name = functionNameString,
            parameters = propertiesList?.propertyList?.toNamedPropertiesList() ?: emptyList(),
            returnType = functionReturnType?.toTypeListType() ?: INFERRED_EMPTY_TYPE
    )
}

fun List<JsTypeDefProperty>.toNamedPropertiesList() : List<JsTypeDefNamedProperty> {
    return map {
        it.toJsNamedProperty()
    }
}

fun JsTypeDefProperty.toJsNamedProperty() : JsTypeDefNamedProperty {
    var typeList = typeList.toJsTypeDefTypeListTypes()
    val interfaceBodyAsType = interfaceBodyProperty?.toJsTypeListType()
    if (interfaceBodyAsType != null)
        typeList = typeList + interfaceBodyAsType
    return JsTypeDefNamedProperty(
            name = propertyNameString,
            comment = docComment?.commentText,
            static = this.staticKeyword != null,
            readonly = this.readonly != null,
            types = InferenceResult(types = typeList, nullable = isNullable),
            default = null
    )
}

fun JsTypeDefInterfaceBodyProperty.toJsTypeListType() : JsTypeListInterfaceBody {
    return JsTypeListInterfaceBody(
            functions = this.functionList.map { it.toJsFunctionType() }.toSet(),
            properties = this.propertyList.toNamedPropertiesList().toSet()
    )
}

/**
 * Type map stub key/value holder
 */
data class JsTypeDefTypeMapEntry (val key:String, val types:InferenceResult)

data class JsTypeDefNamedProperty(
        override val name:String,
        override val types: InferenceResult,
        override val readonly:Boolean = false,
        val static:Boolean = false,
        override val comment:String? = null,
        override val default: String? = null
) : JsTypeDefPropertyBase, JsNamedProperty {
    override val nullable:Boolean get() = types.nullable
}

interface JsTypeDefPropertyBase {
    val types:InferenceResult
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
}

interface JsNamedProperty {
    val name:String?
}

class JsFunction(
        override val name:String? = null,
        val static:Boolean = false,
        override val parameters:List<JsTypeDefNamedProperty>,
        override val returnType:InferenceResult = INFERRED_EMPTY_TYPE,
        override val comment: String? = null
) : JsFunctionType(), JsNamedProperty


fun JsFunction.toJsTypeListType() : JsTypeListFunctionType {
    return JsTypeListFunctionType(name = name, parameters = parameters, returnType = returnType)
}


sealed class JsTypeListType(open val typeName:String) {
    data class JsTypeListArrayType(val types:Set<JsTypeListType>, val dimensions:Int = 1) : JsTypeListType("Array")
    data class JsTypeListKeyOfType(val genericKey:String, val mapName:String) : JsTypeListType("KeyOf:$mapName")
    data class JsTypeListValueOfKeyType(val genericKey:String, val mapName:String) : JsTypeListType("ValueOf:$mapName")
    data class JsTypeListMapType(val keyTypes:Set<JsTypeListType>, val valueTypes:Set<JsTypeListType>) : JsTypeListType("Map")
    data class JsTypeListBasicType(override val typeName:String) : JsTypeListType(typeName)
    data class JsTypeListInterfaceBody(val properties:Set<JsTypeDefNamedProperty>, val functions:Set<JsFunction>) : JsTypeListType("Object")
    data class JsTypeListFunctionType(val name:String? = null, val parameters:List<JsTypeDefNamedProperty>, val returnType: InferenceResult?) : JsTypeListType("Function")
}

enum class TypeListType(val id:Int) {
    BASIC(0),
    ARRAY(1),
    KEYOF(2),
    VALUEOF(3),
    MAP(4),
    INTERFACE_BODY(5),
    ANONYMOUS_FUNCTION(6);

    companion object {
        fun forKey(key:Int):TypeListType {
            return when (key) {
                BASIC.id -> BASIC
                ARRAY.id -> ARRAY
                KEYOF.id -> KEYOF
                VALUEOF.id -> VALUEOF
                MAP.id -> MAP
                INTERFACE_BODY.id -> INTERFACE_BODY
                else -> throw Exception("Invalid class type stub value encountered")
            }
        }
    }
}