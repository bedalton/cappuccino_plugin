package cappuccino.ide.intellij.plugin.contributor

val ObjJGlobalJSVariables:List<JsProperty> = {
    val out = mutableListOf(
            p("E", "number"),
            p("LN2", "number"),
            p("LN10", "number"),
            p("LOG", "number"),
            p("LOG2E", "number"),
            p("LOG10E", "number"),
            p("PI", "number"),
            p("PI2", "number"),
            p("PI_2", "number"),
            p("SQRT1_2", "number"),
            p("SQRT2", "number"),
            p("CPLogDisable", "BOOL"),
            p("kCFErrorLocalizedDescriptionKey", "string"),
            p("kCFErrorLocalizedFailureReasonKey", "string"),
            p("kCFErrorLocalizedRecoverySuggestionKey", "string"),
            p("kCFErrorDescriptionKey", "string"),
            p("kCFErrorUnderlyingErrorKey", "string"),
            p("kCFErrorURLKey", "string"),
            p("kCFErrorFilePathKey", "string"),
            p("kCFErrorDomainCappuccino", "string"),
            p("kCFErrorDomainCocoa", "string"),
            p("kCFURLErrorUnknown", "number"),
            p("kCFURLErrorCancelled", "number"),
            p("kCFURLErrorBadURL", "number"),
            p("kCFURLErrorTimedOut", "number"),
            p("kCFURLErrorUnsupportedURL", "number"),
            p("kCFURLErrorCannotFindHost", "number"),
            p("kCFURLErrorCannotConnectToHost", "number"),
            p("kCFURLErrorNetworkConnectionLost", "number"),
            p("kCFURLErrorDNSLookupFailed", "number"),
            p("kCFURLErrorHTTPTooManyRedirects", "number"),
            p("kCFURLErrorResourceUnavailable", "number"),
            p("kCFURLErrorNotConnectedToInternet", "number"),
            p("kCFURLErrorRedirectToNonExistentLocation", "number"),
            p("kCFURLErrorBadServerResponse", "number"),
            p("kCFURLErrorUserCancelledAuthentication", "number"),
            p("kCFURLErrorUserAuthenticationRequired", "number"),
            p("kCFURLErrorZeroByteResource", "number"),
            p("kCFURLErrorCannotDecodeRawData", "number"),
            p("kCFURLErrorCannotDecodeContentData", "number"),
            p("kCFURLErrorCannotParseResponse", "number"),
            p("kCFURLErrorRequestBodyStreamExhausted", "number"),
            p("kCFURLErrorFileDoesNotExist", "number"),
            p("kCFURLErrorFileIsDirectory", "number"),
            p("kCFURLErrorNoPermissionsToReadFile", "number"),
            p("kCFURLErrorDataLengthExceedsMaximum", "number"),
            p("kCFPropertyListOpenStepFormat", "number"),
            p("kCFPropertyListXMLFormat_v1_0", "number"),
            p("kCFPropertyListBinaryFormat_v1_0", "number"),
            p("kCFPropertyList280NorthFormat_v1_0", "number")
    )
    out.addAll(Window.properties)
    out
}()

val ObjJGlobalJSVariablesNames = ObjJGlobalJSVariables.map { it.name }

val ObjJGlobalVariableNamesWithoutIgnores =
        ObjJGlobalJSVariables
                .filterNot {it.ignore}
                .map { it.name }