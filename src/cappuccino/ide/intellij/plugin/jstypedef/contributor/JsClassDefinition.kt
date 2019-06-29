package cappuccino.ide.intellij.plugin.jstypedef.contributor

import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.inference.INFERRED_EMPTY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.plus
import cappuccino.ide.intellij.plugin.inference.toClassListString
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.Project


data class JsClassDefinition(
        val className: String,
        val extends: Set<JsTypeListType>,
        val enclosingNameSpaceComponents: List<String> = listOf(),
        val properties: Set<JsTypeDefNamedProperty> = setOf(),
        val functions: Set<JsTypeListFunctionType> = setOf(),
        val staticProperties: Set<JsTypeDefNamedProperty> = setOf(),
        val staticFunctions: Set<JsTypeListFunctionType> = setOf(),
        val isObjJ: Boolean = false,
        val isStruct: Boolean = true,
        val static: Boolean = false
)

fun getClassDefinitions(project: Project, className: String) : List<JsClassDefinition> {
    val objjClass = objJClassAsJsClass(project, className)
    val jsClasses = JsTypeDefClassesByNamespaceIndex.instance[className, project].map { it.toJsClassDefinition() }
    return if (objjClass != null)
        (jsClasses + objjClass)
    else
        jsClasses
}

fun getClassDefinition(project: Project, className: String): JsClassDefinition? {
    return getClassDefinitions(project, className).collapse()
}

fun Iterable<JsClassDefinition>.collapse(): JsClassDefinition {
    val firstName = this.firstOrNull()?.className
    val className = if (firstName != null && this.all { it.className == firstName }) firstName else "???"
    return JsClassDefinition(
            className = className,
            extends = flatMap { it.extends }.toSet(),
            enclosingNameSpaceComponents = emptyList(),
            properties = flatMap { it.properties }.toSet(),
            staticProperties = flatMap { it.staticProperties }.toSet(),
            functions = flatMap { it.functions }.toSet(),
            staticFunctions = flatMap { it.staticFunctions }.toSet(),
            static = all { it.static },
            isStruct = any { it.isStruct },
            isObjJ = false
    )
}


fun List<String>.toInferenceResult(): InferenceResult {
    val types = this.map {
        JsTypeListBasicType(it)
    }.toSet()
    return InferenceResult(types = types, nullable = true)
}

fun JsTypeDefFunction.toJsTypeListType(): JsTypeListFunctionType {
    return JsTypeListFunctionType(
            name = functionNameString,
            comment = null, // @todo implement comment parsing
            parameters = propertiesList?.properties?.toNamedFunctionPropertiesList() ?: emptyList(),
            returnType = functionReturnType?.toTypeListType() ?: INFERRED_EMPTY_TYPE
    )
}

fun List<JsTypeDefProperty>.toNamedPropertiesList(): List<JsTypeDefNamedProperty> {
    return map {
        it.toJsNamedProperty()
    }
}

fun JsTypeDefProperty.toJsNamedProperty(): JsTypeDefNamedProperty {
    val typeList = typeList.toJsTypeDefTypeListTypes().toMutableSet()
    val interfaceBodyAsType = interfaceBodyProperty?.toJsTypeListType()
    if (interfaceBodyAsType != null)
        typeList.add(interfaceBodyAsType)
    if (this is JsTypeDefFunctionProperty) {
        val keyOfType = this.keyOfType?.toTypeListType()
        if (keyOfType != null)
            typeList.add(keyOfType)
        val valueOfType = this.valueOfKeyType?.toTypeListType()
        if (valueOfType != null)
            typeList.add(valueOfType)
    }
    return JsTypeDefNamedProperty(
            name = propertyNameString,
            comment = docComment?.commentText,
            static = this.staticKeyword != null,
            readonly = this.readonly != null,
            types = InferenceResult(types = typeList, nullable = isNullable),
            default = null
    )
}

fun List<JsTypeDefFunctionProperty>.toNamedFunctionPropertiesList(): List<JsTypeDefNamedProperty> {
    return map {
        it.toJsNamedProperty()
    }
}
fun JsTypeDefFunctionProperty.toJsNamedProperty(): JsTypeDefNamedProperty {
    val typeList = typeList.toJsTypeDefTypeListTypes().toMutableSet()
    val keyOfType = this.keyOfType?.toTypeListType()
    if (keyOfType != null)
        typeList.add(keyOfType)
    val valueOfType = this.valueOfKeyType?.toTypeListType()
    if (valueOfType != null)
        typeList.add(valueOfType)
    return JsTypeDefNamedProperty(
            name = propertyNameString,
            comment = docComment?.commentText,
            static = false,
            readonly = false,
            types = InferenceResult(types = typeList, nullable = isNullable),
            default = null
    )
}

fun JsTypeDefInterfaceBodyProperty.toJsTypeListType(): JsTypeListClass {
    return JsTypeListClass(
            allFunctions = this.functionList.map { it.toJsTypeListType() }.toSet(),
            allProperties = this.propertyList.toNamedPropertiesList().toSet()
    )
}

/**
 * Type map stub key/value holder
 */
data class JsTypeDefTypeMapEntry(val key: String, val types: InferenceResult)

data class JsTypeDefNamedProperty(
        override val name: String,
        override val types: InferenceResult,
        override val readonly: Boolean = false,
        override val static: Boolean = false,
        override val comment: String? = null,
        override val default: String? = null
) : JsTypeDefPropertyBase, JsNamedProperty {
    override val nullable: Boolean get() = types.nullable
}

interface JsTypeDefPropertyBase {
    val types: InferenceResult
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
}

interface JsNamedProperty {
    val name: String?
    val static:Boolean
}


sealed class JsTypeListType(open val typeName: String) {
    data class JsTypeListArrayType(val types: Set<JsTypeListType>, val dimensions: Int = 1) : JsTypeListType("Array")
    data class JsTypeListKeyOfType(val genericKey: String, val mapName: String) : JsTypeListType("KeyOf:$mapName")
    data class JsTypeListValueOfKeyType(val genericKey: String, val mapName: String) : JsTypeListType("ValueOf:$mapName")
    data class JsTypeListMapType(val keyTypes: Set<JsTypeListType>, val valueTypes: Set<JsTypeListType>) : JsTypeListType("Map")
    data class JsTypeListBasicType(override val typeName: String) : JsTypeListType(typeName)
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    data class JsTypeListClass(
            val allProperties: Set<JsTypeDefNamedProperty>,
            val allFunctions: Set<JsTypeListFunctionType>
    ) : JsTypeListType("Object") {

        operator fun get(name: String): JsNamedProperty? {
            return getInstanceProperty(name) ?: getInstanceFunction(name)
        }

        val instanceProperties: Set<JsTypeDefNamedProperty> by lazy {
            allProperties.filterNot {
                it.static
            }.toSet()
        }

        val staticProperties: Set<JsTypeDefNamedProperty> by lazy {
            allProperties.filter {
                it.static
            }.toSet()
        }

        val instanceFunctions: Set<JsTypeListFunctionType> by lazy {
            allFunctions.filterNot { it.static }.toSet()
        }

        val staticFunctions: Set<JsTypeListFunctionType> by lazy {
            allFunctions.filter { it.static }.toSet()
        }


        fun getInstanceProperty(name: String): JsTypeDefNamedProperty? {
            return instanceProperties.firstOrNull {
                it.name == name
            }
        }

        fun getStaticProperty(name: String): JsTypeDefNamedProperty? {
            return staticProperties.firstOrNull {
                it.name == name
            }
        }

        fun getInstanceFunction(name: String): JsTypeListFunctionType? {
            return instanceFunctions.firstOrNull {
                it.name == name
            }
        }

        fun getStaticFunction(name: String): JsTypeListFunctionType? {
            return staticFunctions.firstOrNull {
                it.name == name
            }
        }

        fun collapseToKeys(): Set<String> {
            val out = mutableSetOf<String>()
            out.addAll(allProperties.map { it.name })
            out.addAll(allFunctions.mapNotNull { it.name })
            return out
        }
    }

    data class JsTypeListFunctionType(override val name: String? = null, val parameters: List<JsTypeDefNamedProperty>, val returnType: InferenceResult?, override val static: Boolean = false, val comment: String? = null) : JsTypeListType("Function"), JsNamedProperty {
        override fun toString(): String {
            val out = StringBuilder()
            if (name != null)
                out.append(name)
            out.append("(")
            val parametersString = parameters.joinToString(", ") { property ->
                property.name + property.types.types.joinToString("|") { type -> type.typeName }
            }
            out.append(parametersString)
                    .append(")")
            val returnTypes = this.returnType?.toClassListString()
            if (returnTypes.isNotNullOrBlank())
                out.append(" => ").append(returnTypes)
            return out.toString()
        }
    }
}

@Throws
operator fun JsTypeDefNamedProperty.plus(otherWithName: JsTypeDefNamedProperty?): JsTypeDefNamedProperty {
    if (otherWithName == null)
        return this

    if (this.name != otherWithName.name) {
        throw Exception("Cannot add properties with different property names.")
    }
    return JsTypeDefNamedProperty(
            name = this.name,
            types = this.types + otherWithName.types,
            default = this.default ?: otherWithName.default,
            readonly = this.readonly.orFalse() && this.readonly.orFalse(),
            static = this.static
    )
}

enum class TypeListType(val id: Int) {
    BASIC(0),
    ARRAY(1),
    KEYOF(2),
    VALUEOF(3),
    MAP(4),
    INTERFACE_BODY(5),
    FUNCTION(6);

    companion object {
        fun forKey(key: Int): TypeListType {
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