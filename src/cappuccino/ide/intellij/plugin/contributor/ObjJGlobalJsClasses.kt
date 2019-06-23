package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.inference.*
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

data class GlobalJSClass(
        val className: String,
        val constructor: GlobalJSConstructor = GlobalJSConstructor(),
        val functions: List<GlobalJSClassFunction> = listOf(),
        val staticFunctions: List<GlobalJSClassFunction> = listOf(),
        val properties: List<JsNamedProperty> = listOf(),
        val staticProperties: List<JsNamedProperty> = listOf(),
        val extends: List<String> = listOf(),
        val comment: String? = null,
        val static: Boolean = false,
        val isStruct: Boolean = false,
        val isObjJ: Boolean = false) // Used to show that this is not a true object kind, but rather a descriptive object

data class JsNamedProperty(
        val name: String,
        override val type: String = "Any",
        val isPublic: Boolean = true,
        override val nullable: Boolean = true,
        override val readonly: Boolean = false,
        override val comment: String? = null,
        override val default: String? = null,
        val ignore: Boolean = false,
        override val callback: AnonymousJsFunction? = null,
        val deprecated: Boolean = false,
        val varArgs: Boolean = type.startsWith("...")
) : JsProperty


val JsNamedProperty.types: Set<String>
    get() {
        return type.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toSet()
    }

fun StubOutputStream.writeNamedProperty(property: JsNamedProperty) {
    writeName(property.name)
    writeName(property.type)
    writeBoolean(property.isPublic)
    writeBoolean(property.nullable)
    writeUTFFast(property.comment ?: "")
    writeUTFFast(property.default ?: "")
    writeBoolean(property.ignore)
    writeBoolean(property.callback != null)
    writeAnonymousFunction(property.callback)
    writeBoolean(property.deprecated)
    writeBoolean(property.varArgs)
}

fun StubInputStream.readNamedProperty(): JsNamedProperty {
    val name = readNameString() ?: "?"
    val type = readNameString() ?: ""
    val isPublic = readBoolean()
    val nullable = readBoolean()
    val comment = readUTFFast()
    val default = readUTFFast()
    val ignore = readBoolean()
    val callback = readAnonymousFunction()
    val deprecated = readBoolean()
    val varArgs = readBoolean()
    return JsNamedProperty(
            name = name,
            type = type,
            isPublic = isPublic,
            nullable = nullable,
            comment = comment,
            default = default,
            ignore = ignore,
            callback = callback,
            deprecated = deprecated,
            varArgs = varArgs
    )
}

data class JsFunctionReturnType(
        override val type: String = "void",
        override val nullable: Boolean = true,
        override val readonly: Boolean = false,
        override val comment: String? = null,
        override val default: String? = null,
        override val callback: AnonymousJsFunction? = null) : JsProperty {
    val types: Set<String> by lazy {
        type.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX)
                .toSet()
    }
}

fun JsFunctionReturnType.toInferenceResult(): InferenceResult {
    val typesSplit = type.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toSet()
    val classes = typesSplit.filterNot {
        it == "Function" || it.contains("[]")
    }.toSet()
    val arrayTypes = typesSplit.filterNot {
        it.endsWith("[]")
    }.map {
        it.removeSuffix("[]")
    }
    val functionTypes = if ("Function" in typesSplit) {
        if (callback != null) {
            listOf(callback.toJsFunctionType())
        } else {
            listOf(AnonymousJsFunction().toJsFunctionType())
        }
    } else if (callback != null) {
        listOf(callback.toJsFunctionType())
    } else {
        null
    }
    return InferenceResult(
            classes = classes,
            arrayTypes = if (arrayTypes.isNotEmpty()) arrayTypes.toSet() else null,
            functionTypes = functionTypes
    )
}

fun StubOutputStream.writeFunctionReturnType(type: JsFunctionReturnType?) {
    writeBoolean(type != null)
    if (type == null)
        return
    writeName(type.type)
    writeBoolean(type.nullable)
    writeBoolean(type.readonly)
    writeUTFFast(type.comment ?: "")
    writeUTFFast(type.default ?: "")
    writeAnonymousFunction(type.callback)
}

fun StubInputStream.readFunctionReturnType(): JsFunctionReturnType? {
    if (!readBoolean())
        return null
    val type = readNameString() ?: "Any"
    val nullable = readBoolean()
    val readonly = readBoolean()
    val comment = readUTFFast()
    val default = readUTFFast()
    val callback = readAnonymousFunction()
    return JsFunctionReturnType(
            type = type,
            nullable = nullable,
            readonly = readonly,
            comment = if (comment.isNotBlank()) comment else null,
            default = if (default.isNotBlank()) default else null,
            callback = callback
    )
}


interface JsFunction {
    val name: String
    val parameters: List<JsNamedProperty>
    val returns: JsFunctionReturnType?
    val comment: String?
}

val JsFunction.returnTypes: Set<String>
    get() {
        return returns?.types ?: setOf(VOID.type)
    }

interface JsProperty {
    val type: String
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
    val callback: AnonymousJsFunction?
}

data class AnonymousJsFunction(
        val parameters: List<JsNamedProperty> = emptyList(),
        val returns: JsFunctionReturnType? = null,
        val comment: String? = null
)

fun AnonymousJsFunction.toJsFunctionType(): JsFunctionType {
    return JsFunctionType(
            parameters = parameters.toMap(),
            returnType = returns?.toInferenceResult() ?: INFERRED_VOID_TYPE
    )
}

fun StubOutputStream.writeAnonymousFunction(function: AnonymousJsFunction?) {
    writeBoolean(function != null)
    if (function == null)
        return
    writeNamedPropertiesList(function.parameters)
    writeFunctionReturnType(function.returns)
    writeUTFFast(function.comment ?: "")
}

fun StubInputStream.readAnonymousFunction(): AnonymousJsFunction? {
    if (!readBoolean())
        return null
    val parameters = readNamedPropertiesList()
    val returnType = readFunctionReturnType()
    val comment = readUTFFast()
    return AnonymousJsFunction(
            parameters = parameters,
            returns = returnType,
            comment = if (comment.isNotBlank()) comment else null
    )
}

fun StubOutputStream.writeNamedPropertiesList(properties: List<JsNamedProperty>) {
    writeInt(properties.size)
    for (property in properties) {
        writeNamedProperty(property)
    }
}

fun StubInputStream.readNamedPropertiesList(): List<JsNamedProperty> {
    val out = mutableListOf<JsNamedProperty>()
    val numProperties = readInt()
    for (i in 0 until numProperties) {
        val item = readNamedProperty()
        out.add(item)
    }
    return out
}


data class GlobalJSClassFunction(override val name: String, override val parameters: List<JsNamedProperty> = listOf(), override val returns: JsFunctionReturnType? = null, val isPublic: Boolean = true, override val comment: String? = null) : JsFunction
data class GlobalJSConstructor(val parameters: List<JsNamedProperty> = listOf())

typealias c = GlobalJSClass
typealias ctor = GlobalJSConstructor
typealias f = GlobalJSClassFunction
typealias p = JsNamedProperty
typealias callback = AnonymousJsFunction
typealias rt = JsFunctionReturnType

val RT_STRING = rt("string")
val RT_BOOL = rt("BOOL")
val RT_ANY = rt("?", nullable = true)
val RT_CFURL = rt("CFURL")
val RT_NUMBER = rt("number")

val Window: GlobalJSClass = c(
        className = "Window",
        properties = listOf(
                p("sessionStorage", "Storage", readonly = true),
                p("localStorage", "Storage", readonly = true),
                p("console", "Console", nullable = true, readonly = true),
                p("onabort", callback = callback(), comment = "Fires when the user aborts the download.", nullable = true, ignore = true),
                p("onanimationcancel", callback = callback(parameters = listOf(p("this", "Window"), p("ev", "Event")), returns = rt("void")), nullable = true, ignore = true),
                p("onanimationend", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onanimationiteration", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onanimationstart", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onauxclick", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onafterprint", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onblur", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the object loses the input focus.", ignore = true),
                p("oncancel", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("oncanplay", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when playback is possible, but would require further buffering.", ignore = true),
                p("oncanplaythrough", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onchange", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the contents of the object or selection have changed.", ignore = true),
                p("onclick", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user clicks the left mouse button on the object", ignore = true),
                p("onclose", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("oncontextmenu", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user clicks the right mouse button in the client area, opening the context menu.", ignore = true),
                p("oncuechange", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ondblclick", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user double-clicks the object.", ignore = true),
                p("ondrag", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the source object continuously during a drag operation.", ignore = true),
                p("ondragend", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the source object when the user releases the mouse at the close of a drag operation.", ignore = true),
                p("ondragenter", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondragexit", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondragleave", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the target object when the user moves the mouse out of a valid drop target during a drag operation.", ignore = true),
                p("ondragover", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the target element continuously while the user drags the object over a valid drop target.", ignore = true),
                p("ondragstart", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the source object when the user starts to drag a text selection or selected object.", ignore = true),
                p("ondrop", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires on the target element when the user drags the object to a valid drop target.", ignore = true),
                p("ondurationchange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the duration attribute is updated.", ignore = true),
                p("onemptied", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the media element is reset to its initial state.", ignore = true),
                p("onended", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the end of playback is reached.", ignore = true),
                p("onerror", "OnErrorEventHandler", nullable = true, comment = "Fires when an error occurs during object loading.", ignore = true),
                p("onfocus", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the object receives focus.", ignore = true),
                p("ongotpointercapture", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("oninput", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("oninvalid", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onkeydown", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user presses a key.", ignore = true),
                p("onkeypress", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user presses an alphanumeric key.", ignore = true),
                p("onkeyup", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user releases a key", ignore = true),
                p("onload", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires immediately after the browser loads the object.", ignore = true),
                p("onloadeddata", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when media data is loaded at the current playback position.", ignore = true),
                p("onloadmetadata", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the duration and dimensions of the media have been determined.", ignore = true),
                p("onloadend", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "ProgressEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onloadstart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when Internet Explorer begins looking for media data.", ignore = true),
                p("onlostpointercapture", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmousedown", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user clicks the object with either mouse button.", ignore = true),
                p("onmouseenter", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmouseenterleave", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmousemove", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user moves the mouse over the object.", ignore = true),
                p("onmouseout", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user moves the mouse pointer outside the boundaries of the object.", ignore = true),
                p("onmouseover", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user moves the mouse pointer into the object.", ignore = true),
                p("onmouseup", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user releases a mouse button while the mouse is over the object.", ignore = true),
                p("onmousedown", callback = callback(parameters = listOf(
                        p("this: Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpause", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when playback is paused.", ignore = true),
                p("onplay", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the play method is requested.", ignore = true),
                p("onplaying", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the audio or video has started playing.", ignore = true),
                p("onpointercancel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerdown", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerenter", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerleave", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointermove", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerout", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerover", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpointerup", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PointerEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onprogress", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "ProgressEvent")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs to indicate progress while downloading media data.", ignore = true),
                p("onratechange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the playback rate is increased or decreased.", ignore = true),
                p("onreset", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user resets a form.", ignore = true),
                p("onresize", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "UIEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onscroll", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the user repositions the scroll box in the scroll bar on the object.", ignore = true),
                p("onsecuritypolicyviolation", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "SecurityPolicyViolationEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onseeked", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the seek operation ends.", ignore = true),
                p("onseeking", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the current playback position is moved.", ignore = true),
                p("onselect", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Fires when the current selection changes.", ignore = true),
                p("onselectionchange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onselectstart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onstalled", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the download has stopped.", ignore = true),
                p("onsubmit", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onsuspend", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs if the load operation has been intentionally halted.", ignore = true),
                p("ontimeupdate", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs to indicate the current playback position.", ignore = true),
                p("ontoggle", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontouchcancel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TouchEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontouchend", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TouchEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontouchmove", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TouchEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontouchstart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TouchEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontransitioncancel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontransitionend", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontransitionrun", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontransitionstart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ontouchcancel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "TouchEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvolumechange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when the volume is changed, or playback is muted or unmuted.", ignore = true),
                p("onwaiting", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, comment = "Occurs when playback stops because the next frame of a video resource is not available.", ignore = true),
                p("onbeforeprint", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onbeforeunload", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "BeforeUnloadEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onhashchange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "HashChangeEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onlanguagechange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmessage", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "MessageEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmessageerror", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "MessageEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onoffline", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ononline", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpagehide", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PageTransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpageshow", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PageTransitionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onpopstate", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PopStateEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onrejectionhandled", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onstorage", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "StorageEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onunhandledrejection", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "PromiseRejectionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onunload", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onwheel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "WheelEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("oncompassneedscalibration", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ondevicelight", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "DeviceLightEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ondevicemotion", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "DeviceMotionEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("ondeviceorientation", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "DeviceOrientationEvent")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmousewheel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgesturechange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgesturedoubletap", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgestureend", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgesturehold", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgesturestart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsgesturetap", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmsinertiastart", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointercancel", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerdown", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerenter", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerleave", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointermove", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerout", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerover", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onmspointerup", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onorientationchange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onreadystatechange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplayactivate", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplayblur", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplayconnect", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplaydeactivate", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplaydisconnect", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplayfocus", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplaypointerrestricted", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplaypointerunrestricted", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
                p("onvrdisplaypresentchange", callback = callback(parameters = listOf(
                        p("this:Window"),
                        p(" ev", "Event")
                ),
                        returns = RT_ANY), nullable = true, ignore = true),
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
                f(name = "confirm", parameters = listOf(p(name = "message", type = "string", nullable = true)), returns = RT_BOOL),
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
                        returns = rt("CSSStyleDeclaration")
                ), f(
                name = "getMatchedCSSRules",
                parameters = listOf(
                        p("elt", "DOMElement"),
                        p(name = "psuedoElt", type = "string", nullable = true)),
                returns = rt("CSSRuleList")
        ),//
                f(
                        name = "getSelection",
                        returns = rt("Selection")
                ),
                f(
                        name = "matchMedia",
                        parameters = listOf(
                                p("query", "string")),
                        returns = rt("MediaQueryList")
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
                        returns = rt("Window", nullable = true)
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
                        returns = rt("string", nullable = true)
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
                        returns = rt("WebKitPoint")
                ),
                f(
                        name = "webkitConvertPointFromPageToNode",
                        parameters = listOf(
                                p("node", "Node"),
                                p("pt", "WebKitPoint")),
                        returns = rt("WebKitPoint")
                ),
                f("webkitRequestAnimationFrame", parameters = listOf(p("callback", "FrameRequestCallback"))),
                f("webkitCancelAnimationFrame", parameters = listOf(p("handle", "int"))),
                f("toString", returns = RT_STRING),
                f("dispatchEvent", parameters = listOf(p("event", "Event")))
        )
)

val JS_SYMBOL = c(
        "Symbol",
        constructor = ctor(listOf(p("name", "string"))),
        staticFunctions = listOf(
                f("for", listOf(p("name", "string")))
        )
)

val VOID = rt("void", nullable = true)

val JS_ANY = c(
        "?",
        extends = listOf("Object"),
        comment = "Object"
)

val JS_NULL = c(
        "null",
        extends = listOf("null", "primitive"),
        isStruct = true
)

val JS_UNDEFINED = c(
        "undefined",
        extends = listOf("primitive"),
        isStruct = true
)

val JS_BOOL = c(
        "BOOL",
        extends = listOf("primitive"),
        isStruct = true
)

val JS_BOOLEAN = c(
        "boolean",
        extends = listOf("BOOL", "primitive"),
        isStruct = true
)

val JS_NUMBER = c(
        "number",
        extends = listOf("primitive"),
        isStruct = true
)

val JS_INT = c(
        "int",
        extends = listOf("number", "primitive"),
        isStruct = true
)

val JS_CHAR = c(
        "char",
        extends = listOf("int", "number", "primitive"),
        isStruct = true
)

val JS_BYTE = c(
        "byte",
        extends = listOf("int", "number", "primitive"),
        isStruct = true
)

val JS_SHORT = c(
        "short",
        extends = listOf("int", "number", "primitive"),
        isStruct = true
)

val JS_INTEGER = c(
        "integer",
        extends = listOf("int", "number", "primitive"),
        isStruct = true
)

val JS_LONG = c(
        "long",
        extends = listOf("number", "primitive"),
        isStruct = true
)

val JS_LONG_LONG = c(
        "long long",
        extends = listOf("number", "long", "primitive"),
        isStruct = true
)

val JS_FLOAT = c(
        "float",
        extends = listOf("number", "primitive"),
        isStruct = true
)

val JS_DOUBLE = c(
        "double",
        extends = listOf("number", "primitive"),
        isStruct = true
)

val JS_PROTOTYPE = c(
        className = "prototype",
        functions = listOf(
                f(
                        name = "isPrototypeOf",
                        parameters = listOf(
                                p("Object", "Object")
                        ),
                        returns = RT_BOOL,
                        comment = "checks if an object exists in another object's prototype chain"
                )
        )
)

val JS_FUNCTION = c(
        className = "Function",
        isStruct = true
)

val JS_PROPERTY_DESCRIPTOR = c(
        "PropertyDescriptor",
        extends = listOf("DataDescriptor", "AccessorDescriptor"),
        isStruct = true,
        properties = listOf(
                p("configurable", "BOOL", comment = "true if and only if the type of this property descriptor may be changed and if the property may be deleted from the corresponding object.", default = "false"),
                p("enumerable", "BOOL", comment = "true if and only if this property shows up during enumeration of the properties on the corresponding object.", default = "false"),
                p("value", "?", comment = "The value associated with the property. Can be any valid JavaScript value (number, object, function, etc).", default = "undefined"),
                p("writable", "BOOL", comment = "true if and only if the value associated with the property may be changed with an assignment operator.", default = "false"),
                p("get", "Function", comment = "A function which serves as a getter for the property, or undefined if there is no getter. The function's return value will be used as the value of the property.", default = "undefined"),
                p("set", "Function", comment = "A function which serves as a setter for the property, or undefined if there is no setter. The function will receive as its only argument the new value being assigned to the property.", default = "undefined")
        )
)

val JS_DATA_DESCRIPTOR = c(
        className = "DataDescriptor",
        isStruct = true,
        properties = listOf(
                p("configurable", "BOOL", comment = "true if and only if the type of this property descriptor may be changed and if the property may be deleted from the corresponding object.", default = "false"),
                p("enumerable", "BOOL", comment = "true if and only if this property shows up during enumeration of the properties on the corresponding object.", default = "false"),
                p("value", "?", comment = "The value associated with the property. Can be any valid JavaScript value (number, object, function, etc).", default = "undefined"),
                p("writable", "BOOL", comment = "true if and only if the value associated with the property may be changed with an assignment operator.", default = "false")
        )
)

val JS_ACCESSOR_DESCRIPTOR = c(
        className = "AccessorDescriptor",
        isStruct = true,
        properties = listOf(
                p("configurable", "BOOL", comment = "true if and only if the type of this property descriptor may be changed and if the property may be deleted from the corresponding object.", default = "false"),
                p("enumerable", "BOOL", comment = "true if and only if this property shows up during enumeration of the properties on the corresponding object.", default = "false"),
                p("get", "Function", comment = "A function which serves as a getter for the property, or undefined if there is no getter. The function's return value will be used as the value of the property.", default = "undefined"),
                p("set", "Function", comment = "A function which serves as a setter for the property, or undefined if there is no setter. The function will receive as its only argument the new value being assigned to the property.", default = "undefined")

        )
)

val JS_PROPERTIES_OBJECT = c(
        className = "PropertiesObject",
        isStruct = true
)

val JS_ITERABLE = c(
        className = "iterable"
)

val JS_ARRAY = c(
        className = "Array",
        extends = listOf("iterable")
)

val JS_OBJECT = c(
        "Object",
        functions = listOf(
                f(
                        name = "hasOwnProperty",
                        parameters = listOf(p("propertyName", "string")),
                        returns = RT_BOOL,
                        comment = "Returns a boolean indicating whether the object has the specified property as its own property (as opposed to inheriting it)"
                ),
                f(
                        name = "propertyIsEnumerable",
                        parameters = listOf(p("prop", "string", comment = "The name of the property to test")),
                        returns = RT_BOOL,
                        comment = "returns a Boolean indicating whether the specified property is enumerable"
                ),
                f(
                        name = "tolocalestring",
                        returns = RT_STRING,
                        comment = "Returns a string representing the object. This method is meant to be overridden by derived objects for locale-specific purposes"
                ),
                f(
                        name = "toString",
                        returns = RT_STRING,
                        comment = "Returns a string representing the object"
                ),
                f(
                        name = "valueOf",
                        returns = RT_ANY,
                        comment = "Returns the primitive value of the specified object"
                )
        ),
        staticFunctions = listOf(
                f(
                        name = "assign",
                        parameters = listOf(
                                p("target", "Object", comment = "The target object"),
                                p("source", "Object", comment = "The source object(s)", varArgs = true)
                        ),
                        returns = rt("Object", comment = "The target object."),
                        comment = "The Object.assign() method is used to copy the values of all enumerable own properties from one or more source objects to a target object. It will return the target object"
                ),
                f(
                        name = "values",
                        parameters = listOf(
                                p("Object", "Object")
                        ),
                        returns = rt("?[]", comment = "An array containing the given object's own enumerable property values"),
                        comment = "Returns an array of a given object's own enumerable property values, in the same order as that provided by a for...in loop (the difference being that a for-in loop enumerates properties in the prototype chain as well)."
                ),
                f(
                        name = "create",
                        parameters = listOf(
                                p("proto", "prototype", comment = "The object which should be the prototype of the newly-created object."),
                                p("propertiesObject", "Object", comment = " If specified and not undefined, an object whose enumerable own properties (that is, those properties defined upon itself and not enumerable properties along its prototype chain) specify property descriptors to be added to the newly-created object, with the corresponding property names. These properties correspond to the second argument of Object.defineProperties().", nullable = true)
                        ),
                        returns = rt("Object", comment = "A new object with the specified prototype object and properties."),
                        comment = "Creates a new object, using an existing object as the prototype of the newly created object."
                ),
                f(
                        name = "defineProperties",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object on which to define or modify properties"),
                                p("props", "PropertiesObject", comment = "An object whose keys represent the names of properties to be defined or modified and whose values are objects describing those properties. Each value in props must be either a data descriptor or an accessor descriptor; it cannot be both (see Object.defineProperty() for more details).")
                        ),
                        returns = rt("Object", comment = "The object that was passed to the function."),
                        comment = "defines new or modifies existing properties directly on an object, returning the object."
                ),
                f(
                        name = "defineProperty",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object on which to define the property"),
                                p("property", "string|Symbol", comment = "The name or Symbol of the property to be defined or modified"),
                                p("descriptor", "DataDescriptor|AccessorDescriptor", comment = "The descriptor for the property being defined or modified")
                        ),
                        returns = rt("Object", comment = "The object that was passed to the function"),
                        comment = "defines a new property directly on an object, or modifies an existing property on an object, and returns the object.\n@returns The object that was passed to the function"
                ),
                f(
                        name = "entries",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object whose own enumerable string-keyed property [key, value] pairs are to be returned.")
                        ),
                        returns = rt("[key, value][]", comment = "An array of the given object's own enumerable string-keyed property [key, value] pairs"),
                        comment = "returns an array of a given object's own enumerable string-keyed property [key, value] pairs, in the same order as that provided by a for...in loop (the difference being that a for-in loop enumerates properties in the prototype chain as well). The order of the array returned by Object.entries() does not depend on how an object is defined. If there is a need for certain ordering then the array should be sorted first like Object.entries(obj).sort((a, b) => b[0].localeCompare(a[0]));"
                ),
                f(
                        name = "freeze",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object to freeze")
                        ),
                        returns = rt("Object", comment = "The object that was passed to the function"),
                        comment = "freezes an object. A frozen object can no longer be changed; freezing an object prevents new properties from being added to it, existing properties from being removed, prevents changing the enumerability, configurability, or writability of existing properties, and prevents the values of existing properties from being changed. In addition, freezing an object also prevents its prototype from being changed. freeze() returns the same object that was passed in."
                ),
                f(
                        name = "fromEntries",
                        parameters = listOf(
                                p("iterable", "iterable", comment = "An iterable such as Array or Map or other objects implementing the iterable protocol.")
                        ),
                        returns = rt("Object", comment = "A new object whose properties are given by the entries of the iterable")
                ),
                f(
                        name = "getOwnPropertyDescriptor",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object in which to look for the property"),
                                p("prop", "string|Symbol", comment = "The name or Symbol of the property whose description is to be retrieved")
                        ),
                        returns = rt("PropertyDescriptor", comment = "A property descriptor of the given property if it exists on the object, undefined otherwise.", nullable = true)
                ),
                f(
                        name = "getOwnPropertyDescriptors",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object for which to get all own property descriptors")
                        ),
                        returns = rt("Object", comment = "An object containing all own property descriptors of an object. Might be an empty object, if there are no properties."),
                        comment = "Returns all own property descriptors of a given object."
                ),
                f(
                        name = "getOwnPropertyNames",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object whose enumerable and non-enumerable properties are to be returned")
                        ),
                        returns = rt("string[]"),
                        comment = "Returns an array of all properties (including non-enumerable properties except for those which use Symbol) found directly in a given object."
                ),
                f(
                        name = "getOwnPropertySymbols",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object whose symbol properties are to be returned")
                        ),
                        returns = rt("Symbol[]", comment = "An array of all symbol properties found directly upon the given object"),
                        comment = "Returns an array of all symbol properties found directly upon a given object."
                ),
                f(
                        name = "getPrototypeOf",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object whose prototype is to be returned")
                        ),
                        returns = rt("prototype", comment = "The prototype of the given object. If there are no inherited properties, null is returned.", nullable = true)
                ),
                f(
                        name = "is",
                        parameters = listOf(
                                p("value1", "?", comment = "The first value to compare."),
                                p("value2", "?", comment = "The second value to compare.")
                        ),
                        returns = RT_BOOL.copy(comment = "A Boolean indicating whether or not the two arguments are the same value"),
                        comment = "Object.is() determines whether two values are the same value. Two values are the same if one of the following holds:\n" +
                                "\n" +
                                "both undefined\n" +
                                "both null\n" +
                                "both true or both false\n" +
                                "both strings of the same length with the same characters in the same order\n" +
                                "both the same object (means both object have same reference)\n" +
                                "both numbers and\n" +
                                "both +0\n" +
                                "both -0\n" +
                                "both NaN\n" +
                                "or both non-zero and both not NaN and both have the same value\n" +
                                "This is not the same as being equal according to the == operator. The == operator applies various coercions to both sides (if they are not the same Type) before testing for equality (resulting in such behavior as \"\" == false being true), but Object.is doesn't coerce either value.\n" +
                                "\n" +
                                "This is also not the same as being equal according to the === operator. The === operator (and the == operator as well) treats the number values -0 and +0 as equal and treats Number.NaN as not equal to NaN."
                ),
                f(
                        name = "isExtensible",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which should be checked")
                        ),
                        returns = RT_BOOL.copy(comment = "A Boolean indicating whether or not the given object is extensible"),
                        comment = "Determines if an object is extensible (whether it can have new properties added to it)."
                ),
                f(
                        name = "isFrozen",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which should be checked")
                        ),
                        returns = RT_BOOL.copy(comment = "A Boolean indicating whether or not the given object is frozen"),
                        comment = "Determines if an object is frozen.\nAn object is frozen if and only if it is not extensible, all its properties are non-configurable, and all its data properties (that is, properties which are not accessor properties with getter or setter components) are non-writable."
                ),
                f(
                        name = "isSealed",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which should be checked")
                        ),
                        returns = RT_BOOL.copy(comment = "A Boolean indicating whether or not the given object is sealed"),
                        comment = "Returns true if the object is sealed, otherwise false. An object is sealed if it is not extensible and if all its properties are non-configurable and therefore not removable (but not necessarily non-writable)."
                ),
                f(
                        name = "keys",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object of which the enumerable's own properties are to be returned.")
                        ),
                        returns = rt("string[]", comment = "An array of strings that represent all the enumerable properties of the given object."),
                        comment = "Returns an array of a given object's own property names, in the same order as we get with a normal loop."
                ),
                f(
                        name = "preventExtensions",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which should be made non-extensible.")
                        ),
                        returns = rt("Object", comment = "The object being made non-extensible."),
                        comment = "Prevents new properties from ever being added to an object (i.e. prevents future extensions to the object).\nAn object is extensible if new properties can be added to it. Object.preventExtensions() marks an object as no longer extensible, so that it will never have properties beyond the ones it had at the time it was marked as non-extensible. Note that the properties of a non-extensible object, in general, may still be deleted. Attempting to add new properties to a non-extensible object will fail, either silently or by throwing a TypeError (most commonly, but not exclusively, when in strict mode)."
                ),
                f(
                        name = "seal",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which should be sealed.")
                        ),
                        returns = rt("Object", comment = "The object being sealed"),
                        comment = "Seals an object, preventing new properties from being added to it and marking all existing properties as non-configurable. Values of present properties can still be changed as long as they are writable."
                ),
                f(
                        name = "setPrototypeOf",
                        parameters = listOf(
                                p("obj", "Object", comment = "The object which is to have its prototype set."),
                                p("prototype", "Object", comment = "The object's new prototype (an object or null).", nullable = true)
                        ),
                        returns = rt("Object", comment = "The specified object."),
                        comment = "Seals an object, preventing new properties from being added to it and marking all existing properties as non-configurable. Values of present properties can still be changed as long as they are writable."
                )


        ),
        properties = listOf(
                p("constructor", "Object")
        ),
        staticProperties = listOf(
                p("prototype", "prototype")
        )
)

val JS_STRING: GlobalJSClass = c(
        className = "string",
        constructor = ctor(listOf(p("aString", "string"))),
        properties = listOf(
                p("isa", "CPString")
        ),
        functions = listOf(
                f("escapeForRegExp", returns = RT_STRING),
                f("stripDiacritics", returns = RT_STRING),
                f("length", returns = RT_NUMBER, comment = "returns the length of a string"),
                f(
                        name = "indexOf",
                        parameters = listOf(
                                p("substring", type = "string", comment = "The string to find index for"),
                                p("startIndex", type = "number", nullable = true)
                        ),
                        returns = RT_NUMBER,
                        comment = "returns the index of the first occurrence of a specified text in a string"
                ),
                f(
                        name = "lastIndexOf",
                        parameters = listOf(
                                p("substring", type = "string", comment = "The string to find index for"),
                                p("endIndex", type = "number", nullable = true, comment = "Starts at this index and works backwards")
                        ),
                        returns = RT_NUMBER,
                        comment = "returns the index of the last occurrence of a specified text in a string"
                ),
                f(
                        name = "search",
                        parameters = listOf(
                                p("substring", type = "string", comment = "The string to find index for")
                        ),
                        returns = RT_NUMBER,
                        comment = "returns the index of the last occurrence of a specified text in a string"
                ),
                f(
                        name = "slice",
                        parameters = listOf(
                                p("start", "number"),
                                p("end", "number", comment = "exclusive", nullable = true)
                        ),
                        returns = RT_STRING,
                        comment = "extracts a part of a string and returns the extracted part in a new string\ncan use negative numbers to count from end"
                ),
                f(
                        name = "substring",
                        parameters = listOf(
                                p("start", "number"),
                                p("end", "number", nullable = true)
                        ),
                        returns = RT_STRING,
                        comment = "extracts a part of a string and returns the extracted part in a new string\ncannot accept negative indexes"
                ),
                f(
                        name = "substr",
                        parameters = listOf(
                                p("start", "number", comment = "Starting index(Inclusive)"),
                                p("length", "number", nullable = true, comment = "length of the extracted part")
                        ),
                        returns = RT_STRING,
                        comment = "Extracts a subset of a string given start index and length"

                ),
                f(
                        name = "replace",
                        parameters = listOf(
                                p("search", "string|RegExp"),
                                p("replacementString", "string")
                        ),
                        returns = RT_STRING,
                        comment = "Replaces first occurrence in a case sensitive manner.\nUse regex search parameter to replace all(/g) and do case insensitive(/i)"
                ),
                f(
                        name = "toUpperCase",
                        returns = RT_STRING,
                        comment = "returns a string is converted to upper case"
                ),

                f(
                        name = "toLowerCase",
                        returns = RT_STRING,
                        comment = "returns a string is converted to lower case"
                ),
                f(
                        name = "concat",
                        comment = "joins two or more strings",
                        parameters = listOf(p(
                                name = "args",
                                type = "string",
                                varArgs = true
                        ))
                ),
                f(
                        name = "trim",
                        comment = "removes whitespace from both sides of a string",
                        returns = RT_STRING
                ),
                f(
                        name = "charAt",
                        parameters = listOf(
                                p("index", "number")
                        ),
                        returns = RT_STRING
                ),

                f(
                        name = "charCodeAt",
                        parameters = listOf(
                                p("index", "number")
                        ),
                        returns = RT_NUMBER
                ),
                f(
                        name = "split",
                        parameters = listOf(
                                p("splitOn", "string")
                        ),
                        returns = rt("string[]")
                )
        )
)

private val JS_BOXED_NUMBER = c(
        "Number",
        constructor = ctor(listOf(p("value", "number"))),
        extends = listOf("number")
)

private val JS_BOXED_STRING = c(
        "String",
        constructor = ctor(listOf(p("value", "string"))),
        extends = listOf("number")
)

private val JS_BOXED_BOOLEAN = c(
        "Boolean",
        constructor = ctor(listOf(p("value", "BOOL"))),
        extends = listOf("number")
)

val globalJSClasses = listOf(
        JS_BOOL,
        JS_BOOLEAN,
        JS_INT,
        JS_BYTE,
        JS_SHORT,
        JS_LONG,
        JS_LONG_LONG,
        JS_FLOAT,
        JS_DOUBLE,
        JS_STRING,
        JS_OBJECT,
        JS_PROTOTYPE,
        JS_INTEGER,
        JS_CHAR,
        JS_NUMBER,
        JS_ITERABLE,
        JS_ACCESSOR_DESCRIPTOR,
        JS_DATA_DESCRIPTOR,
        JS_FUNCTION,
        JS_PROPERTIES_OBJECT,
        JS_FUNCTION,
        JS_SYMBOL,
        JS_NULL,
        JS_UNDEFINED,
        JS_BOXED_STRING,
        JS_BOXED_NUMBER,
        JS_BOXED_BOOLEAN,
        JS_PROPERTY_DESCRIPTOR,
        JS_ANY,
        JS_ARRAY,
        c(
                className = "DOMWindow",
                extends = listOf("Window")
        ),
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
                        f(name = "mainBundle", returns = rt("CFBundle")),
                        f("bundleForClass", listOf(p("aClass", "objj_class"))),
                        f("bundleWithIdentifier", listOf(p("bundleID", "string")))
                ),
                functions = listOf(
                        f(name = "bundleURL", returns = RT_CFURL),
                        f(name = "resourcesDirectoryURL", returns = RT_CFURL),
                        f(name = "resourceURL", parameters = listOf(
                                p("aResourceName", "string"),
                                p("aType", "string"),
                                p("aSubDirectory", "string"),
                                p("localizationName", "string")
                        ), returns = RT_CFURL
                        ),
                        f(name = "mostEligibleEnvironmentURL", returns = RT_CFURL),
                        f(name = "executableURL", returns = RT_CFURL),
                        f(name = "infoDictionary", returns = RT_ANY),
                        f(name = "loadedLanguage", returns = RT_ANY),
                        f(
                                name = "valueForInfoDictionaryKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = RT_ANY
                        ),
                        f(name = "identifier", returns = RT_ANY),
                        f(name = "hasSpritedImages", returns = RT_BOOL),
                        f(name = "environments", returns = RT_ANY),
                        f(
                                name = "mostEligibleEnvironment",
                                parameters = listOf(p("evironments", "array")),
                                returns = RT_ANY
                        ),
                        f(name = "isLoading", returns = RT_BOOL),
                        f(name = "isLoaded", returns = RT_BOOL),
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
                        f(name = "bundlePath", returns = RT_ANY),
                        f(
                                name = "pathForResource",
                                parameters = listOf(
                                        p("aResourceName", "string"),
                                        p("aType", "string"),
                                        p("aSubDirectory", "string"),
                                        p("localizationName", "string")
                                ),
                                returns = RT_CFURL
                        )
                )
        ),
        c(
                className = "CFData",
                properties = listOf(
                        p("isa", "CPData")
                ),
                functions = listOf(
                        f(name = "propertyList", returns = rt("CFPropertyList")),
                        f(name = "JSONObject", returns = rt("Object")),
                        f(name = "rawString", returns = RT_STRING),
                        f(name = "bytes", returns = rt("byte[]")),
                        f(name = "base64", returns = RT_STRING)
                ),
                staticFunctions = listOf(
                        f(
                                name = "decodeBase64ToArray",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = rt("byte[]")
                        ),
                        f(
                                name = "decodeBase64ToString",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = RT_STRING
                        ),
                        f(
                                name = "decodeBase64ToUtf16String",
                                parameters = listOf(
                                        p("input", "string"),
                                        p("strip", "BOOL")
                                ),
                                returns = RT_STRING
                        ),
                        f(
                                name = "encodeBase64Array",
                                parameters = listOf(p("input", "string[]")), // @todo check if this is correct
                                returns = RT_STRING
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
                                        p("anObject", "Object"))
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
                                returns = RT_STRING
                        ),
                        f(
                                name = "stringToBytes",
                                parameters = listOf(p("input", "string")),
                                returns = rt("byte[]")
                        ),
                        f(
                                name = "encodeBase64String",
                                parameters = listOf(p("input", "string")),
                                returns = RT_STRING
                        ),
                        f(
                                name = "bytesToUtf16String",
                                parameters = listOf(p("bytes", "byte[]")),
                                returns = RT_STRING
                        ),
                        f(
                                name = "encodeBase64Utf16String",
                                parameters = listOf(p("input", "string")),
                                returns = RT_STRING
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
                        f(name = "copy", returns = rt("CFDictionary")),
                        f(name = "mutableCopy", returns = rt("CFMutableDictionary")),
                        f(
                                name = "containsKey",
                                parameters = listOf(p("aKey", "string")),
                                returns = RT_BOOL
                        ),
                        f(
                                name = "containsValue",
                                parameters = listOf(p("anObject", "id")),
                                returns = RT_BOOL
                        ),
                        f(name = "count", parameters = listOf(), returns = RT_NUMBER),
                        f(name = "countOfKey", parameters = listOf(p("aKey", "string")), returns = RT_NUMBER),
                        f(name = "countOfValue", parameters = listOf(p("anObject", "id")), returns = RT_NUMBER),
                        f(name = "keys", returns = rt("string[]")),
                        f(name = "valueForKey", parameters = listOf(p("aKey", "string")), returns = rt("?")),
                        f(name = "toString", returns = RT_STRING)
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
                                        p("anObject", "Object")
                                )
                        ),
                        f("removeValueForKey", parameters = listOf(p("aKey", "string"))),
                        f("removeAllValues"),
                        f(
                                name = "replaceValueForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("anObject", "Object")
                                )
                        ),
                        f(
                                name = "setValueForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("anObject", "Object")
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
                        f(name = "domain", returns = RT_STRING),
                        f(name = "code", returns = RT_NUMBER),
                        f(name = "description", returns = RT_STRING),
                        f(name = "failureReason", returns = RT_STRING),
                        f(name = "recoverySuggestion", returns = RT_ANY),
                        f(name = "userInfo", returns = rt("CFDictionary"))
                )
        ),
        c(
                className = "___CHECK_ME___",
                functions = listOf(
                        f(name = "status", returns = RT_NUMBER),
                        f(name = "statusText", returns = RT_STRING),
                        f(name = "readyState", returns = RT_NUMBER),
                        f(name = "success", returns = RT_BOOL),
                        f(name = "responseText", returns = RT_ANY),
                        f(
                                name = "setRequestHeader",
                                parameters = listOf(
                                        p("aHeader", "string"),
                                        p("aValue", "Object")
                                )
                        ),
                        f(name = "responseXML", returns = rt("XMLNode")),
                        f(name = "responsePropertyList", returns = rt("CFPropertyList")),
                        f(name = "setTimeout", parameters = listOf(p("aTimeout", "int"))),
                        f(name = "getTimeout", parameters = listOf(p("aTimeout", "int")), returns = RT_NUMBER),
                        f(name = "getAllResponseHeaders", returns = RT_ANY),
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
                        f(name = "send", parameters = listOf(p("aBody", "Object"))),
                        f(name = "abort", returns = RT_ANY),
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
                        f(name = "withCredentials", returns = RT_BOOL),
                        f(name = "isTimeoutRequest", returns = RT_BOOL)
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
                                returns = rt("CFPropertyList")
                        ),
                        f(
                                name = "propertyListFromString",
                                parameters = listOf(
                                        p("aString", "string"),
                                        p("aFormat", "Format")
                                ),
                                returns = rt("CFPropertyList")

                        ),
                        f(
                                name = "propertyListFromXML",
                                parameters = listOf(
                                        p("aStringOrXMLNode", "string|XMLNode")),
                                returns = rt("CFPropertyList")
                        ),
                        f(name = "sniffedFormatOfString", parameters = listOf(p("aStrign", "string")), returns = rt("int")),
                        f(
                                name = "dataFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFormat", "Format")
                                ),
                                returns = rt("CFMutableData")
                        ),
                        f(
                                name = "stringFromPropertyList",
                                parameters = listOf(
                                        p("aPropertyList", "CFPropertyList"),
                                        p("aFormat", "Format")
                                ),
                                returns = RT_STRING
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
                        f("UID", returns = RT_NUMBER),
                        f("mappedURL", returns = RT_CFURL),
                        f(
                                name = "setMappedURLForURL",
                                parameters = listOf(
                                        p("fromURL", "CFURL"),
                                        p("toURL", "CFURL")
                                )
                        ),
                        f(name = "schemeAndAuthority", returns = RT_STRING),
                        f(name = "absoluteString", returns = RT_STRING),
                        f(name = "toString", returns = RT_STRING),
                        f(name = "absoluteURL", returns = RT_CFURL),
                        f(name = "standardizedURL", returns = RT_CFURL),
                        f(name = "string", returns = RT_STRING),
                        f(name = "authority", returns = RT_ANY),
                        f(name = "hasDirectoryPath", returns = RT_BOOL),
                        f(name = "hostName", returns = RT_ANY),
                        f(name = "fragment", returns = RT_ANY),
                        f(name = "lastPathComponent", returns = RT_ANY),
                        f(name = "createCopyDeletingLastPathComponent", returns = RT_CFURL),
                        f(name = "pathComponents", returns = RT_ANY),
                        f(name = "pathExtension", returns = RT_STRING),
                        f(name = "queryString", returns = RT_STRING),
                        f(name = "scheme", returns = RT_STRING),
                        f(name = "user", returns = RT_STRING),
                        f(name = "password", returns = RT_STRING),
                        f(name = "portNumber", returns = RT_NUMBER),
                        f(name = "domain", returns = RT_STRING),
                        f(name = "baseURL", returns = RT_ANY),
                        f(name = "asDirectoryPathURL", returns = RT_CFURL),
                        f(name = "resourcePropertyForKey", parameters = listOf(p("aKey", "string")), returns = RT_ANY),
                        f(
                                name = "setResourcePropertyForKey",
                                parameters = listOf(
                                        p("aKey", "string"),
                                        p("aValue", "id")
                                )
                        ),
                        f(name = "staticResourceData", returns = rt("CFMutableData"))
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
                        f(name = "toString", returns = RT_STRING)
                )
        ),
        c(
                className = "objj_protocol",
                constructor = ctor(listOf(p("aName", "string"))),
                properties = listOf(
                        p("name", "string"),
                        p("instance_methods", "Object"),
                        p("class_methods", "Object")
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
                        f("composedPath", returns = rt("EventTarget[]")),
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
                                returns = RT_BOOL
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
                        f("toString", returns = RT_STRING),
                        f("hasThemeState", parameters = listOf(p("aState", "?")), returns = RT_BOOL),
                        f("isSubsetOf", parameters = listOf(p("aState", "?")), returns = RT_BOOL),
                        f("without", parameters = listOf(p("aState", "?")), returns = rt("ThemeState")),
                        f("and", parameters = listOf(p("aState", "?")), returns = rt("ThemeState"))
                )
        ),
        c(
                className = "CPString",
                extends = listOf("string")
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
                        f("packageType", returns = RT_STRING),
                        f("infoPlist", returns = RT_ANY),
                        f("themeDescriptors", returns = RT_ANY),
                        f("setThemeDescriptors", parameters = listOf(p("themeDescriptors", "?[]|FileList[]"))),
                        f("defineTasks", parameters = listOf(p("args", "?", varArgs = true))),
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
                        f("updateFunction", returns = RT_ANY),
                        f("identifier", returns = RT_ANY),
                        f("description", returns = RT_STRING),
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
                        f("description", returns = RT_STRING),
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
                                returns = RT_BOOL
                        ),
                        f("keyFrames", returns = RT_STRING),
                        f("appendKeyFramesRule"),
                        f("createKeyFramesStyleElement"),
                        f("endEventListener", returns = rt("AnimationEndListener")),
                        f("completionFunctionForAnimationName", parameters = listOf(p("aName", "string")), returns = rt("Function")),
                        f("addAnimationEndEventListener"),
                        f("setTargetStyleProperties"),
                        f("buildDOMElements"),
                        f("start", returns = RT_BOOL)

                )
        ),
        c(
                className = "Date",
                properties = listOf(
                        p("isa", "CPDate")
                ),
                staticFunctions = listOf(
                        f("parseISO8601", listOf(p("aData", "Date")), returns = RT_NUMBER)
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
                className = "Document",
                functions = listOf(
                        f(
                                name = "createElement",
                                parameters = listOf(
                                        p("tagName", "string")
                                ),
                                returns = rt("DOMElement", nullable = true)
                        )
                )
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
                                returns = rt("string[]|null"),
                                comment = "Executes a search on a string using a regular expression pattern, and returns an array containing the results of that search."
                        ),
                        f(
                                name = "test",
                                parameters = listOf(
                                        p("string", "string", comment = "String on which to perform the search")),
                                returns = RT_BOOL,
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
        c(
                className = "CGRect",
                properties = listOf(
                        p("x", "number"),
                        p("y", "number"),
                        p("width", "number"),
                        p("height", "number")
                ),
                isStruct = true
        ),
        c(
                className = "CGPoint",
                properties = listOf(
                        p("x", "number"),
                        p("y", "number")
                ),
                isStruct = true
        ),
        c(
                className = "CGSize",
                properties = listOf(
                        p("width", "number"),
                        p("height", "number")
                ),
                isStruct = true
        ),
        Window,
        JsClassHTMLElement,
        JsClassGlobalEventHandlers,
        JsClassCSSStyleDeclaration,
        JsClassAnimatable,
        JsClassChildNode,
        JsClassElementCSSInlineStyle,
        JsClassEventTarget,
        JsElementClass,
        JsClassHTMLOrSVGElement,
        JsClassElementContentEditable,
        JsClassDocumentAndElementEventHandlers,
        JsClassNonDocumentTypeChildNode,
        JsClassParentNode,
        JsClassNode

)


val globalJsClassNames = globalJSClasses.filter { !it.isStruct }.map { it.className }
