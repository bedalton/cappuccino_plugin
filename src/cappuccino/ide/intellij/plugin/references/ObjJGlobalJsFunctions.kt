package cappuccino.ide.intellij.plugin.references

val globalJsFunctionString = listOf(
        "alert","parseFloat","parseInt","isFinite","isNaN","setTimeout","clearTimeout",
        // Bootstrap.js
        "objj_importFile","objj_executeFile","objj_import",
        //CFBundle.js
        "CFBundle","CFBundleCopyLocalizedString","CFBundleCopyBundleLocalizations","CFCopyLocalizedString","CFCopyLocalizedStringFromTable","CFCopyLocalizedStringFromTableInBundle","CFCopyLocalizedStringWithDefaultValue","CFBundleGetMainBundle",
        // CFData.js
        "CFData","CFMutableData",
        // CFDictionary.js
        "CFDictionary", "CFMutableDictionary",
        // CFError.js
        "CFError","CFErrorCreate","CFErrorCreateWithUserInfoKeysAndValues","CFErrorGetCode","CFErrorGetDomain","CFErrorCopyDescription","CFErrorCopyUserInfo", "CFErrorCopyFailureReason", "CFErrorCopyRecoverySuggestion",
        // CFHTTPRequest.js
        "CFHTTPRequest",
        // CFPropertyList.js
        "objj_generateObjectUID",
)

private val ABS = func(
        functionName = "ABS",
        parameterNames = mapOf(
                "x" to p("number", "A numeric expression for which the absolute value is needed.")),
        fileName = "Constants.js",
        comment = "Returns the absolute value of a number (the value without regard to whether it is positive or negative).\nFor example, the absolute value of -5 is the same as the absolute value of 5.",
        returns = "number"
)

private val ACOS = func(
        functionName = "ACOS",
        parameterNames = mapOf(
                "x" to p("number", "A numeric ")),
        fileName = "Constants.js",
        comment = "Returns the arc cosine (or inverse cosine) of a number.",
        returns = "number"
)

private val ASIN = func(
        functionName = "ASIN",
        parameterNames = mapOf(
                "x" to p("int", "A numeric expression")),
        fileName = "Constants.js",
        comment = "Returns the arc sine of a number.",
        returns = "number"
)

private val ATAN = func(
        functionName = "ATAN",
        parameterNames = mapOf(
                "x" to p("int", "A numeric expression for which the arctangent is needed.")),
        fileName = "Constants.js",
        comment = "Returns the arc tangent of a number",
        returns = "number"
)

private val ATAN2 = func(
        functionName = "ATAN2",
        parameterNames = mapOf(
                "y" to p("int", "A numeric expression representing the cartesian y-coordinate."),
                "x" to p("int", "A numeric expression representing the cartesian x-coordinate.")),
        fileName = "Constants.js",
        comment = "Returns the angle (in radians) from the X axis to a point.",
        returns = "number"
)

private val SIN = func(
        functionName = "SIN",
        parameterNames = mapOf(
                "x" to p("int", "A numeric expression that contains an angle measured in radians.")),
        fileName = "Constants.js",
        comment = "Returns the sine of a number",
        returns = "number"
)

private val COS = func(
        functionName = "COS",
        parameterNames = mapOf(
                "x" to p("int", "A numeric expression that contains an angle measured in radians.")),
        fileName = "Constants.js",
        comment = "Returns the cosine of a number",
        returns = "number"
)

private val TAN = func(
        functionName = "TAN",
        parameterNames = mapOf(
                "x" to p("number", "A numeric expression that contains an angle measured in radians.")),
        fileName = "Constants.js",
        comment = "Returns the tangent of a number",
        returns = "number"
)

private val EXP = func(
        functionName = "EXP",
        parameterNames = mapOf(
                "x" to p("number", "A numeric expression representing the power of e.")),
        fileName = "Constants.js",
        comment = "Returns e (the base of natural logarithms) raised to a power.",
        returns = "number"
)

private val POW = func(
        functionName = "POW",
        parameterNames = mapOf(
                "x" to p("number", "The base value of the expression."),
                "y" to p("number", "The exponent value of the expression.")),
        fileName = "Constants.js",
        comment = "Returns the value of a base expression taken to a specified power.",
        returns = "number"
)




private val CEIL = func(
        functionName = "CEIL",
        parameterNames = mapOf(
                "x" to p("number")),
        fileName = "Constants.js",
        comment = "",
        returns = "number"
)

private val FLOOR = func(
        functionName = "FLOOR",
        parameterNames = mapOf(
                "x" to p("number")),
        fileName = "Constants.js",
        comment = "",
        returns = "number"
)


private val ROUND = func(
        functionName = "ROUND",
        parameterNames = mapOf(
                "x" to p("number")),
        fileName = "Constants.js",
        comment = "",
        returns = "number"
)

private val MIN = func(
        functionName = "MIN",
        parameterNames = mapOf(
                "...values" to p("number")),
        fileName = "Constants.js",
        comment = "Returns the smaller of a set of supplied numeric expressions.",
        returns = "number"
)

private val MAX = func(
        functionName = "MAX",
        parameterNames = mapOf(
                "...values" to p("number")),
        fileName = "Constants.js",
        comment = "Returns the larger of a set of supplied numeric expressions.",
        returns = "number"
)

private val RAND = func(
        functionName = "RAND",
        parameterNames = mapOf(
                "x" to p("number")),
        fileName = "Constants.js",
        comment = "Returns a pseudorandom number between 0 and 1.",
        returns = "number"
)

private val SQRT = func(
        functionName = "SQRT",
        parameterNames = mapOf(
                "x" to p("number")),
        fileName = "Constants.js",
        comment = "Returns the square root of a number.",
        returns = "number"
)

private val alert = func(
        functionName = "alert",
        parameterNames = mapOf("message" to p("Any"))
)

private val eval = func(
        functionName = "eval",
        parameterNames = mapOf("x" to p("string", "A String value that contains valid JavaScript code.")),
        returns = "any",
        comment = "Evaluates JavaScript code and executes it."
)

private val parseInt  = func(
        functionName = "parseInt",
        parameterNames = mapOf(
                "string" to p("string", "A string to convert into a number."),
                "radix" to p("number", "A value between 2 and 36 that specifies the base of the number in numString.")),
        comment = "Converts A string to an integer.",
        returns = "number"
)

private val parseFloat  = func(
        functionName = "parseFloat",
        parameterNames = mapOf("string" to p("string")),
        returns = "number"
)

private val isNaN  = func(
        functionName = "isNaN",
        parameterNames = mapOf("number" to p("number")),
        returns = "number"
)

private val isFinite  = func(
        functionName = "isFinite",
        parameterNames = mapOf("string" to p("string")),
        returns = "number"
)

private val decodeURI = func (
        functionName = "decodeURI",
        parameterNames = mapOf("encodedURI" to p("string"))
)

private val decodeURIComponent = func (
        functionName = "decodeURIComponent",
        parameterNames = mapOf("encodedURIComponent" to p("string"))
)

private val encodeURI = func (
        functionName = "endcodeURI",
        parameterNames = mapOf("decodedURI" to p("string"))
)

private val encodeURIComponent = func (
        functionName = "encodeURIComponent",
        parameterNames = mapOf("decodedURIComponent" to p("string"))
)

private val escape = func (
        functionName = "escape",
        parameterNames = mapOf("string" to p("string"))
)

private val unescape = func (
        functionName = "unescape",
        parameterNames = mapOf("string" to p("string"))
)


val globalJsFunctions = listOf(
        // Constants.j
        ABS, ACOS, ASIN, ATAN, ATAN2, SIN, COS,TAN, EXP, POW, CEIL, FLOOR, ROUND, RAND, MIN, MAX, SQRT,
        // Window
        alert, parseFloat, isNaN
        "alert","parseFloat","parseInt","isFinite","isNaN","setTimeout","clearTimeout",
)

data class GlobalJsFunction(val functionName:String, val parameterNames:Map<String,GlobalJsFunctionParameterInfo>, val fileName:String? = null, val comment:String? = null, val returns:GlobalJsFunctionParameterInfo? = null) {
    val numParameters:Int = parameterNames.size
}

private fun func(functionName:String, parameterNames:Map<String,GlobalJsFunctionParameterInfo>, fileName:String? = null, comment:String? = null) : GlobalJsFunction {
    return GlobalJsFunction(
            functionName = functionName,
            parameterNames = parameterNames,
            fileName = fileName,
            comment = comment)
}
private fun func(functionName:String, parameterNames:Map<String,GlobalJsFunctionParameterInfo>, fileName:String? = null, comment:String? = null, returns:String) : GlobalJsFunction {
    return GlobalJsFunction(
            functionName = functionName,
            parameterNames = parameterNames,
            fileName = fileName,
            comment = comment,
            returns = p(returns))
}

private fun func(functionName:String, parameterNames:Map<String,GlobalJsFunctionParameterInfo>, fileName:String, comment:String?, returns:GlobalJsFunctionParameterInfo) : GlobalJsFunction {
    return GlobalJsFunction(functionName, parameterNames, fileName, comment, returns)
}

private fun p(type:String, comment:String? = null) : GlobalJsFunctionParameterInfo {
    return GlobalJsFunctionParameterInfo(type, comment)
}

data class GlobalJsFunctionParameterInfo(val type:String, val comment:String? = null)

fun List<GlobalJsFunction>.functionNames() {
    return this.flatMap { it.functionName }
}