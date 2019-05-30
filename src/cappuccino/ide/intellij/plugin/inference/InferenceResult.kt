package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.allObjJClassesAsJsClasses
import cappuccino.ide.intellij.plugin.contributor.GlobalJSClass
import cappuccino.ide.intellij.plugin.contributor.getJsClassObject
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.Project


data class InferenceResult (
        val classes:Set<String> = emptySet(),
        val isNumeric:Boolean = isNumeric(classes),
        val isBoolean:Boolean = isBoolean(classes),
        val isString:Boolean = isString(classes),
        val isDictionary:Boolean = isDictionary(classes),
        val isSelector:Boolean = isSelector(classes),
        val isRegex:Boolean = isRegex(classes),
        val jsObjectKeys:Map<String, InferenceResult>? = null,
        val functionTypes:List<JsFunctionType>? = null,
        val arrayTypes:Set<String>? = null
) {
    val _classes:Set<GlobalJSClass>? = null
    val isJsObject:Boolean by lazy {
        jsObjectKeys?.isNotEmpty().orFalse() || "object" in classes || "?" in classes
    }
    fun jsClasses(project:Project):Iterable<GlobalJSClass> {
        if (_classes != null)
            return _classes
        val objjClasses = allObjJClassesAsJsClasses(project)
        return classes.mapNotNull { getJsClassObject(project, objjClasses, it) }
    }
}

private fun isNumeric(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in numberTypes || it in anyTypes}
}

private fun isBoolean(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in booleanTypes || it in anyTypes}
}

private fun isString(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "string" || it in anyTypes}
}

private fun isRegex(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "regex" || it in anyTypes}
}

private fun isDictionary(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() in dictionaryTypes || it in anyTypes}
}

private fun isSelector(classes:Iterable<String>) : Boolean {
    return classes.any { it.toLowerCase() == "sel" || it in anyTypes}
}

data class JsFunctionType (
        val parameters:Map<String, InferenceResult>,
        val returnType:InferenceResult
)

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
            if (out.containsKey(key))
                out[key] = value + out[key]!!
            else
                out[key] = value
        }
        out
    }
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

internal fun InferenceResult.toClassList() : Set<String> {
    val returnTypes = this
    val returnClasses = mutableListOf<String>()
    val isAnyType = anyType
    if (isAnyType || returnTypes.isNumeric)
        returnClasses.add("number")
    if (isAnyType || returnTypes.isBoolean)
        returnClasses.add("boolean")
    if (isAnyType || returnTypes.isRegex)
        returnClasses.add("regex")
    if (isAnyType || returnTypes.isDictionary)
        returnClasses.add("CPDictionary")
    if (isAnyType || returnTypes.isString)
        returnClasses.add("String")
    if (isAnyType || returnTypes.isSelector)
        returnClasses.add("SEL")
    if (isAnyType || returnTypes.isJsObject)
        returnClasses.add("object")
    if (isAnyType || returnTypes.arrayTypes != null)
        returnClasses.add("Array")
    returnClasses.addAll(classes)
    return returnClasses.toSet()
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
internal val numberTypes = listOf("number", "int", "integer", "float", "long", "double")
internal val dictionaryTypes = listOf("map", "cpdictionary", "cfdictionary", "cpmutabledictionary", "cfmutabledictionary")
internal val anyTypes = listOf("id", "?", "any", "undef")

private fun Iterable<String>.containsAnyType() : Boolean {
    return this.any { it in anyTypes}
}

internal val InferenceResult.anyType : Boolean get() {
    return classes.any { it in anyTypes}
}