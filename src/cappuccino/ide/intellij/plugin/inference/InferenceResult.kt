package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

data class InferenceResult (
        val classes:Set<String> = emptySet(),
        val isNumeric:Boolean = isNumeric(classes),
        val isBoolean:Boolean = isBoolean(classes),
        val isString:Boolean = isString(classes),
        val isDictionary:Boolean = isDictionary(classes),
        val isSelector:Boolean = isSelector(classes),
        val isRegex:Boolean = isRegex(classes),
        val jsObjectKeys:PropertiesMap? = null,
        val functionTypes:List<JsFunctionType>? = null,
        val arrayTypes:Set<String>? = null
) {
    private var globalClasses:Set<GlobalJSClass>? = null

    val isJsObject:Boolean by lazy {
        jsObjectKeys?.isNotEmpty().orFalse() || "object" in classes || "?" in classes
    }

    fun jsClasses(project:Project):Iterable<GlobalJSClass> {
        var out = globalClasses
        if (out != null)
            return out
        if (classes.containsAnyType()) {
            out = globalJSClasses.toSet()
            globalClasses = out
            return out
        }
        return toClassList().mapNotNull { getJsClassObject(project, it) }
    }
}

private fun isNumeric(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in numberTypes}
}

private fun isBoolean(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in booleanTypes}
}

private fun isString(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "string"}
}

private fun isRegex(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "regex"}
}

private fun isDictionary(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in dictionaryTypes}
}

private fun isSelector(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "sel"}
}

data class JsFunctionType (
        val parameters:Map<String, InferenceResult> = mutableMapOf(),
        val returnType:InferenceResult = INFERRED_VOID_TYPE,
        val comment: String? = null
) {

    override fun toString(): String {
        val out = StringBuilder("(")
        val parametersString = parameters.joinToString(false)
        out.append(parametersString)
                .append(")")
        val returnTypes = this.returnType.toClassListString()
        if (returnTypes.isNotNullOrBlank())
            out.append(" => ").append(returnTypes)
        return out.toString()
    }
}

typealias PropertiesMap = Map<String, InferenceResult>

fun PropertiesMap.joinToString(enclose:Boolean = true) : String {
    val parameters = this.map {
        val parameterString = StringBuilder(it.key)
        val types = it.value.toClassList().joinToString(TYPES_DELIM)
        if (types.isNotNullOrBlank())
            parameterString.append(":").append(types)
        parameterString.toString()
    }.joinToString(", ")
    return if (enclose)
        "($parameters)"
    else
        parameters
}

operator fun InferenceResult.plus(other:InferenceResult):InferenceResult {
    val arrayTypes= combine(arrayTypes, other.arrayTypes)
    val jsObjectKeys = combine(jsObjectKeys, other.jsObjectKeys)
    val functionTypes = combine(functionTypes, other.functionTypes)
    return InferenceResult(
            isNumeric = isNumeric || other.isNumeric,
            isBoolean = isBoolean || other.isBoolean,
            isSelector = isSelector || other.isSelector,
            isString = isString || other.isString,
            isDictionary = isDictionary || other.isDictionary,
            isRegex = isRegex || other.isRegex,
            arrayTypes = arrayTypes,
            jsObjectKeys = jsObjectKeys ,
            functionTypes = functionTypes,
            classes = (classes + other.classes)
    )
}

internal fun List<InferenceResult>.collapse() : InferenceResult {
    val isNumeric = this.any { it.isNumeric}
    val isDictionary = this.any { it.isDictionary }
    val isBoolean = this.any { it.isBoolean }
    val isString = this.any { it.isString }
    val isSelector = this.any { it.isSelector }
    val isRegex = this.any { it.isRegex }
    val functionTypes = this.flatMap { it.functionTypes ?: emptyList()  }
    val classes = this.flatMap { it.classes }.toSet()
    var jsObjectKeys:Map<String, InferenceResult> = emptyMap()
    this.mapNotNull { it.jsObjectKeys }.forEach {
        jsObjectKeys = combine(jsObjectKeys, it) ?: jsObjectKeys
    }
    return InferenceResult(
            isNumeric = isNumeric,
            isBoolean = isBoolean,
            isString = isString,
            isDictionary = isDictionary,
            isSelector = isSelector,
            isRegex = isRegex,
            functionTypes = if (functionTypes.isNotEmpty()) functionTypes else null,
            classes = classes,
            jsObjectKeys = if (jsObjectKeys.isNotEmpty()) jsObjectKeys else null
    )
}

private fun <T> combine (thisList:List<T>?, otherList:List<T>?) : List<T>? {
    return if (thisList.isNullOrEmpty() && otherList.isNullOrEmpty())
        null
    else if (thisList.isNullOrEmpty())
        otherList
    else if (otherList.isNullOrEmpty())
        thisList
    else
        (otherList + thisList).toSet().toList()
}


private fun <T> combine (thisList:Set<T>?, otherList:Set<T>?) : Set<T>? {
    return if (thisList.isNullOrEmpty() && otherList.isNullOrEmpty())
        null
    else if (thisList.isNullOrEmpty())
        otherList
    else if (otherList.isNullOrEmpty())
        thisList
    else
        (otherList + thisList).toSet()
}

internal fun combine (thisList:Map<String, InferenceResult>?, otherList:Map<String, InferenceResult>?) : Map<String, InferenceResult>? {
    return if (thisList.isNullOrEmpty() && otherList.isNullOrEmpty())
        null
    else if (thisList.isNullOrEmpty())
        otherList
    else if (otherList.isNullOrEmpty())
        thisList
    else {
        val out = otherList.toMutableMap()
        for ((key, value) in otherList) {
            ProgressManager.checkCanceled()
            if (out.containsKey(key))
                out[key] = value + out[key]!!
            else
                out[key] = value
        }
        out
    }
}


internal fun InferenceResult.toClassList(simplifyAnyTypeTo:String? = "?") : Set<String> {
    if (this == INFERRED_ANY_TYPE) {
        return if (simplifyAnyTypeTo != null)
            setOf(simplifyAnyTypeTo)
        else
            emptySet()
    }
    val returnTypes = this
    val returnClasses = mutableListOf<String>()
    if (returnTypes.isNumeric && numberTypes.intersect(classes).isEmpty())
        returnClasses.add("number")
    if (returnTypes.isBoolean && booleanTypes.intersect(classes).isEmpty())
        returnClasses.add("BOOL")
    if (returnTypes.isRegex)
        returnClasses.add("regex")
    if (returnTypes.isDictionary)
        returnClasses.add("CPDictionary")
    if (returnTypes.isString && stringTypes.intersect(classes).isEmpty())
        returnClasses.add("string")
    if (returnTypes.isSelector)
        returnClasses.add("SEL")
    if (returnTypes.isJsObject)
        returnClasses.add("object")
    if (returnTypes.arrayTypes != null)
        returnClasses.add("Array")
    returnClasses.addAll(classes)
    return returnClasses.map {
        if (it in anyTypes)
            simplifyAnyTypeTo ?: it
        else
            it
        }.toSet()
}


fun InferenceResult.toClassListString(simplifyAnyTypeTo: String? = "?", delimiter:String = TYPES_DELIM) : String? {
    val functionTypes = this.functionTypes?.map {
        it.toString()
    }.orEmpty()
    val arrayTypes = this.arrayTypes?.map {
        "$it[]"
    }.orEmpty()
    val types = this.toClassList(simplifyAnyTypeTo) + functionTypes + arrayTypes
    val typesString = types.joinToString(delimiter)
    return if (typesString.isNotBlank())
        typesString
    else
        null
}


internal fun Iterable<String>.toInferenceResult(): InferenceResult {
    val classes = this.toSet()
    val arrayClasses = this.filter { it.endsWith("[]") }.map { it.substringFromEnd(0, 2) }
    return InferenceResult(
            isString = this.any { it.toLowerCase() in stringTypes },
            isBoolean = this.any { it.toLowerCase() in booleanTypes },
            isNumeric = this.any { it.toLowerCase() in numberTypes },
            isRegex = this.any { it.toLowerCase() == "regex"},
            isDictionary = this.any { it.toLowerCase() in dictionaryTypes},
            isSelector = this.any { it.toLowerCase() == "sel" },
            arrayTypes = arrayClasses.toSet(),
            classes = classes
    )
}

internal val booleanTypes = listOf("bool", "boolean")
internal val stringTypes = listOf("string", "cpstring")
internal val numberTypes = listOf("number", "int", "integer", "float", "long", "long long", "double")
internal val dictionaryTypes = listOf("map", "cpdictionary", "cfdictionary", "cpmutabledictionary", "cfmutabledictionary")
internal val anyTypes = listOf("id", "?", "any", ObjJClassType.UNDEF_CLASS_NAME.toLowerCase(), ObjJClassType.UNDETERMINED.toLowerCase())

internal fun Iterable<String>.containsAnyType() : Boolean {
    return this.any { it in anyTypes}
}

internal val InferenceResult.anyType : Boolean get() {
    return classes.any { it in anyTypes}
}

internal val INFERRED_ANY_TYPE = InferenceResult(
        isString = true,
        isBoolean = true,
        isRegex = true,
        isSelector = true,
        isDictionary = true,
        isNumeric = true,
        classes = setOf("object", "?"),
        arrayTypes = setOf("object", "?")
)

internal val INFERRED_VOID_TYPE = InferenceResult(
        classes = setOf("void")
)

fun StubOutputStream.writeJsFunctionType(function:JsFunctionType?) {
    writeBoolean(function != null)
    if (function == null)
        return
    writePropertiesMap(function.parameters)
    writeInferenceResult(function.returnType)
    writeUTFFast(function.comment ?: "")
}

fun StubInputStream.readJsFunctionType() : JsFunctionType? {
    if (!readBoolean())
        return null
    val parameters = readPropertiesMap()
    val returnType = readInferenceResult()
    val comment = readUTFFast()
    return JsFunctionType(
            parameters = parameters,
            returnType = returnType,
            comment = if (comment.isNotBlank()) comment else null
    )
}

fun StubOutputStream.writePropertiesMap(map:PropertiesMap) {
    writeInt(map.size)
    map.forEach { (key, value) ->
        writeName(key)
        writeInferenceResult(value)
    }
}

fun StubInputStream.readPropertiesMap() : PropertiesMap {
    val numProperties = readInt()
    val out = mutableMapOf<String, InferenceResult>()
    for (i in 0 until numProperties) {
        val name = readNameString() ?: "?"
        out[name] = readInferenceResult()
    }
    return out
}


fun StubOutputStream.writeInferenceResult(result:InferenceResult) {
    writeName(result.toClassList("?").joinToString(TYPES_DELIM))
    val functions = result.functionTypes ?: emptyList()
    writeInt(functions.size)
    functions.forEach {
        writeJsFunctionType(it)
    }
    writeBoolean(result.jsObjectKeys != null && result.jsObjectKeys.isNotEmpty())
    if (result.jsObjectKeys != null && result.jsObjectKeys.isNotEmpty())
        writePropertiesMap(result.jsObjectKeys)
    writeName(result.arrayTypes.orEmpty().joinToString(TYPES_DELIM))
}

fun StubInputStream.readInferenceResult() : InferenceResult {
    val classes = readNameString().orEmpty().split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toSet()
    val numFunctions = readInt()
    val functions = (0 until numFunctions).mapNotNull {
        readJsFunctionType()
    }
    val objectKeys = if (readBoolean())
        readPropertiesMap()
    else
        null
    val arrayTypes = readNameString().orEmpty().split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toSet()

    return InferenceResult(
            classes = classes,
            functionTypes = if (functions.isNotEmpty()) functions else null,
            jsObjectKeys = objectKeys,
            arrayTypes = if (arrayTypes.isNotEmpty()) arrayTypes else null
    )
}

