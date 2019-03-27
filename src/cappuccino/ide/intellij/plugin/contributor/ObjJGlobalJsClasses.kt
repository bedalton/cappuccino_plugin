package cappuccino.ide.intellij.plugin.contributor

data class GlobalJSClass(val className:String, val constructor:GlobalJSConstructor = GlobalJSConstructor(), val functions:List<GlobalJSClassFunction> = listOf(), val staticFunctions: List<GlobalJSClassFunction> = listOf(), val properties:List<JsProperty> = listOf(), val extends:String? = null, val comment:String? = null)

data class JsProperty(val name:String, val type:String = "?", val isPublic:Boolean = true, val nullable:Boolean = true, val comment:String? = null)

interface JsFunction {
    val name:String
    val parameters:List<JsProperty>
    val returns:String?
    val comment:String?
}

data class GlobalJSClassFunction (override val name:String, override val parameters:List<JsProperty> = listOf(), override val returns:String? = null, val isPublic:Boolean = true, override val comment:String? = null) : JsFunction
data class GlobalJSConstructor   (val parameters:List<JsProperty> = listOf())

typealias c = GlobalJSClass
typealias ctor = GlobalJSConstructor
typealias f = GlobalJSClassFunction
typealias p = JsProperty

val globalJSClasses = listOf(
        c(
                className = "CFBundle",
                constructor = ctor(listOf(p("aURL", "CPUrl|String"))),
                staticFunctions = listOf(
                        f("environments"),
                        f("bundleContainingURL", listOf (
                                p("aURL", "CPURL")
                        )),
                        f("bundleContainingURL", listOf (
                                p("aURL", "CPURL")
                        )),
                        f (name = "mainBundle", returns = "CFBundle"),
                        f ("bundleForClass", listOf(p("aClass", "Class"))),
                        f ("bundleWithIdentifier", listOf(p("bundleID", "string")))
                ),
                functions = listOf(
                        f(name = "bundleURL", returns = "CFURL"),
                        f (name = "resourcesDirectoryURL", returns = "CFURL"),
                        f (name = "resourceURL", parameters = listOf(
                                p("aResourceName", "string"),
                                p ("aType", "string"),
                                p ("aSubDirectory", "string"),
                                p ("localizationName", "string")
                                ), returns = "CFURL"
                        ),
                        f (name = "mostEligibleEnvironmentURL", returns = "CFURL"),
                        f (name = "executableURL", returns = "CFURL"),
                        f (name = "infoDictionary", returns = "?"),
                        f (name = "loadedLanguage", returns = "?"),
                        f (
                                name = "valueForInfoDictionaryKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = "?"
                        ),
                        f (name = "identifier", returns = "?"),
                        f (name = "hasSpritedImages", returns = "BOOL"),
                        f (name = "environments", returns = "?"),
                        f (
                                name = "mostEligibleEnvironment",
                                parameters = listOf(p("evironments", "array")),
                                returns = "?"
                        ),
                        f (name = "isLoading", returns = "BOOL"),
                        f (name = "isLoaded", returns = "BOOL"),
                        f (
                                name = "load",
                                parameters = listOf(p("shouldExecute", "BOOL"))
                        ),
                        f (
                                name = "addEventListener",
                                parameters = listOf(p("anEventName", "string"), p("anEventListener", "Function"))
                        ),
                        f (
                                name = "removeEventListener",
                                parameters = listOf(p("anEventName", "string"), p("anEventListener", "Function"))
                        ),
                        f (
                                name = "onerror",
                                parameters = listOf(p("anEvent", "Event"))
                        ),
                        f (name = "bundlePath", returns = "?"),
                        f (
                                name = "pathForResource",
                                parameters = listOf(
                                    p("aResourceName", "string"),
                                    p ("aType", "string"),
                                    p ("aSubDirectory", "string"),
                                    p ("localizationName", "string")
                                ),
                                returns = "CFURL"
                        )
                )
        ),
        c (
                className = "CFData",
                functions = listOf(
                        f (name = "propertyList", returns = "CFPropertyList"),
                        f (name = "JSONObject", returns = "object"),
                        f (name = "rawString", returns = "string"),
                        f (name = "bytes", returns = "byte[]"),
                        f (name = "base64", returns = "string")
                ),
                staticFunctions = listOf(
                        f (
                                name = "decodeBase64ToArray",
                                parameters = listOf(
                                        p ("input", "string"),
                                        p ("strip", "BOOL")
                                ),
                                returns = "byte[]"
                            ),
                        f (
                                name = "decodeBase64ToString",
                                parameters = listOf(
                                        p ("input", "string"),
                                        p ("strip", "BOOL")
                                ),
                                returns = "string"
                        ),
                        f (
                            name = "decodeBase64ToUtf16String"   ,
                                parameters = listOf(
                                        p ("input", "string"),
                                        p ("strip", "BOOL")
                                ),
                                returns = "string"
                        ),
                        f (
                                name = "encodeBase64Array",
                                parameters = listOf(p("input", "string[]")), // @todo check if this is correct
                                returns = "string"
                        )
                )
        ),
        c (
                className = "CFMutableData",
                extends = "CFData",
                functions = listOf(
                        f (
                                name = "setPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p ("aFormat", "Format"))
                        ),
                        f (
                                name = "setJSONObject",
                                parameters = listOf(
                                        p("anObject", "object"))
                        ),
                        f (
                                name = "setRawString",
                                parameters = listOf(
                                        p ("aString", "string"))
                        ),
                        f (
                                name = "setBytes",
                                parameters = listOf(
                                        p("bytes", "byte[]"))
                        ),
                        f (
                                name = "setBase64String",
                                parameters = listOf(
                                        p("aBase64String", "string"))
                        ),
                        f (
                                name = "bytesToString",
                                parameters = listOf(p("bytes", "byte[]")),
                                returns = "string"
                        ),
                        f (
                                name = "stringToBytes",
                                parameters = listOf(p("input", "string")),
                                returns = "byte[]"
                        ),
                        f (
                                name = "encodeBase64String",
                                parameters = listOf(p("input", "string")),
                                returns = "string"
                        ),
                        f (
                                name = "bytesToUtf16String",
                                parameters = listOf(p("bytes", "byte[]")),
                                returns = "string"
                        ),
                        f (
                                name = "encodeBase64Utf16String",
                                parameters = listOf(p("input", "string")),
                                returns = "string"
                        )
                )
        ),
        c (
                className = "CFDictionary",
                constructor = ctor(listOf(p("aDictionary", "CFDictionary"))),
                functions = listOf(
                        f (name = "copy", returns = "CFDictionary"),
                        f (name = "mutableCopy", returns = "CFMutableDictionary"),
                        f (
                                name = "containsKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = "BOOL"
                        ),
                        f (
                                name = "containsValue",
                                parameters = listOf(p("anObject", "id")),
                                returns = "BOOL"
                        ),
                        f (name = "count", parameters = listOf(), returns = "int"),
                        f (name = "countOfKey", parameters = listOf(p("aKey", "string")), returns = "int"),
                        f (name = "countOfValue", parameters = listOf(p("anObject", "id")), returns = "int"),
                        f (name = "keys", returns = "string[]"),
                        f (name = "valueForKey", parameters = listOf(p("aKey", "string")), returns = "id"),
                        f (name = "toString", returns = "string")
                )
        ),
        c (
                className = "CFMutableDictionary",
                extends = "CFDictionary",
                functions = listOf(
                        f (
                                name = "addValueForKey",
                                parameters = listOf(
                                        p ("aKey", "string"),
                                        p ("anObject", "object")
                                )
                        ),
                        f ("removeValueForKey", parameters = listOf(p("aKey", "string"))),
                        f ("removeAllValues"),
                        f (
                                name = "replaceValueForKey",
                                parameters = listOf(
                                    p ("aKey", "string"),
                                    p ("anObject", "object")
                                )
                        ),
                        f (
                                name = "setValueForKey",
                                parameters = listOf(
                                        p ("aKey", "string"),
                                        p ("anObject", "object")
                                )

                        )
                )

        ),
        c (
                className = "CFError",
                constructor = ctor (listOf(
                        p ("domain", "string"),
                        p ("code", "int"),
                        p ("userInfo", "CFDictionary")
                )),
                functions = listOf(
                        f (name = "domain", returns = "string"),
                        f (name = "code", returns = "int"),
                        f (name = "description", returns = "string"),
                        f (name = "failureReason", returns = "string"),
                        f (name = "recoverySuggestion", returns = "?"),
                        f (name = "userInfo", returns = "CFDictionary")
                )
        ),
        c (
                className = "",
                functions = listOf(
                        f (name = "status", returns = "int"),
                        f (name = "statusText", returns = "string"),
                        f (name = "readyState", returns = "int"),
                        f (name = "success", returns = "BOOL"),
                        f (name = "responseText", returns = "?"),
                        f (
                                name = "setRequestHeader",
                                parameters = listOf(
                                    p ("aHeader", "string"),
                                    p ("aValue", "object")
                                )
                        ),
                        f (name = "responseXML", returns = "XMLNode"),
                        f (name = "responsePropertyList", returns = "CFPropertyList"),
                        f (name = "setTimeout", parameters = listOf(p("aTimeout", "int"))),
                        f (name = "getTimeout", parameters = listOf(p("aTimeout", "int")), returns = "int"),
                        f (name = "getAllResponseHeaders", returns = "?"),
                        f (name = "overrideMimeType", parameters = listOf(p("aMimeType", "string"))),
                        f (
                                name = "open",
                                parameters = listOf(
                                        p ("aMethod", "string"),
                                        p ("aURL", "string"),
                                        p ("isAsynchronous", "BOOL"),
                                        p ("aUser", "string"),
                                        p ("aPassword", "string")

                                )
                        ),
                        f (name = "send", parameters = listOf(p("aBody", "object"))),
                        f (name = "abort", returns = "?"),
                        f (
                                name = "addEventListener",
                                parameters = listOf(
                                        p("anEventName", "string"),
                                        p ("anEventListener", "Function")
                                )
                        ),
                        f (
                                name = "removeEventListener",
                                parameters = listOf(
                                        p("anEventName", "string"),
                                        p ("anEventListener", "Function")
                                )
                        ),
                        f (name = "setWithCredentials", parameters = listOf(p("willSendWithCredentials", "BOOL"))),
                        f (name = "withCredentials", returns = "BOOL"),
                        f (name = "isTimeoutRequest", returns = "BOOL")
                    )
        ),
        c (
                className = "CFPropertyList",
                staticFunctions = listOf (
                        f (
                                name = "propertyListFromData",
                                parameters = listOf(
                                        p("aData", "Data"),
                                        p ("aFormat", "Format")
                                ),
                                returns = "CFPropertyList"
                        ),
                        f (
                                name = "propertyListFromString",
                                parameters = listOf(
                                        p("aString", "string"),
                                        p ("aFormat", "Format")
                                ),
                                returns = "CFPropertyList"

                        ),
                        f (
                                name = "propertyListFromXML",
                                parameters = listOf(
                                        p ("aStringOrXMLNode", "string|XMLNode")),
                                returns = "CFPropertyList"
                        ),
                        f (name = "sniffedFormatOfString", parameters = listOf(p("aStrign", "string")), returns = "int"),
                        f (
                                name = "dataFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p ("aFormat", "Format")
                                ),
                                returns = "CFMutableData"
                        ),
                        f (
                                name = "stringFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p ("aFormat", "Format")
                                ),
                                returns = "string"
                        ),
                        f (
                                name = "readPropertyListFromFile",
                                parameters = listOf(
                                        p("aFile", "string")
                                )
                        ),
                        f (
                                name = "writePropertyListToFile",
                                parameters = listOf(
                                        p ("aPropertyList", "CFPropertyList"),
                                        p ("aFile", "string"),
                                        p ("aFormat", "Format")
                                )
                        ),
                        f (
                                name = "modifyPlist",
                                parameters = listOf(
                                        p ("aFileName", "string"),
                                        p ("aCallback", "Function"),
                                        p ("aFormat", "Format")
                                )
                        )

                )

        ),
        c (
                className = "CFURL",
                constructor = ctor (listOf(
                        p ("aURL", "string|CFURL"),
                        p ("aBaseURL", "CFURL")
                )),
                functions = listOf(
                        f("UID", returns = "int"),
                        f ("mappedURL", returns = "CFURL"),
                        f (
                                name = "setMappedURLForURL",
                                parameters = listOf(
                                        p ("fromURL", "CFURL"),
                                        p ("toURL", "CFURL")
                                )
                        ),
                        f (name = "schemeAndAuthority", returns = "string"),
                        f (name = "absoluteString", returns = "string"),
                        f (name = "toString", returns = "string"),
                        f (name = "absoluteURL", returns = "CFURL"),
                        f (name = "standardizedURL", returns = "CFURL"),
                        f (name = "string", returns = "string"),
                        f (name = "authority", returns = "?"),
                        f (name = "hasDirectoryPath", returns = "BOOL"),
                        f (name = "hostName", returns = "?"),
                        f (name = "fragment", returns = "?"),
                        f (name = "lastPathComponent", returns = "?"),
                        f (name = "createCopyDeletingLastPathComponent", returns = "CFURL"),
                        f (name = "pathComponents", returns = "?"),
                        f (name = "pathExtension", returns = "string"),
                        f (name = "queryString", returns = "string"),
                        f (name = "scheme", returns = "string"),
                        f (name = "user", returns = "string"),
                        f (name = "password", returns = "string"),
                        f (name = "portNumber", returns = "int"),
                        f (name = "domain", returns = "string"),
                        f (name = "baseURL", returns = "?"),
                        f (name = "asDirectoryPathURL", returns = "CFURL"),
                        f (name = "resourcePropertyForKey", parameters = listOf(p("aKey", "string")), returns = "?"),
                        f (
                                name = "setResourcePropertyForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("aValue", "id")
                                )
                        ),
                        f (name = "staticResourceData", returns = "CFMutableData")
                )
        ),
        c (
                className = "objj_ivar",
                constructor = ctor(parameters = listOf(p ("aName", "string"), p ("aType", "string"))),
                properties = listOf(
                        p("name", "string"),
                        p("type", "string")
                )
        ),
        c (
                className = "objj_class",
                properties = listOf(
                        p ("isa"),
                        p ("version", "int"),
                        p ("super_class", "int"),
                        p ("name", "string"),
                        p ("info"),
                        p ("ivar_list", "objj_ivar[]"),
                        p ("ivar_store"),
                        p ("ivar_dtable"),
                        p ("method_list", "?[]"),
                        p ("method_store"),
                        p ("method_dtable"),
                        p ("protocol_list", "?[]"),
                        p ("allocator")
                )
        ),
        c (
                className = "objj_protocol",
                constructor = ctor (listOf(p("aName", "string"))),
                properties = listOf(
                        p ("name", "string"),
                        p ("instance_methods", "object"),
                        p ("class_methods", "object")
                )
        ),
        c (
                className = "objj_object",
                properties = listOf(
                        p("isa")
                )
        ),
        c (
                className = "objj_protocol",
                constructor = ctor (listOf(p("aName", "string"))),
                properties = listOf(
                        p ("name", "string"))
        )

)