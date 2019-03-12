@file:Suppress("PropertyName", "ObjectPropertyName")

package cappuccino.ide.intellij.plugin.contributor.javascript

import cappuccino.ide.intellij.plugin.utils.orTrue
import cappuccino.ide.intellij.plugin.utils.splitOnUppercase
import kotlin.RuntimeException

/**
 * A holder for all declared javascript class definitions
 */
object JsClasses {
    private val _classes:MutableMap<String, JsClass> = mutableMapOf()

    fun add(jsClass:JsClass) {
        if (_classes.containsKey(jsClass.className)) {
            throw RuntimeException("Class with name ${jsClass.className} has already been declared")
        }
        _classes[jsClass.className] = jsClass
    }

    val classes:List<JsClass> get() = _classes.values.toList()

    val classNames:List<String> get() = _classes.keys.toList()

    fun find(className:String) : JsClass? {
        return if (_classes.containsKey(className)) {
            _classes[className]
        } else {
            AnyClass
        }
    }

    val window:JsClass? get() = find("Window")
}

/**
 * An interface used for querying a list of JsItems
 */
interface JsNamedEntity {
    val entityName:String
}

/**
 * Represents a statically declared javascript class
 * Used in a simple resolving mechanism for suggestions and completion
 */
interface JsClass : JsNamedEntity {
    val className:String
    val propertyNames:Set<String>
    val properties:List<JsProperty>
    val functionNames:Set<String>
    val functions:List<JsFunction>
    fun property(propertyName:String) : JsProperty?
    fun function(functionName:String) : JsFunction?
}

/**
 * Static placeholder class when no class can be found or determined
 */
val AnyClass : JsClass = JsClassImpl("Any?", mutableMapOf(), mutableMapOf())

/**
 * A holder for all functions declared so far
 */
object AllFunction
{
    private val _functions:MutableSet<JsFunction> = mutableSetOf()

    val functions:List<JsFunction> get() {
        return _functions.toList()
    }

    internal fun add(function:JsFunction) {
        _functions.add(function)
    }
}

/**
 * A holder for all properties declared so far
 */
object AllProperties
{
    private val _properties:MutableSet<JsProperty> = mutableSetOf()

    internal fun add(property:JsProperty) {
        _properties.add(property)
    }

    val properties : List<JsProperty> get () {
        return _properties.toList()
    }
}

/**
 * JsClassBuilder
 */
class JsClassesBuilder
{
    val classes:MutableMap<String, JsClass> = mutableMapOf()
}

fun JsClasses.jsClass(className:String, classInit:JsClassBuilder.()->Unit) {
    val jsClass = JsClassBuilder(className).apply(classInit).build()
    add(jsClass)
}

// ============================== //
// =========== JsClass ========== //
// ============================== //

val JsClass.isDynamic:Boolean get () = this === AnyClass


data class JsClassImpl(override val className:String, internal val _properties:Map<String, JsProperty>, internal val _functions:Map<String, JsFunction>) : JsClass {
    override val propertyNames:Set<String> get() {
        return _properties.keys
    }

    override val properties:List<JsProperty>
        get() = _properties.values.toList()

    override val functionNames:Set<String>
        get() = _functions.keys

    override val functions:List<JsFunction>
        get() = _functions.values.toList()

    override val entityName: String
        get() = className

    override fun property(propertyName:String) : JsProperty? {
        return  if (_properties.containsKey(propertyName)) {
            _properties[propertyName]
        } else {
            null
        }
    }
    override fun function(functionName:String) : JsFunction? {
        return  if (_functions.containsKey(functionName)) {
            _functions[functionName]
        } else {
            null
        }
    }
}

class JsClassBuilder(internal val className:String)
{
    internal val _properties:MutableMap<String, JsProperty> = mutableMapOf()
    internal val _functions:MutableMap<String, JsFunction> = mutableMapOf()

    fun build() : JsClass {
        return JsClassImpl(className, _properties, _functions)
    }
}
fun JsClassesBuilder.jsClass (className:String, init: JsClassBuilder.()->Unit) {
    val jsClass = JsClassBuilder(className).apply (init).build()
    if (classes.containsKey(className)) {
        throw RuntimeException("Class \"$className\" has already been declared")
    }
    classes[className] = jsClass
}

fun JsClassBuilder.property (propertyName:String, init:JsPropertyBuilder.()->Unit) {
    if (_properties.containsKey(propertyName)) {
        throw RuntimeException("JsProperty with propertyName $propertyName has already been added to class ${this.className}")
    }
    _properties[propertyName] = JsPropertyBuilder(propertyName).apply(init).build()
}


// ============================== //
// ========= JsProperty ========= //
// ============================== //

/**
 * Represents a named javascript property
 */
interface JsProperty : JsNamedEntity {
    val propertyName: String
    val propertyType:JsPropertyType?
}


/**
 * Simple data class implementation class of JsProperty
 */
data class JsPropertyImpl(override val propertyName: String, override val propertyType:JsPropertyType?, val nullable:Boolean?) : JsProperty {

    override val entityName: String
        get() = propertyName
}

/**
 * Simple builder class for declaring a javascript property
 */
class JsPropertyBuilder (private val propertyName:String) {
    internal var type:JsPropertyType? = null
    internal var nullable: Boolean? = null

    fun build(): JsProperty {
        if (type == null) {
            type = JsPropertyTypeImpl()
        }
        val property = JsPropertyImpl(propertyName, type, nullable)
        AllProperties.add(property)
        return property
    }
}

// ============================== //
// ======= JsPropertyType ======= //
// ============================== //

/**
 * Represents a javascript's property type
 * Property types can be overloaded and marked as nullable
 */
interface JsPropertyType {

    /**
     * Gets all possible variable types as strings
     */
    val typesAsString:List<String>

    /**
     * Whether this property in the javascript environment can be null
     */
    val nullable:Boolean?
    /**
     * Gets property types as their respective javascript class objects
     */
    val typesAsClasses:List<JsClass>

    /**
     * Determines whether this property type should be considered as an array
     */
    val isArray:Boolean
}

/**
 * JsProperty builder
 */
data class JsPropertyTypeBuilder (internal val types:MutableList<String> = mutableListOf(), internal var nullable:Boolean? = null)

data class JsPropertyTypeImpl(override val typesAsString:List<String> = listOf(), override val nullable:Boolean?= null, override val isArray:Boolean = false) : JsPropertyType {
    override val typesAsClasses:MutableList<JsClass> get() {
        val out = mutableListOf<JsClass>()
        for (className in typesAsString) {
            val jsClass = JsClasses.find(className) ?: continue
            out.add(jsClass)
        }
        return out
    }
}

fun JsPropertyTypeBuilder.types(init:MutableList<String>.() -> Unit) {
    val types:MutableList<String> = mutableListOf()
    types.apply(init)
    this.types.addAll(types)
}

fun JsPropertyTypeBuilder.type(type:String) {
    types.add(type)
}

fun JsPropertyTypeBuilder.nullable(nullable:Boolean) {
    this.nullable = nullable
}


// ============================== //
// ======== JsFunction ========== //
// ============================== //

interface JsFunction : JsNamedEntity {
    val functionName:String
    val params:List<JsProperty>
    val returnType:JsPropertyType?
    val numberOfParams:Int
    val hasRequiredParams:Boolean
    val possibleReturnTypes:List<JsClass>
}

data class JsFunctionImpl(
        override val functionName:String,
        override val params:List<JsProperty>,
        override val returnType:JsPropertyType?
) : JsFunction {
    override val numberOfParams:Int = params.size
    override val hasRequiredParams:Boolean = params.isNotEmpty() && params.hasRequired
    override val possibleReturnTypes:List<JsClass> = returnType?.typesAsClasses ?: listOf()
    override val entityName: String
        get() = functionName
}

internal val List<JsProperty>.hasRequired : Boolean get () {
    return this.filterNot { it.propertyType?.nullable.orTrue() }.isNotEmpty()
}

class JsFunctionBuilder(internal val functionName: String) {
    internal val params:MutableList<JsProperty> = mutableListOf()
    internal var returnType:JsPropertyType? = null

    fun build() : JsFunction {
        return JsFunctionImpl(functionName, params, returnType)
    }
}

fun JsFunctionBuilder.param(param:String, paramInit:JsPropertyBuilder.()->Unit) {
    params.add(JsPropertyBuilder(param).apply(paramInit).build())
}

fun JsFunctionBuilder.withReturnType(returnTypeInit:JsPropertyType.()->Unit) {
    if (returnType != null) {
        throw RuntimeException("Return type has already been set for function $functionName")
    }
    this.returnType = JsPropertyTypeImpl().apply(returnTypeInit)
}


// ============================== //
// ======== Search Utils ======== //
// ============================== //

fun JsClass?.hasFunction(functionName:String) : Boolean {
    return this?._functions?.containsKey(functionName) ?: return true
}

fun JsClass.getFunction(functionName:String) : JsFunction? {
    return _functions[functionName]
}

fun JsClass?.findFunctionNames(query:String) : List<String> {
    return this?.functionNames?.findFuzzy(query) ?: listOf()
}

fun JsClass?.findPropertyNames(query:String) : List<String> {
    return this?.propertyNames.findFuzzy(query) ?: listOf()
}

fun <T:JsNamedEntity> List<T>.find(string:String) : List<T> {
    val regex = string.splitOnUppercase()
            .joinToString("(.*)")
            .toRegex()
    val out = filterByName {
        regex.matches(it)
    }
    out.sortedBy {
        if (it.entityName.startsWith(string)) 0 else 10
    }
    return out
}

fun Collection<String>.findFuzzy(searchString:String) : List<String> {
    val regex = searchString.splitOnUppercase()
            .joinToString("(.*)")
            .toRegex()
    val out = this.filter {
        regex.matches(it)
    }
    out.sortedBy {
        if (it.startsWith(searchString)) 0 else 10
    }
    return out
}

fun <T:JsNamedEntity> List<T>.filterByName(check:(String) -> Boolean) : List<T> {
    return this.filter {
        check(it.entityName)
    }
}
