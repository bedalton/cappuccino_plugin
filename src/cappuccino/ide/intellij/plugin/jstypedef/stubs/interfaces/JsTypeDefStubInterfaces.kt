package cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefClassName
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
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


interface JsTypeDefInterfaceStub : StubElement<JsTypeDefModuleImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val interfaceName:String
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
data class JsTypeDefTypesList(val types:Set<JsTypeDefClassName>, val nullable:Boolean) : Iterable<String> {

    override fun iterator(): Iterator<String> {
        return types.iterator()
    }
}


data class JsTypeDefNamedProperty(val name:String, val types: JsTypeDefTypesList, val readonly:Boolean = false, val static:Boolean = false) {
    val nullable:Boolean get() = types.nullable
}

data class JsTypeDefFunctionType(val name:String? = null, val parameters:List<JsTypeDefNamedProperty>, val returnType:JsTypeDefTypesList, val static:Boolean = false)

sealed class JsTypeDefTypeListType {
    data class JsTypeDefTypeListArrayType(val types:List<JsTypeDefTypeListType>, val dimensions:Int = 1) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListKeyOfType(val genericKey:String, val list:String) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListMapReturn(val genericKey:String, val list:String) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListMapType(val keyTypes:List<JsTypeDefTypeListType>, val valueTypes:List<JsTypeDefTypeListType>) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListGenericType(val baseType:String, val genericParameters:List<JsTypeDefTypeListType>) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListBasicType(val type:String) : JsTypeDefTypeListType()
    data class JsTypeDefTypeListInterfaceBody(val properties:List<JsTypeDefNamedProperty>, val functions:List<JsTypeDefFunctionType>) : JsTypeDefTypeListType()
}

enum class TypeListType(val id:Int) {
    BASIC(0),
    ARRAY(1),
    KEYOF(2),
    MAP_RETURN(3),
    MAP(4),
    GENERIC(5),
    INTERFACE_BODY(6)

}