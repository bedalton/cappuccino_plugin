package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypesList

val ObjJGlobalJSVariables:List<JsNamedProperty> = {
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
            p("kCFErrorLocalizedDescriptionKey", "string", ignore = true),
            p("kCFErrorLocalizedFailureReasonKey", "string", ignore = true),
            p("kCFErrorLocalizedRecoverySuggestionKey", "string", ignore = true),
            p("kCFErrorDescriptionKey", "string", ignore = true),
            p("kCFErrorUnderlyingErrorKey", "string", ignore = true),
            p("kCFErrorURLKey", "string", ignore = true),
            p("kCFErrorFilePathKey", "string", ignore = true),
            p("kCFErrorDomainCappuccino", "string", ignore = true),
            p("kCFErrorDomainCocoa", "string", ignore = true),
            p("kCFURLErrorUnknown", "number", ignore = true),
            p("kCFURLErrorCancelled", "number", ignore = true),
            p("kCFURLErrorBadURL", "number", ignore = true),
            p("kCFURLErrorTimedOut", "number", ignore = true),
            p("kCFURLErrorUnsupportedURL", "number", ignore = true),
            p("kCFURLErrorCannotFindHost", "number", ignore = true),
            p("kCFURLErrorCannotConnectToHost", "number", ignore = true),
            p("kCFURLErrorNetworkConnectionLost", "number", ignore = true),
            p("kCFURLErrorDNSLookupFailed", "number", ignore = true),
            p("kCFURLErrorHTTPTooManyRedirects", "number", ignore = true),
            p("kCFURLErrorResourceUnavailable", "number", ignore = true),
            p("kCFURLErrorNotConnectedToInternet", "number", ignore = true),
            p("kCFURLErrorRedirectToNonExistentLocation", "number", ignore = true),
            p("kCFURLErrorBadServerResponse", "number", ignore = true),
            p("kCFURLErrorUserCancelledAuthentication", "number", ignore = true),
            p("kCFURLErrorUserAuthenticationRequired", "number", ignore = true),
            p("kCFURLErrorZeroByteResource", "number", ignore = true),
            p("kCFURLErrorCannotDecodeRawData", "number", ignore = true),
            p("kCFURLErrorCannotDecodeContentData", "number", ignore = true),
            p("kCFURLErrorCannotParseResponse", "number", ignore = true),
            p("kCFURLErrorRequestBodyStreamExhausted", "number", ignore = true),
            p("kCFURLErrorFileDoesNotExist", "number", ignore = true),
            p("kCFURLErrorFileIsDirectory", "number", ignore = true),
            p("kCFURLErrorNoPermissionsToReadFile", "number", ignore = true),
            p("kCFURLErrorDataLengthExceedsMaximum", "number", ignore = true),
            p("kCFPropertyListOpenStepFormat", "number", ignore = true),
            p("kCFPropertyListXMLFormat_v1_0", "number", ignore = true),
            p("kCFPropertyListBinaryFormat_v1_0", "number", ignore = true),
            p("kCFPropertyList280NorthFormat_v1_0", "number", ignore = true),
            p ("DEBUG", "BOOL")
    )
    out
}()



val ObjJGlobalJSVariablesNames = ObjJGlobalJSVariables.filterNot{ it.ignore }.map { it.name }

val ObjJGlobalVariableNamesWithoutIgnores =
        ObjJGlobalJSVariables
                .filterNot {it.ignore}
                .map { it.name }