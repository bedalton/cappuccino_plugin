package cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces

import cappuccino.ide.intellij.plugin.contributor.EMPTY_TYPES_LIST
import cappuccino.ide.intellij.plugin.contributor.JsProperty
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefClassName
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement

interface JsTypeDefFileStub : PsiFileStub<JsTypeDefFile> {
    val fileName:String
}

/**
 * Keys list stub interface
 */
interface JsTypeDefKeysListStub : StubElement<JsTypeDefKeyListImpl> {
    val fileName:String
    val listName:String
    val values:List<String>
}

/**
 * Function stub interface
 */
interface JsTypeDefFunctionStub : StubElement<JsTypeDefFunctionImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val functionName:String
    val parameters:List<JsTypeDefNamedProperty>
    val returnType: JsTypesList
    val global:Boolean
    val static:Boolean
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + functionName
}

fun JsTypeDefProperty.toStubParameter() : JsTypeDefNamedProperty {
    return JsTypeDefNamedProperty(
            name = propertyName.text,
            types = JsTypesList(propertyTypes.toJsTypeDefTypeListTypes(), nullable = isNullable)
    )
}

/**
 * Property stub interface
 */
interface JsTypeDefPropertyStub : StubElement<JsTypeDefPropertyImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val propertyName:String
    val types:JsTypesList
    val nullable:Boolean
    val static:Boolean
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + propertyName
}

/**
 * Stub type for modules
 */
interface JsTypeDefModuleStub : StubElement<JsTypeDefModuleImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val moduleName:String
    val fullyNamespacedName:String
        get() = namespaceComponents.joinToString (".")
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + moduleName
}

/**
 * Stub type for module name
 */
interface JsTypeDefModuleNameStub : StubElement<JsTypeDefModuleNameImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val moduleName:String
    val fullyNamespacedName:String
        get() = namespaceComponents.joinToString (".")
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + moduleName
}


interface JsTypeDefInterfaceStub : StubElement<JsTypeDefInterfaceElementImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val className:String
    val superTypes:List<JsTypeDefClassName>
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + className
}

interface JsTypeDefNamespacedComponent {
    val enclosingNamespace:String
    val enclosingNamespaceComponents:List<String> get() = enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX)
    val namespaceComponents:List<String>
}

val JsTypeDefNamespacedComponent.fullyNamespacedName : String
    get() = namespaceComponents.joinToString(".")


interface JsTypeDefTypeMapStub : StubElement<JsTypeDefTypeMapImpl> {
    val fileName:String
    val mapName:String
    val values:List<JsTypeDefTypeMapEntry>
    fun getTypesForKey(key:String) : JsTypesList
}

interface JsTypeDefVariableDeclarationStub : StubElement<JsTypeDefVariableDeclarationImpl>, JsTypeDefNamespacedComponent, JsProperty {
    val fileName:String
    val variableName:String
    val types:JsTypesList
    val static:Boolean
}

/**
 * Type map stub key/value holder
 */
data class JsTypeDefTypeMapEntry (val key:String, val types:JsTypesList)

/**
 * Type list holder for stubs
 */
class JsTypesList(val types:List<JsTypeListType> = listOf(), val nullable:Boolean = true) : Iterable<String>{

    val mapTypes:List<JsTypeListMapType> get() {
        return types.mapNotNull { it as? JsTypeListMapType }
    }

    val arrayTypes: JsTypeListArrayType by lazy {
        val all = types.mapNotNull { it as? JsTypeListArrayType }
        val typesOut = all.flatMap {
            it.types
        }
        val maxDimensions = all.map { it.dimensions }.max().orElse(1)
        JsTypeListArrayType(typesOut, maxDimensions)
    }

    val keyOfTypes: List<JsTypeListKeyOfType> get() {
        return types.mapNotNull { it as? JsTypeListKeyOfType }
    }

    val valueOfKeyTypes: List<JsTypeListValueOfKeyType> get() {
        return types.mapNotNull { it as? JsTypeListValueOfKeyType }
    }

    val basicTypes: List<JsTypeListBasicType> get() {
        return types.mapNotNull { it as? JsTypeListBasicType }
    }

    val interfaceTypes: List<JsTypeListInterfaceBody> get() {
        return types.mapNotNull { it as? JsTypeListInterfaceBody }
    }

    val anonymousFunctionTypes: List<JsTypeListAnonymousFunctionType> get() {
        return types.mapNotNull { it as? JsTypeListAnonymousFunctionType }
    }

    override fun iterator(): Iterator<String> {
        return types.map { it.typeName }.iterator()
    }

    override fun toString(): String {
        return types.joinToString("|") { it.typeName }
    }
}


data class JsTypeDefNamedProperty(
        val name:String,
        override val types: JsTypesList,
        override val readonly:Boolean = false,
        val static:Boolean = false,
        override val comment:String? = null,
        override val default: String? = null
) : JsTypeDefPropertyBase {
    override val nullable:Boolean get() = types.nullable
}

interface JsTypeDefPropertyBase {
    val types:JsTypesList
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
}

data class JsFunction(
        val name:String? = null,
        val parameters:List<JsTypeDefNamedProperty>,
        val returnType:JsTypesList? = EMPTY_TYPES_LIST,
        val static:Boolean = false,
        val comment: String? = null
)

sealed class JsTypeListType(open val typeName:String) {
    data class JsTypeListArrayType(val types:List<JsTypeListType>, val dimensions:Int = 1) : JsTypeListType("Array")
    data class JsTypeListKeyOfType(val genericKey:String, val mapName:String) : JsTypeListType("KeyOf:$mapName")
    data class JsTypeListValueOfKeyType(val genericKey:String, val mapName:String) : JsTypeListType("ValueOf:$mapName")
    data class JsTypeListMapType(val keyTypes:List<JsTypeListType>, val valueTypes:List<JsTypeListType>) : JsTypeListType("Map")
    data class JsTypeListBasicType(override val typeName:String) : JsTypeListType(typeName)
    data class JsTypeListInterfaceBody(val properties:List<JsTypeDefNamedProperty>, val functions:List<JsFunction>) : JsTypeListType("Object")
    data class JsTypeListAnonymousFunctionType(val parameters:List<JsTypeDefNamedProperty>, val returnType: JsTypesList?) : JsTypeListType("Function")
}

enum class TypeListType(val id:Int) {
    BASIC(0),
    ARRAY(1),
    KEYOF(2),
    VALUEOF(3),
    MAP(4),
    INTERFACE_BODY(5),
    ANONYMOUS_FUNCTION(6);

    companion object {
        fun forKey(key:Int):TypeListType {
            return when (key) {
                BASIC.id -> BASIC
                ARRAY.id -> ARRAY
                KEYOF.id -> KEYOF
                VALUEOF.id -> VALUEOF
                MAP.id -> MAP
                INTERFACE_BODY.id -> INTERFACE_BODY
                else -> throw Exception("Invalid class type stub value encountered")
            }
        }
    }
}