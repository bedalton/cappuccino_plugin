package cappuccino.ide.intellij.plugin.stubs.stucts

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.toJsTypeList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope.INSTANCE
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope.SELECTOR_LITERAL
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNode
import com.intellij.openapi.util.Key
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct.Companion.Getter as Getter

val startsWithVowelRegex = "^[aAeEiIoOuU]".toRegex()


// ============================== //
// ========== Structs =========== //
// ============================== //
data class ObjJMethodStruct(val selectors:List<ObjJSelectorStruct>, val returnType:InferenceResult?, val methodScope: MethodScope, val containingClassName:String?) {
    val selectorString:String by lazy {
        if (selectors.size == 1) {
            val selector = selectors[0]
            return@lazy if (selector.hasColon) "${selector.selector}:" else selector.selector
        }
        selectors.joinToString(":") { it.selector } + ":"
    }
    val selectorStringWithColon:String by lazy {
        selectors.joinToString(":") { it.selector } + ":"
    }
}

data class ObjJSelectorStruct(val selector:String, val variableType:String?, val variableName:String?, val hasColon:Boolean = variableName != null, val containerName: String, val isContainerAClass:Boolean = true) {
    val selectorString:String by lazy {
        if (this.hasColon) "$selector:" else selector
    }

    val tail:String? by lazy {
        if (!hasColon && variableType == null) {
            return@lazy null
        }
        if (variableType.isNullOrBlank())
            return@lazy ":"
        val builder = StringBuilder(":")
        builder.append('(').append(variableType) .append(")")
        if (variableName != null)
            builder.append(variableName)
        builder.toString()
    }

    override fun toString(): String {
        return selectorString
    }
    companion object {
        @Suppress("FunctionName")
        fun Getter(selectorIn:String, containingClassName: String) = ObjJSelectorStruct(
                selector = selectorIn,
                variableType = null,
                variableName = null,
                hasColon = false,
                containerName = containingClassName

        )
    }
}

private val METHOD_STRUCT_KEY = Key<ObjJMethodStruct>("objj.userdata.structs.method")

// ============================== //
// ===== Extension Methods ====== //
// ============================== //

fun ObjJMethodHeader.toMethodStruct(tag:Long) : ObjJMethodStruct {
    val cached = getUserData(METHOD_STRUCT_KEY)
    if (cached != null)
        return cached
    val selectors = methodDeclarationSelectorList.map {
        it.toSelectorStruct()
    }
    val returnTypeStrings = getReturnTypes(tag).toJsTypeList()
    val returnType = if (returnTypeStrings.isNotEmpty())
        InferenceResult(types = returnTypeStrings)
    else
        null
    val out = ObjJMethodStruct(selectors = selectors, returnType = returnType, methodScope = methodScope, containingClassName = containingClassName)
    putUserData(METHOD_STRUCT_KEY, out)
    return out
}

fun ObjJInstanceVariableDeclaration.getMethodStructs() : List<ObjJMethodStruct> {
    return stub?.accessorStructs ?: accessorPropertyList.flatMap {
        it.getMethodStructs()
    }
}

fun ObjJAccessorProperty.getMethodStructs() : List<ObjJMethodStruct> {
    val methods = mutableListOf<ObjJMethodStruct>()
    val containingClassName = containingClassName
    var getter = getter
    val variableType = getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.formalVariableType?.variableType ?: return emptyList()
    if (getter != null) {
        if (getter.endsWith(":"))
            getter = getter.substring(0, getter.lastIndex)
        methods.add(
                ObjJMethodStruct(
                        selectors = listOf(Getter(getter, containingClassName)),
                        returnType = InferenceResult(setOf(variableType).toJsTypeList()),
                        methodScope = INSTANCE,
                        containingClassName = containingClassName
                )
        )
    }
    var setter = setter
    if (setter != null) {
        val prefix = if (startsWithVowelRegex.containsMatchIn(variableType)) "an" else "a"
        val variableName = prefix + variableType.capitalize()
        methods.add(
                ObjJMethodStruct(
                        selectors = listOf(ObjJSelectorStruct(
                                selector = setter,
                                variableType = variableType,
                                variableName = variableName,
                                hasColon = true,
                                containerName = containingClassName,
                                isContainerAClass = true
                        )),
                        returnType = null,
                        methodScope = INSTANCE,
                        containingClassName = containingClassName
                )
        )
    }
    return methods
}

fun ObjJSelectorLiteral.toMethodStruct() : ObjJMethodStruct {
    return ObjJMethodStruct(
            selectors = selectorStructs,
            returnType = null,
            methodScope = SELECTOR_LITERAL,
            containingClassName = null
    )
}

fun ObjJSelector.toSelectorStruct(containingClassName: String) : ObjJSelectorStruct {
    val selectorString = getSelectorString(false)
    val parent = parent
    if (parent is ObjJSelectorLiteral) {

    }
    if (parent is ObjJMethodDeclarationSelector) {
        return parent.toSelectorStruct()
    }
    LOGGER.warning("Selector struct fetched outside fo expected context. Parent is: <${parent.elementType}>")
    return ObjJSelectorStruct(
            selector = selectorString,
            variableType = null,
            variableName = null,
            hasColon = hasColon,
            containerName = containingClassName
    )
}

private val ObjJSelector.hasColon:Boolean
    get() = getNextNonEmptyNode(true)?.elementType == ObjJTypes.ObjJ_COLON

fun ObjJMethodDeclarationSelector.toSelectorStruct() : ObjJSelectorStruct {
    val selector = selector?.getSelectorString(false) ?: EMPTY_SELECTOR
    val variableType = formalVariableType?.variableType
    val variableName = variableName?.text
    return ObjJSelectorStruct(
            containerName = containingClassName,
            selector = selector,
            variableType = variableType,
            variableName = variableName
    )
}


// ============================== //
// ======= Stub Streams ========= //
// ============================== //


fun StubInputStream.readMethodStructList() : List<ObjJMethodStruct> {
    val numberOfStructs = readInt()
    return (0 until numberOfStructs).map {
        readMethodStruct()
    }
}

fun StubOutputStream.writeMethodStructList(structs:List<ObjJMethodStruct>) {
    writeInt(structs.size)
    for (struct in structs) {
        writeMethodStruct(struct)
    }
}


fun StubInputStream.readMethodStruct() : ObjJMethodStruct {
    val selectors = readSelectorStructList()
    val returnType = readInferenceResult()
    val containingClassName = readNameString()
    val methodScope = MethodScope.getScope(readNameString() ?: "")
    return ObjJMethodStruct(
            containingClassName = containingClassName,
            selectors = selectors,
            returnType = returnType,
            methodScope = methodScope
    )
}

fun StubOutputStream.writeMethodStruct(method:ObjJMethodStruct) {
    writeSelectorStructList(method.selectors)
    writeInferenceResult(method.returnType)
    writeName(method.containingClassName)
    writeName(method.methodScope.scopeMarker)
}




fun StubInputStream.readSelectorStructList() : List<ObjJSelectorStruct> {
    val numberOfSelectors = readInt()
    return (0 until numberOfSelectors).map {
        readSelectorStruct()
    }
}


fun StubOutputStream.writeSelectorStructList(selectors:List<ObjJSelectorStruct>) {
    writeInt(selectors.size)
    for(selector in selectors) {
        writeSelectorStruct(selector)
    }
}

fun StubInputStream.readSelectorStruct() : ObjJSelectorStruct {
    val containerName = readNameString() ?: ObjJClassType.UNDEF_CLASS_NAME
    val isContainerAClass = readBoolean()
    val selector = readNameString() ?: EMPTY_SELECTOR
    val type = readNameString()
    val variableName = readNameString()
    val hasColon = readBoolean()
    return ObjJSelectorStruct(
            containerName = containerName,
            selector = selector,
            variableType = type,
            variableName = variableName,
            hasColon = hasColon
    )
}

fun StubOutputStream.writeSelectorStruct(selector:ObjJSelectorStruct) {
    writeName(selector.containerName)
    writeBoolean(selector.isContainerAClass)
    writeName(selector.selector)
    writeName(selector.variableType)
    writeName(selector.variableName)
    writeBoolean(selector.hasColon)
}