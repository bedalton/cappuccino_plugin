package cappuccino.ide.intellij.plugin.contributor


private val globalJsFunctionString = listOf (
        //CFBundle.js
        "CFBundleCopyLocalizedString", "CFBundleCopyBundleLocalizations", "CFCopyLocalizedString", "CFCopyLocalizedStringFromTable", "CFCopyLocalizedStringFromTableInBundle", "CFCopyLocalizedStringWithDefaultValue", "CFBundleGetMainBundle",
        // CFError.js
        "CFErrorCreate", "CFErrorCreateWithUserInfoKeysAndValues", "CFErrorGetCode", "CFErrorGetDomain", "CFErrorCopyDescription", "CFErrorCopyUserInfo", "CFErrorCopyFailureReason", "CFErrorCopyRecoverySuggestion",
        // CFHTTPRequest.js
        "CFHTTPRequest",
        // CFPropertyList.js
        "CFPropertyList",
        "CFPropertyListCreate"
)

private val ABS = fn (
        name = "ABS",
        parameters = listOf (p(name = "x", type = "number", comment = "A numeric expression for which the absolute value is needed.")),
        comment = "Returns the absolute value of a number (the value without regard to whether it is positive or negative).\nFor example, the absolute value of -5 is the same as the absolute value of 5.",
        returns = "number"
)

private val ACOS = fn (
        name = "ACOS",
        parameters = listOf (
                p(name = "x" , type = "number", comment = "A numeric")),
        comment = "Returns the arc cosine (or inverse cosine) of a number.",
        returns = "number"
)

private val ASIN = fn (
        name = "ASIN",
        parameters = listOf (
               p(name =  "x", type = "number", comment = "A numeric expression")),
        comment = "Returns the arc sine of a number.",
        returns = "number"
)

private val ATAN = fn (
        name = "ATAN",
        parameters = listOf (
                p(name = "x", type = "number", comment = "A numeric expression for which the arctangent is needed.")),
        comment = "Returns the arc tangent of a number",
        returns = "number"
)

private val ATAN2 = fn (
        name = "ATAN2",
        parameters = listOf (
                p (name = "y", type = "number", comment = "A numeric expression representing the cartesian y-coordinate."),
                p (name = "x", type = "number", comment = "A numeric expression representing the cartesian x-coordinate.")
        ),
        comment = "Returns the angle (in radians) from the X axis to a point.",
        returns = "number"
)

private val SIN = fn (
        name = "SIN",
        parameters = listOf (p(name = "x", type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the sine of a number",
        returns = "number"
)

private val COS = fn (
        name = "COS",
        parameters = listOf (
                p(name = "x" , type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the cosine of a number",
        returns = "number"
)

private val TAN = fn (
        name = "TAN",
        parameters = listOf (p (name = "x", type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the tangent of a number",
        returns = "number"
)

private val EXP = fn (
        name = "EXP",
        parameters = listOf (p(name = "x", type = "number", comment = "A numeric expression representing the power of e.")),
        comment = "Returns e (the base of natural logarithms) raised to a power.",
        returns = "number"
)

private val POW = fn (
        name = "POW",
        parameters = listOf (
                p(name = "x", type = "number", comment = "The base value of the expression."),
                p (name = "y", type = "number", comment = "The exponent value of the expression.")),
        comment = "Returns the value of a base expression taken to a specified power.",
        returns = "number"
)

private val CEIL = fn (
        name = "CEIL",
        parameters = listOf (p("x", "number")),
        comment = "",
        returns = "number"
)

private val FLOOR = fn (
        name = "FLOOR",
        parameters = listOf (p("x", "number")),
        comment = "",
        returns = "number"
)


private val ROUND = fn (
        name = "ROUND",
        parameters = listOf (p("x", "number")),
        comment = "",
        returns = "number"
)

private val MIN = fn (
        name = "MIN",
        parameters = listOf (p("...values", "number")),
        comment = "Returns the smaller of a set of supplied numeric expressions.",
        returns = "number"
)

private val MAX = fn (
        name = "MAX",
        parameters = listOf (p("...values", "number")),
        comment = "Returns the larger of a set of supplied numeric expressions.",
        returns = "number"
)

private val RAND = fn (
        name = "RAND",
        parameters = listOf (p("x", "number")),
        comment = "Returns a pseudorandom number between 0 and 1.",
        returns = "number"
)

private val SQRT = fn (
        name = "SQRT",
        parameters = listOf (p("x", "number")),
        comment = "Returns the square root of a number.",
        returns = "number"
)


private fun createGlobalJsFunctions(): List<GlobalJsFunction> {
        val globalJsFunctions = mutableListOf<GlobalJsFunction>(
                ABS, ACOS, ASIN, ATAN, ATAN2, SIN, COS, TAN, EXP, POW, CEIL, FLOOR, ROUND, RAND, MIN, MAX, SQRT,
                fn(
                        name = "CPLogRegister",
                        parameters = listOf(p(name = "aProvider", type = "?"), p(name = "aMaxLevel", type = "?"), p(name = "aFormatter", type = "?")),
                        comment = "Register a logger for all levels, or up to an optional max level"
                ),
                fn(
                        name = "CPLogRegisterRange",
                        parameters = listOf(p(name = "aProvider", type = "?"), p(name = "aMinLevel", type = "?"), p(name = "aMaxLevel", type = "?"), p(name = "aFormatter", type = "?")),
                        comment = "Register a logger for a range of levels"
                ),
                fn(
                        name = "CPLogRegisterSingle",
                        parameters = listOf(p(name = "aProvider", type = "?"), p(name = "aLevel", type = "?"), p(name = "aFormatter", type = "?")),
                        comment = "Register a logger for a single level"
                ),
                fn(
                        name = "CPLogUnregister",
                        parameters = listOf(p(name = "aProvider", type = "?"))
                ),
                fn(
                        name = "CPLog",
                        parameters = listOf(p(name = "...arguments", type = "..."))
                ),
                fn(
                        name = "CPLogConsole",
                        parameters = listOf(
                                p(name = "aString", type = "?"),
                                p(name = "aLevel", type = "?"),
                                p(name = "aTitle", type = "?"),
                                p(name = "aFormatter", type = "?")),
                        comment = "CPLogConsole uses the built in \"console\" object"
                ),
                fn(
                        name = "CPLogColorize",
                        parameters = listOf(p(name = "aString"), p(name = "aLevel")),
                        comment = "A stub to allow the same formatter to be used for both stream and browser output"
                ),
                fn(
                        name = "CPLogAlert",
                        parameters = listOf(
                                p("aString", "string"),
                                p("aLevel", "int"),
                                p("aTitle"),
                                p("aFormatter")
                        ),
                        comment = "CPLogAlert uses basic browser alert() functions"
                ),
                fn(
                        name = "CPLogPopup",
                        parameters = listOf(
                                p("aString"), p("aLevel"), p("aTitle"), p("aFormatter")),
                        comment = "CPLogPopup uses a slick popup window in the browser"
                ),
                fn(
                        name = "CPLogPrint",
                        parameters = listOf(p("aString"), p("aLevel"), p("aTitle"), p("aFormatter"))
                ),
                fn(
                        name = "CFURL",
                        parameters = listOf(
                                p(name = "aURL", type = "string|CFURL"),
                                p(name = "aBaseURL", type = "CFURL"))

                ),
                fn(
                        name = "CFPropertyListCreateFromXMLData",
                        parameters = listOf(p(name = "data", type = "?", comment = "data")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFPropertyListCreateXMLData",
                        parameters = listOf(p(name = "aPropertyList", type = "CFPropertyList")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFPropertyListCreateFrom280NorthData",
                        parameters = listOf(p(name = "data", type = "?", comment = "data")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFPropertyListCreate280NorthData",
                        parameters = listOf(p(name = "aPropertyList", type = "CFPropertyList")),
                        skipCompletion = true
                ),
                fn(
                        name = "CPPropertyListCreateFromData",
                        parameters = listOf(
                                p(name = "data", type = "CFData"),
                                p(name = "aFormat", type = "Format")),
                        skipCompletion = true
                ),
                fn(
                        name = "CPPropertyListCreateData",
                        parameters = listOf(
                                p(name = "aPropertyList", type = "CFPropertyList"),
                                p(name = "aFormat", type = "Format")),
                        skipCompletion = true
                ),
                fn(
                        name = "alert",
                        parameters = listOf(p(name = "message", type = "?"))
                ),
                fn(
                        name = "eval",
                        parameters = listOf(p(name = "x", type = "string", comment = "A String value that contains valid JavaScript code.")),
                        returns = "?",
                        comment = "Evaluates JavaScript code and executes it."
                ),
                fn(
                        name = "parseInt",
                        parameters = listOf(
                                p(name = "string", type = "string", comment = "A string to convert into a number."),
                                p(name = "radix", type = "number", comment = "A value between 2 and 36 that specifies the base of the number in numString.")),
                        comment = "Converts A string to an integer.",
                        returns = "number"
                ),
                fn(
                        name = "parseFloat",
                        parameters = listOf(p(name = "string", type = "string")),
                        returns = "number"
                ),
                fn(
                        name = "isNaN",
                        parameters = listOf(p(name = "number", type = "number")),
                        returns = "number"
                ),
                fn(
                        name = "isFinite",
                        parameters = listOf(p(name = "string", type = "string")),
                        returns = "number"
                ),
                fn(
                        name = "setTimeout",
                        parameters = listOf(
                                p(name = "callback", type = "string"),
                                p(name = "timeout", type = "number")),
                        returns = "number"
                ),
                fn(
                        name = "clearTimeout",
                        parameters = listOf(p(name = "ref", type = "number"))
                ),
                fn(
                        name = "decodeURI",
                        parameters = listOf(p(name = "encodedURI", type = "string"))
                ),
                fn(
                        name = "decodeURIComponent",
                        parameters = listOf(p(name = "encodedURIComponent", type = "string"))
                ),
                fn(
                        name = "endcodeURI",
                        parameters = listOf(p(name = "decodedURI", type = "string"))
                ),
                fn(
                        name = "encodeURIComponent",
                        parameters = listOf(p(name = "decodedURIComponent", type = "string"))
                ),
                fn(
                        name = "escape",
                        parameters = listOf(p(name = "string", type = "string"))
                ),
                fn(
                        name = "unescape",
                        parameters = listOf(p(name = "string", type = "string"))
                ),
                fn(
                        name = "objj_importFile",
                        parameters = listOf(p("aUrl")),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_executeFile",
                        parameters = listOf(p("aUrl")),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_import",
                        parameters = listOf(),
                        skipCompletion = true
                ),
                fn(
                        name = "CFBundleCopyLocalizedString",
                        parameters = listOf(p("bundle"), p("key"), p("value"), p("tableName")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFBundleCopyBundleLocalizations",
                        parameters = listOf(p(name = "aBundle", type = "Bundle")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFCopyLocalizedString",
                        parameters = listOf(p("key"), p("comment")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFCopyLocalizedStringFromTable",
                        parameters = listOf(p("key"), p("tableName"), p("comment")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFCopyLocalizedStringFromTableInBundle",
                        parameters = listOf(p("key"), p("tableName"), p("bundle"), p("comment")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFCopyLocalizedStringWithDefaultValue",
                        parameters = listOf(p("key"), p("tableName"), p("bundle"), p("value"), p("comment")),
                        skipCompletion = true
                ),
                fn(
                        name = "CFBundleGetMainBundle",
                        parameters = listOf(),
                        skipCompletion = true
                ),
                fn(
                        name = "CFErrorCreate",
                        parameters = listOf(
                                p(name = "domain", type = "CFString"),
                                p(name = "code", type = "int"),
                                p(name = "userInfo", type = "CFDictionary"))
                ),
                fn(
                        name = "CFErrorCreateWithUserInfoKeysAndValues",
                        parameters = listOf(p(name = "domain", type = "string"), p(name = "code", type = "int"), p(name = "userInfoKeys", type = "array"), p(name = "userInfoValues", type = "array"), p(name = "numUserInfoValues", type = "int"))
                ),
                fn(
                        name = "CFErrorGetCode",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFErrorGetDomain",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFErrorCopyDescription",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFErrorCopyUserInfo",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFErrorCopyFailureReason",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFErrorCopyRecoverySuggestion",
                        parameters = listOf(p(name = "err", type = "CFError"))
                ),
                fn(
                        name = "CFHTTPRequest"
                ),
                fn(
                        name = "objj_generateObjectUID",
                        returns = "int"
                ),
                fn(
                        name = "CFPropertyListCreate",
                        returns = "CFPropertyList"
                ),
                fn(
                        name = "CFPropertyListCreateFromXMLData",
                        parameters = listOf(p("data")),
                        returns = "CFPropertyList"
                ),
                fn(
                        name = "CFPropertyListCreateXMLData",
                        parameters = listOf(p(name = "aPropertyList", type = "XMLNode"))
                ),
                fn(name = "objj_msgSend_reset", comment = "reset to default objj_msgSend* implementations", skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_msgSend_decorate", comment = "decorate both objj_msgSend and objj_msgSendSuper", skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_msgSend_set_decorators", comment = "reset then decorate both objj_msgSend and objj_msgSendSuper", skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_backtrace_print", parameters = listOf(p("aStream", "Callable")), skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_backtrace_decorator", parameters = listOf(p("msgSend")), skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_supress_exceptions_decorator", parameters = listOf(p("msgSend")), skipCompletion = true, fileName = "Debug.js"),
                fn(name = "objj_typecheck_decorator", parameters = listOf(p("msgSend")), skipCompletion = true, fileName = "Debug.js"),
                fn(
                        name = "objj_debug_typecheck",
                        parameters = listOf(
                                p("expectedType"),
                                p("object")),
                        skipCompletion = true
                ),
                fn(name = "objj_eval", parameters = listOf(p("aString", "string")), returns = "?"),
                fn(
                        name = "objj_method",
                        parameters = listOf(
                                p("aName", "string"),
                                p("anImplementation", "IMP"),
                                p("types", "string[]")
                        ),
                        returns = "?"
                ),
                fn(name = "class_getName", parameters = listOf(p("aClass", "Class")), returns = "string", skipCompletion = true),
                fn(name = "class_isMetaClass", parameters = listOf(p("aClass")), returns = "BOOL", skipCompletion = true),
                fn(name = "class_getSuperclass", parameters = listOf(p("aClass")), returns = "Class", skipCompletion = true),
                fn(name = "class_setSuperclass", parameters = listOf(p("aClass", "Class"), p("aSuperClass", "Class")), skipCompletion = true),
                fn(
                        name = "class_addIvar",
                        parameters = listOf(
                                p("aClass", "Class"),
                                p("aName", "string"),
                                p("aType", "string")
                        ),
                        skipCompletion = true
                ),
                fn(name = "class_addIvars", parameters = listOf(p("aClass", "Class"), p("ivars", "objj_ivar[]")), skipCompletion = true),
                fn(name = "class_copyIvarList", parameters = listOf(p("aClass", "Class")), returns = "objj_ivar[]", skipCompletion = true),
                fn(
                        name = "class_addMethod",
                        parameters = listOf(
                                p ("aClass", "Class"),
                                p ("aName", "SEL"),
                                p ("anImplementation", "IMP"),
                                p ("types", "string[]")
                        )),
                fn (
                        name = "class_addMethods",

                )


        )
        globalJsFunctionString.forEach {
                globalJsFunctions.add(fn(
                        name = it,
                        parameters = listOf(),
                        skipCompletion = true
                ))
        }
        return globalJsFunctions
}

val globalJsFunctions = createGlobalJsFunctions()

val globalJsFunctionNames = globalJsFunctions.names()

data class GlobalJsFunction (override val name:String, override val parameters:List<JsProperty> = listOf(), override val returns:String? = null, override val comment:String? = null, val skipCompletion:Boolean = false, val fileName:String? = null) : JsFunction

typealias fn = GlobalJsFunction

fun List<JsFunction>.names(): List<String> {
    return this.map { it.name }
}
