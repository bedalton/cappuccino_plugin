package cappuccino.ide.intellij.plugin.contributor

import com.intellij.openapi.project.Project

data class GlobalJSClass(
        val className: String,
        val constructor: GlobalJSConstructor = GlobalJSConstructor(),
        val functions: List<GlobalJSClassFunction> = listOf(),
        val staticFunctions: List<GlobalJSClassFunction> = listOf(),
        val properties: List<JsProperty> = listOf(),
        val staticProperties: List<JsProperty> = listOf(),
        val extends: List<String> = listOf(),
        val comment: String? = null,
        val static: Boolean = false,
        val isStruct: Boolean = false,
        val isObjJ: Boolean = false) // Used to show that this is not a true object kind, but rather a descriptive object

data class JsProperty(
        val name: String,
        val type: String = "?",
        val isPublic: Boolean = true,
        val nullable: Boolean = true,
        val readonly: Boolean = false,
        val comment: String? = null,
        val default: String? = null,
        val ignore: Boolean = false,
        val callback: AnonymousJsFunction? = null,
        val deprecated: Boolean = false)

interface JsFunction {
    val name: String
    val parameters: List<JsProperty>
    val returns: String?
    val comment: String?
}

data class AnonymousJsFunction(
        val parameters: List<JsProperty> = emptyList(),
        val returns: String? = null,
        val comment: String? = null
)

data class GlobalJSClassFunction(override val name: String, override val parameters: List<JsProperty> = listOf(), override val returns: String? = null, val isPublic: Boolean = true, override val comment: String? = null) : JsFunction
data class GlobalJSConstructor(val parameters: List<JsProperty> = listOf())

typealias c = GlobalJSClass
typealias ctor = GlobalJSConstructor
typealias f = GlobalJSClassFunction
typealias p = JsProperty
typealias callback = AnonymousJsFunction

val Window: GlobalJSClass = c(
        className = "Window",
        properties = listOf(
                p("sessionStorage", "Storage", readonly = true),
                p("localStorage", "Storage", readonly = true),
                p("console", "Console", nullable = true, readonly = true),
                p("onabort", "Function", comment = "Fires when the user aborts the download.", nullable = true, ignore = true),
                p("onanimationcancel", type = "Function(this: Window, ev:Event)=>void", nullable = true, ignore = true),
                p("onanimationend", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onanimationiteration", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onanimationstart", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onauxclick", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onafterprint", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onblur", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the object loses the input focus.", ignore = true),
                p("oncancel", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("oncanplay", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Occurs when playback is possible, but would require further buffering.", ignore = true),
                p("oncanplaythrough", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onchange", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the contents of the object or selection have changed.", ignore = true),
                p("onclick", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user clicks the left mouse button on the object", ignore = true),
                p("onclose", "Function(this:Window, ev:Event)=>any", nullable = true, ignore = true),
                p("oncontextmenu", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user clicks the right mouse button in the client area, opening the context menu.", ignore = true),
                p("oncuechange", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("ondblclick", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user double-clicks the object.", ignore = true),
                p("ondrag", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the source object continuously during a drag operation.", ignore = true),
                p("ondragend", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the source object when the user releases the mouse at the close of a drag operation.", ignore = true),
                p("ondragenter", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondragexit", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondragleave", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the target object when the user moves the mouse out of a valid drop target during a drag operation.", ignore = true),
                p("ondragover", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the target element continuously while the user drags the object over a valid drop target.", ignore = true),
                p("ondragstart", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the source object when the user starts to drag a text selection or selected object.", ignore = true),
                p("ondrop", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondurationchange", "Function(this:Window, ev:Event)=>any", nullable = true, comment = "Occurs when the duration attribute is updated.", ignore = true),
                p("onemptied", "Function(this:Window, ev:Event)=>any", nullable = true, comment = "Occurs when the media element is reset to its initial state.", ignore = true),
                p("onended", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the end of playback is reached.", ignore = true),
                p("onerror", "OnErrorEventHandler", nullable = true, comment = "Fires when an error occurs during object loading.", ignore = true),
                p("onfocus", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Fires when the object receives focus.", ignore = true),
                p("ongotpointercapture", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("oninput", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("oninvalid", "Function(this: Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onkeydown", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user presses a key.", ignore = true),
                p("onkeypress", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user presses an alphanumeric key.", ignore = true),
                p("onkeyup", "Function(this: Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user releases a key", ignore = true),
                p("onload", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Fires immediately after the browser loads the object.", ignore = true),
                p("onloadeddata", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when media data is loaded at the current playback position.", ignore = true),
                p("onloadmetadata", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the duration and dimensions of the media have been determined.", ignore = true),
                p("onloadend", "Function(this:Window, ev:ProgressEvent)=>Any", nullable = true, ignore = true),
                p("onloadstart", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when Internet Explorer begins looking for media data.", ignore = true),
                p("onlostpointercapture", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onmousedown", "Function(this: Window, ev:Event))=>Any", nullable = true, comment = "Fires when the user clicks the object with either mouse button.", ignore = true),
                p("onmouseenter", "Function(this: Window, ev:Event))=>Any", nullable = true, ignore = true),
                p("onmouseenterleave", "Function(this: Window, ev:Event))=>Any", nullable = true, ignore = true),
                p("onmousemove", "Function(this: Window, ev:Event))=>Any", nullable = true, comment = "Fires when the user moves the mouse over the object.", ignore = true),
                p("onmouseout", "Function(this: Window, ev:Event))=>Any", nullable = true, comment = "Fires when the user moves the mouse pointer outside the boundaries of the object.", ignore = true),
                p("onmouseover", "Function(this: Window, ev:Event))=>Any", nullable = true, comment = "Fires when the user moves the mouse pointer into the object.", ignore = true),
                p("onmouseup", "Function(this: Window, ev:Event))=>Any", nullable = true, comment = "Fires when the user releases a mouse button while the mouse is over the object.", ignore = true),
                p("onmousedown", "Function(this: Window, ev:Event))=>Any", nullable = true, ignore = true),
                p("onpause", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when playback is paused.", ignore = true),
                p("onplay", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the play method is requested.", ignore = true),
                p("onplaying", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the audio or video has started playing.", ignore = true),
                p("onpointercancel", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerdown", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerenter", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerleave", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointermove", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerout", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerover", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onpointerup", "Function(this:Window, ev:PointerEvent)=>Any", nullable = true, ignore = true),
                p("onprogress", "Function(this:Window, ev:ProgressEvent)=>Any", nullable = true, comment = "Occurs to indicate progress while downloading media data.", ignore = true),
                p("onratechange", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the playback rate is increased or decreased.", ignore = true),
                p("onreset", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user resets a form.", ignore = true),
                p("onresize", "Function(this:Window, ev:UIEvent)=>Any", nullable = true, ignore = true),
                p("onscroll", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Fires when the user repositions the scroll box in the scroll bar on the object.", ignore = true),
                p("onsecuritypolicyviolation", "Function(this:Window, ev:SecurityPolicyViolationEvent)=>Any", nullable = true, ignore = true),
                p("onseeked", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the seek operation ends.", ignore = true),
                p("onseeking", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the current playback position is moved.", ignore = true),
                p("onselect", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Fires when the current selection changes.", ignore = true),
                p("onselectionchange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onselectstart", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onstalled", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the download has stopped.", ignore = true),
                p("onsubmit", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onsuspend", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs if the load operation has been intentionally halted.", ignore = true),
                p("ontimeupdate", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs to indicate the current playback position.", ignore = true),
                p("ontoggle", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("ontouchcancel", "Function(this:Window, ev:TouchEvent)=>Any", nullable = true, ignore = true),
                p("ontouchend", "Function(this:Window, ev:TouchEvent)=>Any", nullable = true, ignore = true),
                p("ontouchmove", "Function(this:Window, ev:TouchEvent)=>Any", nullable = true, ignore = true),
                p("ontouchstart", "Function(this:Window, ev:TouchEvent)=>Any", nullable = true, ignore = true),
                p("ontransitioncancel", "Function(this:Window, ev:TransitionEvent)=>Any", nullable = true, ignore = true),
                p("ontransitionend", "Function(this:Window, ev:TransitionEvent)=>Any", nullable = true, ignore = true),
                p("ontransitionrun", "Function(this:Window, ev:TransitionEvent)=>Any", nullable = true, ignore = true),
                p("ontransitionstart", "Function(this:Window, ev:TransitionEvent)=>Any", nullable = true, ignore = true),
                p("ontouchcancel", "Function(this:Window, ev:TouchEvent)=>Any", nullable = true, ignore = true),
                p("onvolumechange", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when the volume is changed, or playback is muted or unmuted.", ignore = true),
                p("onwaiting", "Function(this:Window, ev:Event)=>Any", nullable = true, comment = "Occurs when playback stops because the next frame of a video resource is not available.", ignore = true),
                p("onbeforeprint", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onbeforeunload", "Function(this:Window, ev:BeforeUnloadEvent)=>Any", nullable = true, ignore = true),
                p("onhashchange", "Function(this:Window, ev:HashChangeEvent)=>Any", nullable = true, ignore = true),
                p("onlanguagechange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmessage", "Function(this:Window, ev:MessageEvent)=>Any", nullable = true, ignore = true),
                p("onmessageerror", "Function(this:Window, ev:MessageEvent)=>Any", nullable = true, ignore = true),
                p("onoffline", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("ononline", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onpagehide", "Function(this:Window, ev:PageTransitionEvent)=>Any", nullable = true, ignore = true),
                p("onpageshow", "Function(this:Window, ev:PageTransitionEvent)=>Any", nullable = true, ignore = true),
                p("onpopstate", "Function(this:Window, ev:PopStateEvent)=>Any", nullable = true, ignore = true),
                p("onrejectionhandled", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onstorage", "Function(this:Window, ev:StorageEvent)=>Any", nullable = true, ignore = true),
                p("onunhandledrejection", "Function(this:Window, ev:PromiseRejectionEvent)=>Any", nullable = true, ignore = true),
                p("onunload", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onwheel", "Function(this:Window, ev:WheelEvent)=>Any", nullable = true, ignore = true),
                p("oncompassneedscalibration", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("ondevicelight", "Function(this:Window, ev:DeviceLightEvent)=>Any", nullable = true, ignore = true),
                p("ondevicemotion", "Function(this:Window, ev:DeviceMotionEvent)=>Any", nullable = true, ignore = true),
                p("ondeviceorientation", "Function(this:Window, ev:DeviceOrientationEvent)=>Any", nullable = true, ignore = true),
                p("onmousewheel", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgesturechange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgesturedoubletap", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgestureend", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgesturehold", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgesturestart", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsgesturetap", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmsinertiastart", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointercancel", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerdown", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerenter", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerleave", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointermove", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerout", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerover", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onmspointerup", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onorientationchange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onreadystatechange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplayactivate", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplayblur", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplayconnect", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplaydeactivate", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplaydisconnect", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplayfocus", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplaypointerrestricted", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplaypointerunrestricted", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("onvrdisplaypresentchange", "Function(this:Window, ev:Event)=>Any", nullable = true, ignore = true),
                p("indexedDB", "IDBFactory", nullable = true, readonly = true),
                p("caches", "CacheStorage", readonly = true),
                p("crypto", "Crypto", readonly = true),
                p("indexedDB", "IDBFactory", readonly = true),
                p("origin", "string"),
                p("performance", "Performance"),
                p("customElements", "CustomElementsRegistry"),
                p("defaultStatus", "string"),
                p("devicePixelRatio", "number"),
                p("doNotTrack", "string"),
                p("document", "Document"),
                p("event", "Event", nullable = true),
                p("external", "External"),
                p("frameElement", "DOMElement"),
                p("frames", "Window"),
                p("history", "History"),
                p("innerHeight", "number"),
                p("innerWidth", "number"),
                p("isSecureContext", "BOOL"),
                p("length", "number"),
                p("location", "Location"),
                p("locationbar", "BarProp"),
                p("menubar", "BarProp"),
                p("msContentScript", "ExtensionScriptApis"),
                p("name", "never"),
                p("navigator", "Navigator"),
                p("offscreenBuffering", "string|BOOL"),
                p("opener", "?"),
                p("orientation", "string|number", readonly = true),
                p("outerHeight", "number"),
                p("outerWidth", "number"),
                p("pageXOffset", "number"),
                p("pageYOffset", "number"),
                p("parent", "Window"),
                p("performance", "Performance"),
                p("personalbar", "BarProp"),
                p("screen", "Screen"),
                p("screenLeft", "number"),
                p("screenTop", "number"),
                p("screenX", "number"),
                p("screenY", "number"),
                p("scrollX", "number"),
                p("scrollY", "number"),
                p("scrollbars", "BarProp"),
                p("self", "Windows"),
                p("speechSynthesis", "SpeechSynthesis"),
                p("status", "string"),
                p("statusbar", "BarProp"),
                p("styleMedia", "StyleMedia"),
                p("toolbar", "BarProp"),
                p("top", "Window"),
                p("window", "Window")
        ),
        functions = listOf(
                f(name = "alert", parameters = listOf(p("message"))),
                f(name = "blur"),
                f(name = "cancelAnimationFrame", parameters = listOf(p("handler", "number"))),
                f(name = "captureEvents"),
                f("close"),
                f(name = "confirm", parameters = listOf(p(name = "message", type = "string", nullable = true)), returns = "BOOL"),
                //departFocus(navigationReason: NavigationReason, origin: FocusNavigationOrigin): void
                f(
                        name = "departFocus",
                        parameters = listOf(
                                p("navigationReason", "NavigationReason"),
                                p("origin", "FocusNavigationOrigin"))
                ),
                f("focus"),
                f(
                        name = "getComputedStyle",
                        parameters = listOf(
                                p("elt", "DOMElement"),
                                p(name = "psuedoElt", type = "string", nullable = true)),
                        returns = "CSSStyleDeclaration"
                ), f(
                name = "getMatchedCSSRules",
                parameters = listOf(
                        p("elt", "DOMElement"),
                        p(name = "psuedoElt", type = "string", nullable = true)),
                returns = "CSSRuleList"
        ),//
                f(
                        name = "getSelection",
                        returns = "Selection"
                ),
                f(
                        name = "matchMedia",
                        parameters = listOf(
                                p("query", "string")),
                        returns = "MediaQueryList"
                ),
                f(
                        name = "moveBy",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        name = "moveTo",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        name = "msWriteProfilerMark",
                        parameters = listOf(
                                p("profilerMarkName", "string"))
                ),
                f(
                        name = "open",
                        parameters = listOf(
                                p(name = "url", type = "string", nullable = true),
                                p("target", "string", nullable = true),
                                p("features", "string", nullable = true),
                                p("replace", "BOOL", nullable = true)),
                        returns = "Window|null"
                ),
                f(
                        name = "postMessage",
                        parameters = listOf(
                                p("message", "string"),
                                p("target", "string"),
                                p("transfer", "Transferable[]", nullable = true))
                ),
                f("print"),
                f(
                        name = "prompt",
                        parameters = listOf(
                                p("message", "string", nullable = true),
                                p("_default", "string", nullable = true)),
                        returns = "string|null"
                ),
                f("releaseEvents"),
                f(
                        "requestAnimationFrame",
                        parameters = listOf(
                                p("callback", "FrameRequestCallback")
                        )
                ),
                f(
                        name = "resizeBy",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        name = "resizeTo",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        "scroll",
                        parameters = listOf(
                                p("options", "ScrollToOptions", nullable = true)
                        )
                ),
                f(
                        name = "scroll",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        "scrollBy",
                        parameters = listOf(
                                p("options", "ScrollToOptions", nullable = true)
                        )
                ),
                f(
                        name = "scrollBy",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f(
                        "scrollTo",
                        parameters = listOf(
                                p("options", "ScrollToOptions", nullable = true)
                        )
                ),
                f(
                        name = "scrollTo",
                        parameters = listOf(
                                p("x", "number"),
                                p("y", "number"))
                ),
                f("webkitCancelAnimationFrame", parameters = listOf(p("handle", "int"))),
                f(
                        name = "webkitConvertPointFromNodeToPage",
                        parameters = listOf(
                                p("node", "Node"),
                                p("pt", "WebKitPoint")),
                        returns = "WebKitPoint"
                ),
                f(
                        name = "webkitConvertPointFromPageToNode",
                        parameters = listOf(
                                p("node", "Node"),
                                p("pt", "WebKitPoint")),
                        returns = "WebKitPoint"
                ),
                f("webkitRequestAnimationFrame", parameters = listOf(p("callback", "FrameRequestCallback"))),
                f("webkitCancelAnimationFrame", parameters = listOf(p("handle", "int"))),
                f("toString", returns = "string"),
                f("dispatchEvent", parameters = listOf(p("event", "Event")))
        )
)

val globalJSClasses = listOf(
        c(
                className = "CFBundle",
                constructor = ctor(listOf(p("aURL", "CPUrl|String"))),
                staticFunctions = listOf(
                        f("environments"),
                        f("bundleContainingURL", listOf(
                                p("aURL", "CPURL")
                        )),
                        f("bundleContainingURL", listOf(
                                p("aURL", "CPURL")
                        )),
                        f(name = "mainBundle", returns = "CFBundle"),
                        f("bundleForClass", listOf(p("aClass", "objj_class"))),
                        f("bundleWithIdentifier", listOf(p("bundleID", "string")))
                ),
                functions = listOf(
                        f(name = "bundleURL", returns = "CFURL"),
                        f(name = "resourcesDirectoryURL", returns = "CFURL"),
                        f(name = "resourceURL", parameters = listOf(
                                p("aResourceName", "string"),
                                p("aType", "string"),
                                p("aSubDirectory", "string"),
                                p("localizationName", "string")
                        ), returns = "CFURL"
                        ),
                        f(name = "mostEligibleEnvironmentURL", returns = "CFURL"),
                        f(name = "executableURL", returns = "CFURL"),
                        f(name = "infoDictionary", returns = "?"),
                        f(name = "loadedLanguage", returns = "?"),
                        f(
                                name = "valueForInfoDictionaryKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = "?"
                        ),
                        f(name = "identifier", returns = "?"),
                        f(name = "hasSpritedImages", returns = "BOOL"),
                        f(name = "environments", returns = "?"),
                        f(
                                name = "mostEligibleEnvironment",
                                parameters = listOf(p("evironments", "array")),
                                returns = "?"
                        ),
                        f(name = "isLoading", returns = "BOOL"),
                        f(name = "isLoaded", returns = "BOOL"),
                        f(
                                name = "load",
                                parameters = listOf(p("shouldExecute", "BOOL"))
                        ),
                        f(
                                name = "addEventListener",
                                parameters = listOf(p("anEventName", "string"), p("anEventListener", "Function"))
                        ),
                        f(
                                name = "removeEventListener",
                                parameters = listOf(p("anEventName", "string"), p("anEventListener", "Function"))
                        ),
                        f(
                                name = "onerror",
                                parameters = listOf(p("anEvent", "Event"))
                        ),
                        f(name = "bundlePath", returns = "?"),
                        f(
                                name = "pathForResource",
                                parameters = listOf(
                                        p("aResourceName", "string"),
                                        p("aType", "string"),
                                        p("aSubDirectory", "string"),
                                        p("localizationName", "string")
                                ),
                                returns = "CFURL"
                        )
                )
        ),
        c(
                className = "CFData",
                properties = listOf(
                        p("isa", "CPData")
                ),
                functions = listOf(
                        f(name = "propertyList", returns = "CFPropertyList"),
                        f(name = "JSONObject", returns = "object"),
                        f(name = "rawString", returns = "string"),
                        f(name = "bytes", returns = "byte[]"),
                        f(name = "base64", returns = "string")
                ),
                staticFunctions = listOf(
                        f(
                                name = "decodeBase64ToArray",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = "byte[]"
                        ),
                        f(
                                name = "decodeBase64ToString",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = "string"
                        ),
                        f(
                                name = "decodeBase64ToUtf16String",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = "string"
                        ),
                        f(
                                name = "encodeBase64Array",
                                parameters = listOf(p("input", "string[]")), // @todo check if this is correct
                                returns = "string"
                        )
                )
        ),
        c(
                className = "CFMutableData",
                extends = listOf("CFData"),
                properties = listOf(
                        p("isa", "CPMutableData")
                ),
                functions = listOf(
                        f(
                                name = "setPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFormat", "Format"))
                        ),
                        f(
                                name = "setJSONObject",
                                parameters = listOf(
                                        p("anObject", "object"))
                        ),
                        f(
                                name = "setRawString",
                                parameters = listOf(
                                        p("aString", "string"))
                        ),
                        f(
                                name = "setBytes",
                                parameters = listOf(
                                        p("bytes", "byte[]"))
                        ),
                        f(
                                name = "setBase64String",
                                parameters = listOf(
                                        p("aBase64String", "string"))
                        ),
                        f(
                                name = "bytesToString",
                                parameters = listOf(p("bytes", "byte[]")),
                                returns = "string"
                        ),
                        f(
                                name = "stringToBytes",
                                parameters = listOf(p("input", "string")),
                                returns = "byte[]"
                        ),
                        f(
                                name = "encodeBase64String",
                                parameters = listOf(p("input", "string")),
                                returns = "string"
                        ),
                        f(
                                name = "bytesToUtf16String",
                                parameters = listOf(p("bytes", "byte[]")),
                                returns = "string"
                        ),
                        f(
                                name = "encodeBase64Utf16String",
                                parameters = listOf(p("input", "string")),
                                returns = "string"
                        )
                )
        ),
        c(
                className = "CFDictionary",
                constructor = ctor(listOf(p("aDictionary", "CFDictionary"))),
                properties = listOf(
                        p("isa", "CPDictionary")
                ),
                functions = listOf(
                        f(name = "copy", returns = "CFDictionary"),
                        f(name = "mutableCopy", returns = "CFMutableDictionary"),
                        f(
                                name = "containsKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = "BOOL"
                        ),
                        f(
                                name = "containsValue",
                                parameters = listOf(p("anObject", "id")),
                                returns = "BOOL"
                        ),
                        f(name = "count", parameters = listOf(), returns = "int"),
                        f(name = "countOfKey", parameters = listOf(p("aKey", "string")), returns = "int"),
                        f(name = "countOfValue", parameters = listOf(p("anObject", "id")), returns = "int"),
                        f(name = "keys", returns = "string[]"),
                        f(name = "valueForKey", parameters = listOf(p("aKey", "string")), returns = "id"),
                        f(name = "toString", returns = "string")
                )
        ),
        c(
                className = "CFMutableDictionary",
                extends = listOf("CFDictionary"),
                properties = listOf(
                        p("isa", "CPMutableDictionary")
                ),
                functions = listOf(
                        f(
                                name = "addValueForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("anObject", "object")
                                )
                        ),
                        f("removeValueForKey", parameters = listOf(p("aKey", "string"))),
                        f("removeAllValues"),
                        f(
                                name = "replaceValueForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("anObject", "object")
                                )
                        ),
                        f(
                                name = "setValueForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("anObject", "object")
                                )

                        )
                )

        ),
        c(
                className = "CFError",
                properties = listOf(
                        p("isa", "CPError")
                ),
                constructor = ctor(listOf(
                        p("domain", "string"),
                        p("code", "int"),
                        p("userInfo", "CFDictionary")
                )),
                functions = listOf(
                        f(name = "domain", returns = "string"),
                        f(name = "code", returns = "int"),
                        f(name = "description", returns = "string"),
                        f(name = "failureReason", returns = "string"),
                        f(name = "recoverySuggestion", returns = "?"),
                        f(name = "userInfo", returns = "CFDictionary")
                )
        ),
        c(
                className = "",
                functions = listOf(
                        f(name = "status", returns = "int"),
                        f(name = "statusText", returns = "string"),
                        f(name = "readyState", returns = "int"),
                        f(name = "success", returns = "BOOL"),
                        f(name = "responseText", returns = "?"),
                        f(
                                name = "setRequestHeader",
                                parameters = listOf(
                                        p("aHeader", "string"),
                                        p("aValue", "object")
                                )
                        ),
                        f(name = "responseXML", returns = "XMLNode"),
                        f(name = "responsePropertyList", returns = "CFPropertyList"),
                        f(name = "setTimeout", parameters = listOf(p("aTimeout", "int"))),
                        f(name = "getTimeout", parameters = listOf(p("aTimeout", "int")), returns = "int"),
                        f(name = "getAllResponseHeaders", returns = "?"),
                        f(name = "overrideMimeType", parameters = listOf(p("aMimeType", "string"))),
                        f(
                                name = "open",
                                parameters = listOf(
                                        p("aMethod", "string"),
                                        p("aURL", "string"),
                                        p("isAsynchronous", "BOOL"),
                                        p("aUser", "string"),
                                        p("aPassword", "string")

                                )
                        ),
                        f(name = "send", parameters = listOf(p("aBody", "object"))),
                        f(name = "abort", returns = "?"),
                        f(
                                name = "addEventListener",
                                parameters = listOf(
                                        p("anEventName", "string"),
                                        p("anEventListener", "Function")
                                )
                        ),
                        f(
                                name = "removeEventListener",
                                parameters = listOf(
                                        p("anEventName", "string"),
                                        p("anEventListener", "Function")
                                )
                        ),
                        f(name = "setWithCredentials", parameters = listOf(p("willSendWithCredentials", "BOOL"))),
                        f(name = "withCredentials", returns = "BOOL"),
                        f(name = "isTimeoutRequest", returns = "BOOL")
                )
        ),
        c(
                className = "CFPropertyList",
                staticFunctions = listOf(
                        f(
                                name = "propertyListFromData",
                                parameters = listOf(
                                        p("aData", "Data"),
                                        p("aFormat", "Format")
                                ),
                                returns = "CFPropertyList"
                        ),
                        f(
                                name = "propertyListFromString",
                                parameters = listOf(
                                        p("aString", "string"),
                                        p("aFormat", "Format")
                                ),
                                returns = "CFPropertyList"

                        ),
                        f(
                                name = "propertyListFromXML",
                                parameters = listOf(
                                        p("aStringOrXMLNode", "string|XMLNode")),
                                returns = "CFPropertyList"
                        ),
                        f(name = "sniffedFormatOfString", parameters = listOf(p("aStrign", "string")), returns = "int"),
                        f(
                                name = "dataFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFormat", "Format")
                                ),
                                returns = "CFMutableData"
                        ),
                        f(
                                name = "stringFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFormat", "Format")
                                ),
                                returns = "string"
                        ),
                        f(
                                name = "readPropertyListFromFile",
                                parameters = listOf(
                                        p("aFile", "string")
                                )
                        ),
                        f(
                                name = "writePropertyListToFile",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFile", "string"),
                                        p("aFormat", "Format")
                                )
                        ),
                        f(
                                name = "modifyPlist",
                                parameters = listOf(
                                        p("aFileName", "string"),
                                        p("aCallback", "Function"),
                                        p("aFormat", "Format")
                                )
                        )

                )

        ),
        c(
                className = "CFURL",
                constructor = ctor(listOf(
                        p("aURL", "string|CFURL"),
                        p("aBaseURL", "CFURL")
                )),
                functions = listOf(
                        f("UID", returns = "int"),
                        f("mappedURL", returns = "CFURL"),
                        f(
                                name = "setMappedURLForURL",
                                parameters = listOf(
                                        p("fromURL", "CFURL"),
                                        p("toURL", "CFURL")
                                )
                        ),
                        f(name = "schemeAndAuthority", returns = "string"),
                        f(name = "absoluteString", returns = "string"),
                        f(name = "toString", returns = "string"),
                        f(name = "absoluteURL", returns = "CFURL"),
                        f(name = "standardizedURL", returns = "CFURL"),
                        f(name = "string", returns = "string"),
                        f(name = "authority", returns = "?"),
                        f(name = "hasDirectoryPath", returns = "BOOL"),
                        f(name = "hostName", returns = "?"),
                        f(name = "fragment", returns = "?"),
                        f(name = "lastPathComponent", returns = "?"),
                        f(name = "createCopyDeletingLastPathComponent", returns = "CFURL"),
                        f(name = "pathComponents", returns = "?"),
                        f(name = "pathExtension", returns = "string"),
                        f(name = "queryString", returns = "string"),
                        f(name = "scheme", returns = "string"),
                        f(name = "user", returns = "string"),
                        f(name = "password", returns = "string"),
                        f(name = "portNumber", returns = "int"),
                        f(name = "domain", returns = "string"),
                        f(name = "baseURL", returns = "?"),
                        f(name = "asDirectoryPathURL", returns = "CFURL"),
                        f(name = "resourcePropertyForKey", parameters = listOf(p("aKey", "string")), returns = "?"),
                        f(
                                name = "setResourcePropertyForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("aValue", "id")
                                )
                        ),
                        f(name = "staticResourceData", returns = "CFMutableData")
                )
        ),
        c(
                className = "objj_ivar",
                constructor = ctor(parameters = listOf(p("aName", "string"), p("aType", "string"))),
                properties = listOf(
                        p("name", "string"),
                        p("type", "string")
                )
        ),
        c(
                className = "objj_class",
                properties = listOf(
                        p("isa"),
                        p("version", "int"),
                        p("super_class", "int"),
                        p("name", "string"),
                        p("info"),
                        p("ivar_list", "objj_ivar[]"),
                        p("ivar_store"),
                        p("ivar_dtable"),
                        p("method_list", "?[]"),
                        p("method_store"),
                        p("method_dtable"),
                        p("protocol_list", "?[]"),
                        p("allocator")
                ),
                functions = listOf(
                        f(name = "objj_msgSend"),
                        f(name = "objj_msgSend0"),
                        f(name = "objj_msgSend1"),
                        f(name = "objj_msgSend2"),
                        f(name = "objj_msgSend3"),
                        f(name = "method_msgSend"),
                        f(name = "toString", returns = "string")
                )
        ),
        c(
                className = "objj_protocol",
                constructor = ctor(listOf(p("aName", "string"))),
                properties = listOf(
                        p("name", "string"),
                        p("instance_methods", "object"),
                        p("class_methods", "object")
                )
        ),
        c(
                className = "objj_object",
                properties = listOf(
                        p("isa")
                )
        ),
        c(
                className = "objj_protocol",
                constructor = ctor(listOf(p("aName", "string"))),
                properties = listOf(
                        p("name", "string"))
        ),
        c(
                className = "objj_method",
                constructor = ctor(parameters = listOf(
                        p("aName", "string"),
                        p("anImplementation", "IMP"),
                        p("types", "string[]")
                )
                ),
                properties = listOf(
                        p("method_name", "string"),
                        p("method_imp", "IMP"),
                        p("method_types", "string[]")
                )
        ),
        c(
                className = "objj_typeDef",
                constructor = ctor(listOf(p("aName", "string")))
        ),
        Window,
        c(
                className = "Event",
                constructor = ctor(
                        parameters = listOf(
                                p("type", "string"),
                                p("eventInitDict", "EventInit", nullable = true)
                        )
                ),
                properties = listOf(
                        p("bubbles", "BOOL", readonly = true, comment = "Returns true or false depending on how event was initialized. True if event goes through its target's ancestors in reverse tree order, and false otherwise."),
                        p("cancelBubble", "BOOL"),
                        p("cancelable", "BOOL", readonly = true),
                        p("composed", "BOOL", readonly = true, comment = "Returns true or false depending on how event was initialized. True if event invokes listeners past a ShadowRoot node that is the root of its target, and false otherwise."),
                        p("currentTarget", "EventTarget", readonly = true, nullable = true, comment = "Returns the object whose event listener's callback is currently being invoked"),
                        p("defaultPrevented", "BOOL", readonly = true),
                        p("eventPhase", "number", readonly = true),
                        p("isTrusted", "BOOL", readonly = true, comment = "Returns true if event was dispatched by the user agent, and false otherwise"),
                        p("returnValue", "BOOL"),
                        p("srcElement", "EventTarget", readonly = true, nullable = true),
                        p("target", "EventTarget", readonly = true, comment = "Returns the object to which event is dispatched (its target).", nullable = true),
                        p("timeStamp", "number", readonly = true, comment = "Returns the event's timestamp as the number of milliseconds measured relative to the time origin"),
                        p("type", "string", readonly = true, comment = "Returns the type of event, e.g. \"click\", \"hashchange\", or \"submit\"."),
                        p("AT_TARGET", "number", readonly = true),
                        p("BUBBLING_PHASE", "number", readonly = true),
                        p("CAPTURING_PHASE", "number", readonly = true),
                        p("NONE", "number", readonly = true)
                ),
                staticProperties = listOf(
                        p("AT_TARGET", "number", readonly = true),
                        p("BUBBLING_PHASE", "number", readonly = true),
                        p("CAPTURING_PHASE", "number", readonly = true),
                        p("NONE", "number", readonly = true)
                ),
                functions = listOf(
                        f("composedPath", returns = "EventTarget[]"),
                        f(
                                name = "initEvent",
                                parameters = listOf(
                                        p("type", "string"),
                                        p("bubbles", "BOOL", nullable = true),
                                        p("cancelable", "BOOL", nullable = true)
                                )),
                        f("preventDefault"),
                        f("stopImmediatePropagation",
                                comment = "Invoking this method prevents event from reaching any registered event listeners after the current one finishes running and, when dispatched in a tree, also prevents event from reaching any other objects."),
                        f("stopPropagation", comment = "When dispatched in a tree, invoking this method prevents event from reaching any objects other than the current object.")
                )
        ),
        c(
                className = "EventTarget",
                constructor = ctor(),
                functions = listOf(
                        f("addEventListener", listOf(
                                p("type", "string"),
                                p("listener", "EventListenerOrEventListenerObject", nullable = true),
                                p("options", "BOOL|AddEventListenerOptions", nullable = true)
                        )),
                        f("dispatchEvent", listOf(
                                p("event", "Event")),
                                returns = "BOOL"
                        ),
                        f("removeEventListener", listOf(
                                p("type", "string"),
                                p("listener", "EventListenerOrEventListenerObject", nullable = true),
                                p("options", "BOOL|AddEventListenerOptions", nullable = true))

                        )
                )
        ),
        c(
                className = "ThemeState",
                functions = listOf(
                        f("toString", returns = "string"),
                        f("hasThemeState", parameters = listOf(p("aState", "?")), returns = "BOOL"),
                        f("isSubsetOf", parameters = listOf(p("aState", "?")), returns = "BOOL"),
                        f("without", parameters = listOf(p("aState", "?")), returns = "ThemeState"),
                        f("and", parameters = listOf(p("aState", "?")), returns = "ThemeState")
                )
        ),
        c(
                className = "String",
                constructor = ctor(listOf(p("aString", "string"))),
                properties = listOf(
                        p("isa", "CPString")
                ),
                functions = listOf(
                        f("escapeForRegExp", returns = "string"),
                        f("stripDiacritics", returns = "string")
                )
        ),
        c(
                className = "Boolean",
                constructor = ctor(listOf(p("aBool", "BOOL"))),
                properties = listOf(
                        p("isa", "CPNumber")
                )
        ),
        c(
                className = "BlendTask",
                functions = listOf(
                        f("packageType", returns = "string"),
                        f("infoPlist", returns = "?"),
                        f("themeDescriptors", returns = "?"),
                        f("setThemeDescriptors", parameters = listOf(p("themeDescriptors", "?[]|FileList[]"))),
                        f("defineTasks", parameters = listOf(p("...args", "?"))),
                        f("defineSourceTasks"),
                        f("defineThemeDescriptorTasks")
                )
        ),
        c(
                className = "CompletionHandlerAgent",
                constructor = ctor(listOf(p("aCompletionHandler", "?"))),
                properties = listOf(
                        p("total", "number"),
                        p("valid", "BOOL"),
                        p("id", "number")
                ),
                functions = listOf(
                        f("fire"),
                        f("increment", listOf(p("inc"))),
                        f("decrement"),
                        f("invalidate")
                )
        ),
        c(
                className = "FrameUpdater",
                constructor = ctor(listOf(p("anIdentifier"))),
                functions = listOf(
                        f("start"),
                        f("stop"),
                        f("updateFunction", returns = "?"),
                        f("identifier", returns = "?"),
                        f("description", returns = "string"),
                        f(
                                name = "addTarget",
                                parameters = listOf(
                                        p("target"),
                                        p("keypath"),
                                        p("duration"))
                        )
                )
        ),
        c(
                className = "CSSAnimation",
                constructor = ctor(listOf(
                        p("aTarget", "DOMElement"),
                        p("anIdentifier", "string"),
                        p("aTargetName", "string")
                )),
                properties = listOf(
                        p("target", "DOMElement"),
                        p("identifier", "string"),
                        p("targetName", "string"),
                        p("animationName", "string"),
                        p("listener", nullable = true),
                        p("styleElement", nullable = true),
                        p("propertyanimations", "?[]"),
                        p("animationsnames", "?[]"),
                        p("animationstimingfunctions", "?[]"),
                        p("animationsdurations", "?[]"),
                        p("islive", "BOOL"),
                        p("didBuildDOMElements", "BOOL")
                ),
                functions = listOf(
                        f("description", returns = "string"),
                        f(
                                name = "addPropertyAnimation",
                                parameters = listOf(
                                        p("propertyName", "string"),
                                        p("valueFunction", "Function"),
                                        p("aDuration", "number"),
                                        p("aKeyTimes", "d|d[]"),
                                        p("aValues", "?[]"),
                                        p("aTimingFunctions", "d[]|d[][]", comment = "[d,d,d,d] | [[d,d,d,d]]"),
                                        p("aCompletionfunction", "Function")
                                ),
                                returns = "BOOL"
                        ),
                        f("keyFrames", returns = "string"),
                        f("appendKeyFramesRule"),
                        f("createKeyFramesStyleElement"),
                        f("endEventListener", returns = "AnimationEndListener"),
                        f("completionFunctionForAnimationName", parameters = listOf(p("aName", "string")), returns = "Function"),
                        f("addAnimationEndEventListener"),
                        f("setTargetStyleProperties"),
                        f("buildDOMElements"),
                        f("start", returns = "BOOL")

                )
        ),
        c(
                className = "Date",
                properties = listOf(
                        p("isa", "CPDate")
                ),
                staticFunctions = listOf(
                        f("parseISO8601", listOf(p("aData", "Date")), returns = "number")
                )
        ),
        c(
                className = "Error",
                properties = listOf(
                        p("isa", "CPException")
                )
        ),
        c(
                className = "Number",
                properties = listOf(
                        p("isa", "CPNumber")
                )
        ),
        c(
                className = "Math",
                static = true
        ),
        c(
                className = "Document"
        ),
        c(
                className = "Array"
        ),
        c(
                className = "RegExp",
                constructor = ctor(listOf(
                        p("pattern", "string|RegExp"),
                        p("flags", "string", nullable = true)
                )),
                functions = listOf(
                        f(
                                name = "exec",
                                parameters = listOf(p("string", "string", comment = "The String object or string literal on which to perform the search.")),
                                returns = "string[]|null",
                                comment = "Executes a search on a string using a regular expression pattern, and returns an array containing the results of that search."
                        ),
                        f(
                                name = "test",
                                parameters = listOf(
                                        p("string", "string", comment = "String on which to perform the search")),
                                returns = "BOOL",
                                comment = "Returns a Boolean value that indicates whether or not a pattern exists in a searched string.")
                ),
                properties = listOf(
                        p("source", "string", readonly = true, comment = "Returns a copy of the text of the regular expression pattern"),
                        p("global", "BOOL", readonly = true, comment = "Returns a Boolean value indicating the state of the global flag (g) used with a regular expression.", default = "false"),
                        p("ignoreCase", "BOOL", readonly = true, comment = "Returns a Boolean value indicating the state of the ignoreCase flag (i) used with a regular expression.", default = "false"),
                        p("multiline", "BOOL", readonly = true, comment = "Returns a Boolean value indicating the state of the multiline flag (m) used with a regular expression.", default = "false")

                )
        ),
        c(
                className = "Image"
        ),
        c(
                className = "CPLog",
                staticFunctions = listOf(
                        f("fatal"),
                        f("error"),
                        f("warn"),
                        f("info"),
                        f("debug"),
                        f("trace")
                )
        ),
        c(
                className = "Slotable",
                properties = listOf(
                        p(name = "assignedSlot", type = "HTMLSlotElement | null", readonly = true)
                )
        ),
        JsClassHTMLElement,
        JsClassGlobalEventHandlers,
        JsClassCSSStyleDeclaration,
        JsClassAnimatable,
        JsClassChildNode,
        JsClassElementCSSInlineStyle,
        JsClassEventTarget

)

val globalJSClassNames = globalJSClasses.filter { !it.isStruct }.map { it.className }

fun getObjJAndJsClassObjects(project: Project, className: String? = null): List<GlobalJSClass> {
    if (className == null)
        return globalJSClasses + AllObjJClassesAsJsClasses(project)
    return globalJSClasses.filter { it.className == className } + AllObjJClassesAsJsClasses(project).filter { it.className == className }
}