package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.plus
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getAllMethodHeaders
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJMethodStruct
import cappuccino.ide.intellij.plugin.stubs.stucts.getMethodStructs
import cappuccino.ide.intellij.plugin.stubs.stucts.toMethodStruct
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

/**
 * Base class for class declarations Cache
 */
abstract class ObjJClassDeclarationsCache(declaration: ObjJClassDeclarationElement<*>) {
    /**
     * Cached values manager
     */
    protected val manager: CachedValuesManager = CachedValuesManager.getManager(declaration.project)
    /**
     * Tree change tracker for tracking modifications
     */
    private val myTreeChangeTracker = MyModificationTracker()

    /**
     * Cache dependencies object
     */
    protected val dependencies: Array<Any> by lazy { listOf(myTreeChangeTracker, PsiModificationTracker.MODIFICATION_COUNT).toTypedArray() }

    /**
     * A cached value for all inherited protocol elements
     */
    protected val inheritedProtocolsCache: CachedValue<List<ObjJProtocolDeclaration>> by lazy {
        createInheritedProtocolsCachedValue(declaration, manager, dependencies)
    }

    val inheritedProtocols: List<ObjJProtocolDeclaration>
        get() = inheritedProtocolsCache.value

    /**
     * A cache value for all methods defined in this class declaration element only
     */
    private val internalMethodsCache: CachedValue<List<ObjJMethodHeader>> by lazy {
        createInternalMethodsCachedValue(declaration, manager, dependencies)
    }

    private var internalMethodStructs: List<ObjJMethodStruct> = emptyList()

    private var classMethodStructs: List<ObjJMethodStruct> = emptyList()

    /**
     * A cached value for all accessors in this and inherited classes
     */
    abstract val allAccessorProperties: CachedValue<List<ObjJAccessorProperty>>

    /**
     * A cache value for accessors defined in this class declaration element only
     */
    abstract val internalAccessorProperties: CachedValue<List<ObjJAccessorProperty>>

    /**
     * A cache of all methods defined in this and inherited classes
     */
    abstract val classMethodsCache: CachedValue<List<ObjJMethodHeader>>

    /**
     * A map of selectors to their respective method elements
     */
    protected abstract val methodReturnValuesMap: CachedValue<Map<String, Pair<ObjJCompositeElement, InferenceResult?>>>


    /**
     * Creates cache value for all inherited protocol elements
     */
    protected fun createInheritedProtocolsCachedValue(
            declaration: ObjJClassDeclarationElement<*>,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJProtocolDeclaration>> {
        val provider = CachedValueProvider<List<ObjJProtocolDeclaration>> {
            val protocols = ObjJInheritanceUtil.getAllProtocols(declaration)
            CachedValueProvider.Result.create(protocols, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    /**
     * Creates cache value for internal method declarations defined in this class element only
     */
    private fun createInternalMethodsCachedValue(
            declaration: ObjJClassDeclarationElement<*>,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val internalMethods = getAllMethods(declaration)
            CachedValueProvider.Result.create(internalMethods, dependencies)
        }
        return manager.createCachedValue(provider)
    }


    /**
     * Gets methods for this class, filtering by internal only if needed
     */
    fun getMethods(internalOnly: Boolean = false): List<ObjJMethodHeader> {
        return if (internalOnly)
            internalMethodsCache.value
        else
            classMethodsCache.value
    }

    fun getMethodStructs(internalOnly: Boolean, tag: Long): List<ObjJMethodStruct> {
        if(tag == myTreeChangeTracker.tag) {
            return emptyList()
        }
        return if (internalOnly) {
            if (internalMethodsCache.hasUpToDateValue() && internalAccessorProperties.hasUpToDateValue() && internalMethodStructs.isNotEmpty())
                internalMethodStructs
            else {
                myTreeChangeTracker.tag = tag
                internalMethodsCache.value.map { it.toMethodStruct(tag) } + internalAccessorProperties.value.flatMap { (it.parent.parent as? ObjJInstanceVariableDeclaration)?.getMethodStructs().orEmpty() }.toSet()
            }
        } else if (classMethodsCache.hasUpToDateValue() && allAccessorProperties.hasUpToDateValue() && classMethodStructs.isNotEmpty()) {
            classMethodStructs
        } else {
            classMethodsCache.value.map {
                it.toMethodStruct(tag)
            } + allAccessorProperties.value
                    .mapNotNull { it.parent.parent as? ObjJInstanceVariableDeclaration }.toSet()
                    .flatMap {
                        it.getMethodStructs()
                    }.toSet()
        }
    }

    /**
     * Gets accessor properties for this class, filtering by internal if needed
     */
    fun getAccessorProperties(internalOnly: Boolean = false): List<ObjJAccessorProperty> {
        return if (internalOnly)
            internalAccessorProperties.value
        else
            allAccessorProperties.value
    }

    fun getAllSelectors(internalOnly: Boolean): Set<String> {
        return getMethodStructs(internalOnly, createTag()).map { it.selectorString }.toSet()
    }

    protected fun createMethodReturnValuesMap(manager: CachedValuesManager, dependencies: Array<Any>): CachedValue<Map<String, Pair<ObjJCompositeElement, InferenceResult?>>> {
        val provider = CachedValueProvider<Map<String, Pair<ObjJCompositeElement, InferenceResult?>>> {
            val allMethodHeaders: List<ObjJMethodHeaderDeclaration<*>> = this.classMethodsCache.value
            val map: MutableMap<String, Pair<ObjJCompositeElement, InferenceResult?>> = mutableMapOf()
            allMethodHeaders.forEach {
                val parent = it.parent as? ObjJCompositeElement ?: return@forEach
                val existing = map[it.selectorString]?.second
                val thisType = it.getCachedReturnType(myTreeChangeTracker.tag) ?: return@forEach
                if (existing != null)
                    map[it.selectorString] = Pair(parent, thisType + existing)
                else
                    map[it.selectorString] = Pair(parent, thisType)
            }
            allAccessorProperties.value.forEach {
                val parent = it.parent as? ObjJCompositeElement ?: return@forEach
                val existing = map[it.selectorString]?.second
                val thisType = it.getCachedReturnType(myTreeChangeTracker.tag) ?: return@forEach
                val pair =
                        if (existing != null)
                            Pair(parent, thisType + existing)
                        else
                            Pair(parent, thisType)
                if (it.getter != null)
                    map[it.getter!!] = pair
                if (it.setter != null)
                    map[it.setter!!] = pair
            }
            CachedValueProvider.Result.create(map, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    fun getMethodReturnType(selector: String, tag: Long): InferenceResult? {
        if (myTreeChangeTracker.tag == tag)
            return null
        myTreeChangeTracker.tag = tag
        return methodReturnValuesMap.value[selector]?.second ?: INFERRED_ANY_TYPE
    }

}

/**
 * A Cache values class for @implementation classes
 */
class ObjJImplementationDeclarationCache(classDeclaration: ObjJImplementationDeclaration) : ObjJClassDeclarationsCache(classDeclaration) {
    /**
     * Gets all super classes for this class
     */
    val superClassCachedValue: CachedValue<List<ObjJImplementationDeclaration>> by lazy {
        createSuperClassCachedValue(classDeclaration, manager, dependencies)
    }

    /**
     * Cache value for all methods in this and inherited protocols and super classes
     */
    override val classMethodsCache: CachedValue<List<ObjJMethodHeader>> by lazy {
        createClassMethodsCache(classDeclaration, manager, dependencies)
    }

    /**
     * Cache of all accessor properties for this and inherited protocols and super classes
     */
    override val allAccessorProperties: CachedValue<List<ObjJAccessorProperty>> by lazy {
        createAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    /**
     * Cacge of accessors defined only in this implementation
     */
    override val internalAccessorProperties: CachedValue<List<ObjJAccessorProperty>> by lazy {
        createInternalAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    /**
     * Cache of method return types by selector
     */
    override val methodReturnValuesMap: CachedValue<Map<String, Pair<ObjJCompositeElement, InferenceResult?>>> by lazy {
        createMethodReturnValuesMap(manager, dependencies)
    }

    val superClassDeclarations: List<ObjJImplementationDeclaration>
        get() = superClassCachedValue.value

    /**
     * Creates cache value for super classes
     */
    private fun createSuperClassCachedValue(
            declaration: ObjJImplementationDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJImplementationDeclaration>> {
        val provider = CachedValueProvider<List<ObjJImplementationDeclaration>> {
            val protocols = ObjJInheritanceUtil.getAllSuperClasses(declaration)
            CachedValueProvider.Result.create(protocols, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    /**
     * Creates a cache value for all class methods in this and super classes and protocols
     */
    private fun createClassMethodsCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(superClassCachedValue.value)
            classes.addAll(inheritedProtocolsCache.value)
            classes.add(classDeclaration)
            val methods = getAllMethods(classes)
            CachedValueProvider.Result.create(methods, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    /**
     * Create a cache value for accessors in this declaration element only
     */
    private fun createInternalAccessorPropertiesCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            CachedValueProvider.Result.create(getAccessors(classDeclaration), dependencies)
        }
        return manager.createCachedValue(provider)
    }


    /**
     * Creates a cache for all accessor properties in this and super classes and protocols
     */
    private fun createAccessorPropertiesCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {

        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(superClassCachedValue.value)
            classes.addAll(inheritedProtocolsCache.value)
            classes.add(classDeclaration)
            CachedValueProvider.Result.create(getAccessors(classes), dependencies)
        }
        return manager.createCachedValue(provider)
    }
}


/**
 * A Cache values object for protocol declarations
 */
class ObjJProtocolDeclarationCache(classDeclaration: ObjJProtocolDeclaration) : ObjJClassDeclarationsCache(classDeclaration) {

    /**
     * Caches all class methods for this and inherited protocols
     */
    override val classMethodsCache: CachedValue<List<ObjJMethodHeader>> by lazy {
        createClassMethodsCache(classDeclaration, manager, dependencies)
    }

    /**
     * Gets all accessor properties in this and inherited protocols
     */
    override val allAccessorProperties: CachedValue<List<ObjJAccessorProperty>> by lazy {
        createAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    /**
     * Gets accessors only in this protocol
     */
    override val internalAccessorProperties: CachedValue<List<ObjJAccessorProperty>> by lazy {
        createInternalAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    /**
     * Takes method list and creates a value map
     */
    override val methodReturnValuesMap: CachedValue<Map<String, Pair<ObjJCompositeElement, InferenceResult?>>> by lazy {
        createMethodReturnValuesMap(manager, dependencies)
    }

    /**
     * Creates a class methods cache value for this and inherited protocols
     */
    private fun createClassMethodsCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(inheritedProtocolsCache.value)
            classes.add(classDeclaration)
            val methods = getAllMethods(classes)
            CachedValueProvider.Result.create(methods, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    /**
     * Creates a cache value for internal accessors
     */
    private fun createInternalAccessorPropertiesCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            CachedValueProvider.Result.create(getAccessors(classDeclaration), dependencies)
        }
        return manager.createCachedValue(provider)
    }


    /**
     * Creates an accessor properties cache for this and inherited accessors
     */
    private fun createAccessorPropertiesCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(inheritedProtocolsCache.value)
            classes.add(classDeclaration)
            CachedValueProvider.Result.create(getAccessors(classes), dependencies)
        }
        return manager.createCachedValue(provider)
    }
}


/**
 * Gets all methods from a list of declarations
 */
private fun getAllMethods(declarations: List<ObjJClassDeclarationElement<*>>): List<ObjJMethodHeader> {
    return declarations.flatMap {
        getAllMethods(it)
    }
}

/**
 * Gets all method for a declaration
 */
private fun getAllMethods(declaration: ObjJClassDeclarationElement<*>): List<ObjJMethodHeader> {
    if (declaration is ObjJImplementationDeclaration) {
        return getAllMethodHeaders(declaration)
    }
    if (declaration is ObjJProtocolDeclaration) {
        return getAllMethodHeaders(declaration)
    }
    throw Exception("Unknown class declaration element type encountered")
}

/**
 * Gets accessors for a list of class declaration elements
 */
private fun getAccessors(declarations: List<ObjJClassDeclarationElement<*>>): List<ObjJAccessorProperty> {
    return declarations.flatMap { getAccessors(it) }
}

/**
 * Gets accessors for a single class declaration element
 */
private fun getAccessors(declaration: ObjJClassDeclarationElement<*>): List<ObjJAccessorProperty> {
    if (declaration is ObjJProtocolDeclaration)
        return getAccessors(declaration)
    if (declaration is ObjJImplementationDeclaration)
        return getAccessors(declaration)
    throw Exception("Unknown class declaration element type encountered")
}

/**
 * Gets accessors for a protocol element
 */
private fun getAccessors(declaration: ObjJProtocolDeclaration): List<ObjJAccessorProperty> {
    return declaration.instanceVariableDeclarationList.flatMap {
        it.accessorPropertyList
    } + declaration.protocolScopedMethodBlockList.flatMap { scopedMethodBlock ->
        scopedMethodBlock.instanceVariableDeclarationList.flatMap { it.accessorPropertyList }
    }
}


/**
 * Gets accessors for an implementation element
 */
private fun getAccessors(declaration: ObjJImplementationDeclaration): List<ObjJAccessorProperty> {
    return declaration.instanceVariableList?.instanceVariableDeclarationList?.flatMap {
        it.accessorPropertyList
    }.orEmpty()
}