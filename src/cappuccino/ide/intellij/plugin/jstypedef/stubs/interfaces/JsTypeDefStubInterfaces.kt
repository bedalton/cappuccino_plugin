package cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefClassName
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeListType.JsTypeDefTypeListArrayType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeListType.JsTypeDefTypeListMapType
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import sun.rmi.rmic.iiop.InterfaceType

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
    val returnType: JsTypeDefTypesList
    val global:Boolean
    val static:Boolean
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + functionName
}

fun JsTypeDefProperty.toStubParameter() : JsTypeDefNamedProperty {
    return JsTypeDefNamedProperty(
            name = propertyName.text,
            types = JsTypeDefTypesList(propertyTypes.map { type -> type.text }.toSet(), nullable = isNullable)
    )
}

/**
 * Property stub interface
 */
interface JsTypeDefPropertyStub : StubElement<JsTypeDefPropertyImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val propertyName:String
    val types:JsTypeDefTypesList
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
    val interfaceName:String
    val superTypes:List<JsTypeDefClassName>
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + interfaceName
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
    fun getTypesForKey(key:String) : JsTypeDefTypesList
}

/**
 * Type map stub key/value holder
 */
data class JsTypeDefTypeMapEntry (val key:String, val types:JsTypeDefTypesList)

/**
 * Type list holder for stubs
 */
data class JsTypeDefTypesList(val types:List<JsTypeDefTypeListType> = listOf(), val nullable:Boolean = true) : Iterable<String> {

    val mapTypes:List<JsTypeDefTypeListMapType> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListMapType }
    }

    val arrayTypes: List<JsTypeDefTypeListArrayType> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListArrayType }
    }

    val keyOfTypes: List<JsTypeDefTypeListType.JsTypeDefTypeListKeyOfType> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListType.JsTypeDefTypeListKeyOfType }
    }

    val valueOfKeyTypes: List<JsTypeDefTypeListType.JsTypeDefTypeListValueOfKeyType> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListType.JsTypeDefTypeListValueOfKeyType }
    }

    val basicTypes: List<JsTypeDefTypeListType.JsTypeDefTypeListBasicType> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListType.JsTypeDefTypeListBasicType }
    }

    val interfaceTypes: List<JsTypeDefTypeListType.JsTypeDefTypeListInterfaceBody> get() {
        return types.mapNotNull { it as? JsTypeDefTypeListType.JsTypeDefTypeListInterfaceBody }
    }

    override fun iterator(): Iterator<String> {
        return types.map { it.typeName }.iterator()
    }
}


data class JsTypeDefNamedProperty(val name:String, val types: JsTypeDefTypesList, val readonly:Boolean = false, val static:Boolean = false) {
    val nullable:Boolean get() = types.nullable
}

data class JsTypeDefFunctionType(val name:String? = null, val parameters:List<JsTypeDefNamedProperty>, val returnType:JsTypeDefTypesList?, val static:Boolean = false)


sealed class JsTypeDefTypeListType(open val typeName:String) {
    data class JsTypeDefTypeListArrayType(val types:List<JsTypeDefTypeListType>, val dimensions:Int = 1) : JsTypeDefTypeListType("Array")
    data class JsTypeDefTypeListKeyOfType(val genericKey:String, val mapName:String) : JsTypeDefTypeListType("KeyOf:$mapName")
    data class JsTypeDefTypeListValueOfKeyType(val genericKey:String, val mapName:String) : JsTypeDefTypeListType("ValueOf:$mapName")
    data class JsTypeDefTypeListMapType(val keyTypes:List<JsTypeDefTypeListType>, val valueTypes:List<JsTypeDefTypeListType>) : JsTypeDefTypeListType("Map")
    data class JsTypeDefTypeListBasicType(override val typeName:String) : JsTypeDefTypeListType(typeName)
    data class JsTypeDefTypeListInterfaceBody(val properties:List<JsTypeDefNamedProperty>, val functions:List<JsTypeDefFunctionType>) : JsTypeDefTypeListType("Object")
    data class JsTypeDefTypeListAnonymousFunctionType(val parameters:List<JsTypeDefNamedProperty>, val returnType: JsTypeDefTypesList?) : JsTypeDefTypeListType("Function")
}

enum class TypeListType(val id:Int) {
    BASIC(0),
    ARRAY(1),
    KEYOF(2),
    VALUEOF(3),
    MAP(4),
    INTERFACE_BODY(5);

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