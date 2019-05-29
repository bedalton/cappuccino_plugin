package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.GlobalJSClass
import cappuccino.ide.intellij.plugin.contributor.JS_ANY
import cappuccino.ide.intellij.plugin.contributor.JS_OBJECT
import cappuccino.ide.intellij.plugin.utils.orFalse


data class InferenceResult (
        val classes:List<GlobalJSClass> = emptyList(),
        val isNumeric:Boolean = isNumeric(classes),
        val isBoolean:Boolean = isBoolean(classes),
        val isString:Boolean = isString(classes),
        val isDictionary:Boolean = isDictionary(classes),
        val isSelector:Boolean = isSelector(classes),
        val isRegex:Boolean = isRegex(classes),
        val jsObjectKeys:Map<String, InferenceResult>? = null,
        val functionTypes:List<JsFunctionType>? = null,
        val arrayTypes:List<String>? = null
) {
    val isJsObject:Boolean by lazy {
        jsObjectKeys?.isNotEmpty().orFalse() || JS_OBJECT in classes || JS_ANY in classes
    }
}

private fun isNumeric(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() in numberTypes}
}

private fun isBoolean(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() in booleanTypes}
}

private fun isString(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() == "string"}
}

private fun isRegex(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() == "regex"}
}

private fun isDictionary(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() in dictionaryTypes}
}

private fun isSelector(classes:List<GlobalJSClass>) : Boolean {
    return classes.containsAnyType() || classes.any { it.className.toLowerCase() == "sel"}
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
            classes = (classes + other.classes).toSet().toList()
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
        classes = listOf(JS_OBJECT, JS_ANY),
        arrayTypes = listOf("object", "?")
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
    returnClasses.addAll(classes.map{ it.className })
    return returnClasses.toSet()
}


internal val booleanTypes = listOf("bool", "boolean")
internal val stringTypes = listOf("string", "cpstring")
internal val numberTypes = listOf("number", "int", "integer", "float", "long", "double")
internal val dictionaryTypes = listOf("map", "cpdictionary", "cfdictionary", "cpmutabledictionary", "cfmutabledictionary")
internal val anyTypes = listOf("id", "?", "any", "undef")

private fun List<GlobalJSClass>.containsAnyType() : Boolean {
    return this.any { it.className in anyTypes}
}

internal val InferenceResult.anyType : Boolean get() {
    return classes.any { it.className in anyTypes}
}