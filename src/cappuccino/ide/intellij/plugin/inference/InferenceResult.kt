package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orElse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.Project

data class InferenceResult(
        val types: Set<JsTypeListType> = emptySet(),
        val nullable: Boolean = true,
        val isNumeric: Boolean = isNumeric(types),
        val isBoolean: Boolean = isBoolean(types),
        val isString: Boolean = isString(types),
        val isDictionary: Boolean = isDictionary(types),
        val isSelector: Boolean = isSelector(types),
        val isRegex: Boolean = isRegex(types)
) : Iterable<String> {

    val classes:Set<String> by lazy {
        types.map { it.typeName }.toSet()
    }

    val toIndexSearchString:String by lazy {
        "(" + classes.joinToString("|") { Regex.escapeReplacement(it) } + ")"
    }

    val properties: List<JsNamedProperty> by lazy {
        interfaceTypes.flatMap {
            it.allProperties + it.allFunctions
        }
    }

    fun toJsClasses(project:Project) : List<JsClassDefinition> {
        return classes.flatMap {
            getClassDefinitions(project, it)
        }
    }

    fun propertyForKey(name: String): JsNamedProperty? {
        return properties.firstOrNull {
            it.name == name
        }
    }

    val jsObjectKeys: Set<String> by lazy {
        val out = mutableSetOf<String>()
        interfaceTypes.forEach {
            for (property in it.allProperties) {
                out.add(property.name)
            }
            it.allFunctions.forEach { function ->
                if (function.name != null)
                    out.add(function.name)
            }
        }
        out
    }

    val isJsObject: Boolean by lazy {
        interfaceTypes.isNotNullOrEmpty() || "Object" in classes || "?" in classes
    }

    val mapTypes: List<JsTypeListType.JsTypeListMapType> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListMapType }
    }

    val arrayTypes: JsTypeListType.JsTypeListArrayType by lazy {
        val all = types.mapNotNull { it as? JsTypeListType.JsTypeListArrayType }
        val typesOut = all.flatMap {
            it.types
        }.toSet()
        val maxDimensions = all.map { it.dimensions }.max().orElse(1)
        JsTypeListType.JsTypeListArrayType(typesOut, maxDimensions)
    }

    val keyOfTypes: List<JsTypeListType.JsTypeListKeyOfType> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListKeyOfType }
    }

    val valueOfKeyTypes: List<JsTypeListType.JsTypeListValueOfKeyType> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListValueOfKeyType }
    }

    val basicTypes: List<JsTypeListType.JsTypeListBasicType> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListBasicType }
    }

    val interfaceTypes: List<JsTypeListType.JsTypeListClass> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListClass }
    }

    val functionTypes: List<JsTypeListType.JsTypeListFunctionType> by lazy {
        types.mapNotNull { it as? JsTypeListType.JsTypeListFunctionType }
    }

    override fun iterator(): Iterator<String> {
        return types.map { it.typeName }.iterator()
    }

    override fun toString(): String {
        return types.joinToString("|") { it.typeName }
    }

}

private fun isNumeric(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() in numberTypes }
}

private fun isBoolean(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() in booleanTypes }
}

private fun isString(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() in stringTypes }
}

private fun isRegex(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() == "regex" }
}

private fun isDictionary(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() in dictionaryTypes }
}

private fun isSelector(types:Iterable<JsTypeListType>): Boolean {
    return types.map { it.typeName }.any { it.toLowerCase() == "sel" }
}

open class JsFunctionType(
        open val parameters: List<JsTypeDefNamedProperty> = mutableListOf(),
        open val returnType: InferenceResult = INFERRED_VOID_TYPE,
        open val comment: String? = null
) {

    override fun toString(): String {
        val out = StringBuilder("(")
        val parametersString = parameters.joinToString(", ") { property ->
            property.name + property.types.types.joinToString("|") { type -> type.typeName }
        }
        out.append(parametersString)
                .append(")")
        val returnTypes = this.returnType.toClassListString()
        if (returnTypes.isNotNullOrBlank())
            out.append(" => ").append(returnTypes)
        return out.toString()
    }
}

typealias PropertiesMap = Map<String, InferenceResult>

fun PropertiesMap.joinToString(enclose: Boolean = true): String {
    val parameters = this.map {
        val parameterString = StringBuilder(it.key)
        val types = it.value.toClassList(null).joinToString(TYPES_DELIM)
        if (types.isNotNullOrBlank())
            parameterString.append(":").append(types)
        parameterString.toString()
    }.joinToString(", ")
    return if (enclose)
        "($parameters)"
    else
        parameters
}

operator fun InferenceResult.plus(other: InferenceResult): InferenceResult {
    return InferenceResult(
            types = (types + other.types),
            nullable = nullable || other.nullable
    )
}

fun Iterable<InferenceResult>.combine(): InferenceResult {
    return InferenceResult(
            types = this.flatMap { it.types }.toSet(),
            nullable = this.any { it.nullable }
    )
}

internal fun InferenceResult.toClassList(simplifyAnyTypeTo: String? = "?"): Set<String> {
    if (this == INFERRED_ANY_TYPE) {
        return if (simplifyAnyTypeTo != null)
            setOf(simplifyAnyTypeTo)
        else
            emptySet()
    }
    val returnClasses = mutableListOf<String>()
    if (isNumeric && numberTypes.intersect(classes).isEmpty())
        returnClasses.add("number")
    if (isBoolean && booleanTypes.intersect(classes).isEmpty())
        returnClasses.add("BOOL")
    if (isRegex)
        returnClasses.add("regex")
    if (isDictionary)
        returnClasses.add("CPDictionary")
    if (isString && stringTypes.intersect(classes).isEmpty())
        returnClasses.add("CPString")
    if (isSelector)
        returnClasses.add("SEL")
    if (isJsObject)
        returnClasses.add("Object")
    if (arrayTypes.types.isNotNullOrEmpty()) {
        var arrayTypes = arrayTypes.types.mapNotNull {
            if (it.typeName != "Array" && it.typeName !in anyTypes) {
                "${it.typeName}[]".replace("[][]", "[]")
            } else
                null
        }
        if (arrayTypes.isEmpty()) {
            arrayTypes = listOf("Array")
        }
        returnClasses.addAll(arrayTypes)
    }
    returnClasses.addAll(classes)
    return returnClasses.mapNotNull {
        when (it) {
            in anyTypes -> simplifyAnyTypeTo
            "string" -> "CPString"
            else -> it
        }
    }.toSet()
}


fun InferenceResult.toClassListString(simplifyAnyTypeTo: String? = "?", delimiter: String = TYPES_DELIM): String? {
    val functionTypes = this.functionTypes.map {
        it.toString()
    }
    val types = this.toClassList(simplifyAnyTypeTo) + functionTypes //+ arrayTypes.types.map { "${it.typeName}[]".replace("[][]", "[]")}
    val typesString = types.joinToString(delimiter)
    return if (typesString.isNotBlank())
        typesString
    else
        null
}


internal fun Iterable<String>.toInferenceResult(): InferenceResult {
    val classes = this.toJsTypeList()
    val arrayClasses = this.filter { it.endsWith("[]") }.map { it.substringFromEnd(0, 2) }.map {
        JsTypeListType.JsTypeListArrayType(listOf(it).toJsTypeList(), 1)
    }
    return InferenceResult(
            types = classes + arrayClasses,
            nullable = true
    )
}

fun Iterable<String>.toJsTypeList() : Set<JsTypeListType.JsTypeListBasicType> {
    return map { JsTypeListType.JsTypeListBasicType(it) }.toSet()
}

internal val booleanTypes = listOf("bool", "boolean")
internal val stringTypes = listOf("string", "cpstring")
internal val numberTypes = listOf("number", "int", "integer", "float", "long", "long long", "double")
internal val dictionaryTypes = listOf("map", "cpdictionary", "cfdictionary", "cpmutabledictionary", "cfmutabledictionary")
internal val anyTypes = listOf("id", "?", "any", ObjJClassType.UNDEF_CLASS_NAME.toLowerCase(), ObjJClassType.UNDETERMINED.toLowerCase())

internal fun Iterable<String>.withoutAnyType(): Set<String> {
    return this.filterNot { it in anyTypes }.toSet()
}


internal val InferenceResult.anyType: Boolean
    get() {
        return classes.any { it in anyTypes }
    }

internal val INFERRED_ANY_TYPE = InferenceResult(
        types = setOf(JsTypeListType.JsTypeListBasicType("?"))
)

internal val INFERRED_VOID_TYPE = InferenceResult(
        types = setOf(JsTypeListType.JsTypeListBasicType("void"))
)


internal val INFERRED_EMPTY_TYPE: InferenceResult by lazy {
    InferenceResult(
            types = setOf()
    )
}