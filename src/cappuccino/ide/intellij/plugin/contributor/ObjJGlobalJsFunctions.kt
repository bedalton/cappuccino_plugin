package cappuccino.ide.intellij.plugin.contributor


private val ABS = fn(
        name = "ABS",
        parameters = listOf(p(name = "x", type = "number", comment = "A numeric expression for which the absolute value is needed.")),
        comment = "Returns the absolute value of a number (the value without regard to whether it is positive or negative).\nFor example, the absolute value of -5 is the same as the absolute value of 5.",
        returns = "number"
)

private val ACOS = fn(
        name = "ACOS",
        parameters = listOf(
                p(name = "x", type = "number", comment = "A numeric")),
        comment = "Returns the arc cosine (or inverse cosine) of a number.",
        returns = "number"
)

private val ASIN = fn(
        name = "ASIN",
        parameters = listOf(
                p(name = "x", type = "number", comment = "A numeric expression")),
        comment = "Returns the arc sine of a number.",
        returns = "number"
)

private val ATAN = fn(
        name = "ATAN",
        parameters = listOf(
                p(name = "x", type = "number", comment = "A numeric expression for which the arctangent is needed.")),
        comment = "Returns the arc tangent of a number",
        returns = "number"
)

private val ATAN2 = fn(
        name = "ATAN2",
        parameters = listOf(
                p(name = "y", type = "number", comment = "A numeric expression representing the cartesian y-coordinate."),
                p(name = "x", type = "number", comment = "A numeric expression representing the cartesian x-coordinate.")
        ),
        comment = "Returns the angle (in radians) from the X axis to a point.",
        returns = "number"
)

private val SIN = fn(
        name = "SIN",
        parameters = listOf(p(name = "x", type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the sine of a number",
        returns = "number"
)

private val COS = fn(
        name = "COS",
        parameters = listOf(
                p(name = "x", type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the cosine of a number",
        returns = "number"
)

private val TAN = fn(
        name = "TAN",
        parameters = listOf(p(name = "x", type = "number", comment = "A numeric expression that contains an angle measured in radians.")),
        comment = "Returns the tangent of a number",
        returns = "number"
)

private val EXP = fn(
        name = "EXP",
        parameters = listOf(p(name = "x", type = "number", comment = "A numeric expression representing the power of e.")),
        comment = "Returns e (the base of natural logarithms) raised to a power.",
        returns = "number"
)

private val POW = fn(
        name = "POW",
        parameters = listOf(
                p(name = "x", type = "number", comment = "The base value of the expression."),
                p(name = "y", type = "number", comment = "The exponent value of the expression.")),
        comment = "Returns the value of a base expression taken to a specified power.",
        returns = "number"
)

private val CEIL = fn(
        name = "CEIL",
        parameters = listOf(p("x", "number")),
        comment = "",
        returns = "number"
)

private val FLOOR = fn(
        name = "FLOOR",
        parameters = listOf(p("x", "number")),
        comment = "",
        returns = "number"
)


private val ROUND = fn(
        name = "ROUND",
        parameters = listOf(p("x", "number")),
        comment = "",
        returns = "number"
)

private val MIN = fn(
        name = "MIN",
        parameters = listOf(p("...values", "number")),
        comment = "Returns the smaller of a set of supplied numeric expressions.",
        returns = "number"
)

private val MAX = fn(
        name = "MAX",
        parameters = listOf(p("...values", "number")),
        comment = "Returns the larger of a set of supplied numeric expressions.",
        returns = "number"
)

private val RAND = fn(
        name = "RAND",
        parameters = listOf(p("x", "number")),
        comment = "Returns a pseudorandom number between 0 and 1.",
        returns = "number"
)

private val SQRT = fn(
        name = "SQRT",
        parameters = listOf(p("x", "number")),
        comment = "Returns the square root of a number.",
        returns = "number"
)


private fun createGlobalJsFunctions(): List<JsFunction> {
        val globalJsFunctions = mutableListOf<JsFunction>(
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
                fn(name = "class_getName", parameters = listOf(p("aClass", "objj_class")), returns = "string", skipCompletion = true),
                fn(name = "class_isMetaClass", parameters = listOf(p("aClass")), returns = "BOOL", skipCompletion = true),
                fn(name = "class_getSuperclass", parameters = listOf(p("aClass")), returns = "objj_class", skipCompletion = true),
                fn(name = "class_setSuperclass", parameters = listOf(p("aClass", "objj_class"), p("aSuperClass", "objj_class")), skipCompletion = true),
                fn(
                        name = "class_addIvar",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aName", "string"),
                                p("aType", "string")
                        ),
                        skipCompletion = true
                ),
                fn(name = "class_addIvars", parameters = listOf(p("aClass", "objj_class"), p("ivars", "objj_ivar[]")), skipCompletion = true),
                fn(name = "class_copyIvarList", parameters = listOf(p("aClass", "objj_class")), returns = "objj_ivar[]", skipCompletion = true),
                fn(
                        name = "class_addMethod",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aName", "SEL"),
                                p("anImplementation", "IMP"),
                                p("types", "string[]")),
                        skipCompletion = true
                ),
                fn(
                        name = "class_addMethods",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("methods", "objj_method[]")),
                        skipCompletion = true
                ),
                fn(
                        name = "class_getInstanceMethod",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aSelector", "SEL")),
                        returns = "objj_method",
                        skipCompletion = true
                ),
                fn(
                        name = "class_getInstanceVariable",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aName", "string")),
                        returns = "objj_ivar",
                        skipCompletion = true
                ),
                fn(
                        name = "class_getClassMethod",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aSelector", "SEL")),
                        returns = "objj_method",
                        skipCompletion = true
                ),
                fn(
                        name = "class_respondsToSelector",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aSelector", "SEL")),
                        returns = "objj_method",
                        skipCompletion = true
                ),
                fn(
                        name = "class_copyMethodList",
                        parameters = listOf(
                                p("aClass", "objj_class")),
                        returns = "Method[]",
                        skipCompletion = true
                ),
                fn(
                        name = "class_getVersion",
                        parameters = listOf(
                                p("aClass", "objj_class")),
                        returns = "number",
                        skipCompletion = true
                ),
                fn(
                        name = "class_setVersion",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aVersion", "int")),
                        skipCompletion = true
                ),
                fn(
                        name = "class_replaceMethod",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aSelector", "SEL"),
                                p("aMethodImplementation", "objj_method")),
                        skipCompletion = true
                ),
                fn(
                        name = "class_addProtocol",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aProtocol", "objj_protocol")),
                        skipCompletion = true
                ),
                fn(
                        name = "class_conformsToProtocol",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aProtocol", "objj_protocol")
                        ),
                        returns = "BOOL",
                        skipCompletion = true
                ),
                fn(
                        name = "class_copyProtocolList",
                        parameters = listOf(
                                p("aClass", "objj_class")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "class_copyProtocolList",
                        parameters = listOf(
                                p("aClass", "objj_class")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_conformsToProtocol",
                        parameters = listOf(
                                p("p1", "objj_protocol"),
                                p("p2", "objj_protocol")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_allocateProtocol",
                        parameters = listOf(p("aName", "string")),
                        returns = "objj_protocol",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_registerProtocol",
                        parameters = listOf(
                                p("proto", "objj_protocol")),
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_getName",
                        parameters = listOf(
                                p("proto", "objj_protocol")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_addMethodDescription",
                        parameters = listOf(
                                p("proto", "objj_protocol"),
                                p("aSelector", "SEL"),
                                p("types", "string[]"),
                                p("isRequired", "BOOL"),
                                p("isInstanceMethod", "BOOL")),
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_addMethodDescriptions",
                        parameters = listOf(
                                p("proto", "objj_protocol"),
                                p("methods", "objj_method[]"),
                                p("isRequired", "BOOL"),
                                p("isInstanceMethod", "BOOL")),
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_copyMethodDescriptionList",
                        parameters = listOf(
                                p("proto", "objj_protocol"),
                                p("isRequired", "BOOL"),
                                p("isInstanceMethod", "BOOL")),
                        returns = "objj_method[]",
                        skipCompletion = true
                ),
                fn(
                        name = "protocol_addProtocol",
                        parameters = listOf(
                                p("proto", "objj_protocol"),
                                p("addition", "objj_protocol")),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_allocateTypeDef",
                        parameters = listOf(
                                p("aName", "string")
                        ),
                        returns = "objj_typeDef"
                ),
                fn(
                        name = "objj_registerTypeDef",
                        parameters = listOf(
                                p("typeDef", "objj_typeDef")
                        )
                ),
                fn(
                        name = "typeDef_getName",
                        parameters = listOf(
                                p("typeDef", "objj_typeDef")
                        )
                ),
                fn(
                        name = "_objj_forward",
                        parameters = listOf(
                                p("self", "?"),
                                p("_cmd", "?"))
                ),
                fn(
                        name = "class_getMethodImplementation",
                        parameters = listOf(
                                p("aClass", "objj_class"),
                                p("aSelector", "SEL")),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_enumerateClassesUsingBlock",
                        parameters = listOf(
                                p("aBlock", "Function(objj_class)")
                        )
                ),
                fn(
                        name = "objj_allocateClassPair",
                        parameters = listOf(
                                p("superClass", "objj_class"),
                                p("aName", "string")
                        ),
                        returns = "objj_class",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_registerClassPair",
                        parameters = listOf(
                                p("aClass", "objj_class")),
                        skipCompletion = true
                ),
                fn(name = "objj_resetRegisterClasses"),
                fn(
                        name = "class_createInstance",
                        parameters = listOf(
                                p("aClass", "objj_class")),
                        returns = "object",
                        skipCompletion = true),
                fn(
                        name = "object_getClassName",
                        parameters = listOf(
                                p("anObject", "object")),
                        returns = "string",
                        skipCompletion = true),
                fn(
                        name = "objj_lookUpClass",
                        parameters = listOf(
                                p("aName", "string")
                        ),
                        returns = "objj_class",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_getClass",
                        parameters = listOf(
                                p("aName", "string")
                        ),
                        returns = "objj_class",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_getClassList",
                        parameters = listOf(
                                p("buffer", "CPArray"),
                                p("bufferLength", "int")),
                        returns = "int",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_getMetaClass",
                        parameters = listOf(
                                p("aName", "string")),
                        returns = "?",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_getProtocol",
                        parameters = listOf(
                                p("aName", "string")),
                        returns = "objj_protocol",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_getTypeDef",
                        parameters = listOf(
                                p("aName", "string")),
                        returns = "objj_typeDef",
                        skipCompletion = true
                ),
                fn(
                        name = "ivar_getName",
                        parameters = listOf(
                                p("anIvar", "objj_ivar")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "ivar_getTypeEncoding",
                        parameters = listOf(
                                p("anIvar", "objj_ivar")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSend",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendFast",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendFast0",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendFast1",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendFast2",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendFast3",
                        parameters = listOf(
                                p("aReceiver", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendSuper",
                        parameters = listOf(
                                p("aSuper", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendSuper0",
                        parameters = listOf(
                                p("aSuper", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendSuper1",
                        parameters = listOf(
                                p("aSuper", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendSuper2",
                        parameters = listOf(
                                p("aSuper", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "objj_msgSendSuper3",
                        parameters = listOf(
                                p("aSuper", "id"),
                                p("aSelector", "SEL"),
                                p("...args", "?[]")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "method_getName",
                        parameters = listOf(
                                p("aMethod", "objj_method")
                        ),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "method_copyReturnType",
                        parameters = listOf(
                                p("aMethod", "objj_method")),
                        returns = "?",
                        comment = "This will not return correct values if the compiler does not have the option 'IncludeTypeSignatures'",
                        skipCompletion = true
                ),
                fn(
                        name = "method_copyArgumentType",
                        parameters = listOf(
                                p("aMethod", "objj_method"),
                                p("anIndex", "int")),
                        returns = "?",
                        comment = "This will not return correct values for index > 1 if the compiler does not have the option 'IncludeTypeSignatures'",
                        skipCompletion = true
                ),
                fn(
                        name = "method_getNumberOfArguments",
                        parameters = listOf(
                                p("aMethod", "objj_method")),
                        returns = "int",
                        comment = "Returns number of arguments for a method. The first argument is 'self' and the second is the selector.\n" +
                                "Those are followed by the method arguments. So for example it will return 2 for a method with no arguments.",
                        skipCompletion = true
                ),
                fn(
                        name = "method_getImplementation",
                        parameters = listOf(
                                p("aMethod", "objj_method")),
                        returns = "IMP",
                        skipCompletion = true
                ),
                fn(
                        name = "method_setImplementation",
                        parameters = listOf(
                                p("aMethod", "objj_method"),
                                p("anImplementation", "IMP")),
                        returns = "IMP",
                        comment = "returns old implementation",
                        skipCompletion = true
                ),
                fn(
                        name = "method_exchangeImplementations",
                        parameters = listOf(
                                p("lhs", "objj_method"),
                                p("rhs", "objj_method")
                        ),
                        skipCompletion = true
                ),
                fn(
                        name = "sel_getName",
                        parameters = listOf(p("aSelector", "SEL")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "sel_getUid",
                        parameters = listOf(p("aName", "string")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn(
                        name = "sel_isEqual",
                        parameters = listOf(
                                p("lhs", "SEL"),
                                p("rhs", "SEL")
                        ),
                        returns = "BOOL",
                        skipCompletion = true
                ),
                fn(
                        name = "sel_registerName",
                        parameters = listOf(p("aName", "string")),
                        returns = "string",
                        skipCompletion = true
                ),
                fn("atob", listOf(p("encodedString", "string")), returns = "string"),
                fn("btoa", listOf(p("rawString", "string")), returns = "string"),
                fn(
                        name = "fetch",
                        parameters = listOf(
                                p("input", "RequestInfo"),
                                p("init", "RequestInit", nullable = true)),
                        returns = "Promise<Response>"
                ),

                fn(
                        name = "clearInterval",
                        parameters = listOf(p("handler", "numer"))
                ),
                fn(
                        name = "clearTimeout",
                        parameters = listOf(p(name = "ref", type = "number"))
                ),
                fn(
                        name = "createImageBitmap",
                        parameters = listOf(p("image", "ImageBitmapSource")),
                        returns = "Promise<ImageBitmap>"
                ),
                fn(
                        name = "createImageBitmap(image: ImageBitmapSource, sx: number, sy: number, sw: number, sh: number): ",
                        parameters = listOf(
                                p("image", "ImageBitmapSource"),
                                p("sx", "number"),
                                p("sy", "number"),
                                p("sw", "number"),
                                p("sh", "number")),
                        returns = "Promise<ImageBitmap>"
                ),
                fn("queueMicrotask", listOf(p("callback", "Function"))),
                fn(
                        name = "setInterval",
                        parameters = listOf(
                                p("handler", "TimerHandler"),
                                p("timeout", "number"),
                                p("...arguments", "any[]")),
                        returns = "number"
                ),
                fn(
                        name = "setTimeout",
                        parameters = listOf(
                                p("handler", "TimerHandler"),
                                p("timeout", "number"),
                                p("...arguments", "any[]")),
                        returns = "number"
                ),
                fn (
                        name = "CPThemeState",
                        parameters = listOf(p("...values", "string|ThemeState")),
                        returns = "ThemeState"
                ),
                fn (
                        name = "require",
                        parameters = listOf(p("x", "string")),
                        returns = "?"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleLeftTop",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p("aTransform", "?"),
                                p("aLeft", "number"),
                                p("aTop", "number")
                        ),
                        fileName = "CPDOMDisplayServer"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleRightTop",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p("aTransform", "?"),
                                p("aRight", "number"),
                                p("aTop", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleLeftBottom",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p("aTransform", "?"),
                                p("aLeft", "number"),
                                p("aBottom", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleRightBottom",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p("aTransform", "?"),
                                p("aRight", "number"),
                                p("aBottom", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleSize",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p ("aWidth", "number"),
                                p("aHeight", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerSetSize",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p ("aWidth", "number"),
                                p("aHeight", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerSetStyleBackgroundSize",
                        parameters = listOf(
                                p("aDOMElement", "DOMElement"),
                                p ("aWidth", "number"),
                                p("aHeight", "number")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerAppendChild",
                        parameters = listOf(
                                p("aParentElement", "DOMElement"),
                                p ("aChildElement", "DOMElement")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerInsertBefore",
                        parameters = listOf(
                                p("aParentElement", "DOMElement"),
                                p("aChildElement", "DOMElement"),
                                p("aBeforeElement", "DOMElement")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "CPDOMDisplayServerRemoveChild",
                        parameters = listOf(
                                p("aParentElement", "DOMElement"),
                                p("aChildElement", "DOMElement")
                        ),
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "PREPARE_DOM_OPTIMIZATION",
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "EXECUTE_DOM_INSTRUCTIONS",
                        fileName = "CPDOMDisplayServer.h"
                ),
                fn (
                        name = "PLATFORM",
                        parameters = listOf(p("FEATURE")),
                        fileName = "Platform.h"
                ),
                fn (
                        name = "_IS_NUMERIC",
                        parameters = listOf(p("n", "number")),
                        fileName = "Foundation.h"
                )

        )
        globalJsFunctions.addAll(Window.functions)
        return globalJsFunctions
}

val globalJsFunctions = createGlobalJsFunctions()

val globalJsFunctionNames = globalJsFunctions.names()

data class GlobalJsFunction(override val name: String, override val parameters: List<JsProperty> = listOf(), override val returns: String? = null, override val comment: String? = null, val skipCompletion: Boolean = false, val fileName: String? = null) : JsFunction

typealias fn = GlobalJsFunction

fun List<JsFunction>.names(): List<String> {
    return this.map { it.name }
}
