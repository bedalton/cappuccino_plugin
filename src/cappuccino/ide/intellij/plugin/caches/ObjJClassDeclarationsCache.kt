package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getAllMethodHeaders
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

abstract class ObjJClassDeclarationsCache(declaration:ObjJClassDeclarationElement<*>) {
    protected val manager: CachedValuesManager = CachedValuesManager.getManager(declaration.project)
    protected val myTreeChangeTracker = MyModificationTracker()
    val inheritedProtocols:CachedValue<List<ObjJProtocolDeclaration>>
    private val internalMethodsCache:CachedValue<List<ObjJMethodHeader>>
    abstract val allAccessorProperties:CachedValue<List<ObjJAccessorProperty>>
    abstract val internalAccessorProperties:CachedValue<List<ObjJAccessorProperty>>
    abstract val classMethodsCache:CachedValue<List<ObjJMethodHeader>>

    init {
        val dependencies:Array<Any> = listOf(myTreeChangeTracker).toTypedArray()
        inheritedProtocols= createInheritedProtocolsCachedValue(declaration, manager, dependencies)
        internalMethodsCache = createInternalMethodsCachedValue(declaration, manager, dependencies)
    }

    private fun createInheritedProtocolsCachedValue(
            declaration:ObjJClassDeclarationElement<*>,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<List<ObjJProtocolDeclaration>> {
        val provider = CachedValueProvider<List<ObjJProtocolDeclaration>> {
            val protocols = ObjJInheritanceUtil.getAllProtocols(declaration)
            CachedValueProvider.Result.create(protocols, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createInternalMethodsCachedValue(
            declaration:ObjJClassDeclarationElement<*>,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val internalMethods = getAllMethods(declaration)
            CachedValueProvider.Result.create(internalMethods, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    fun getMethods(internalOnly:Boolean = false) : List<ObjJMethodHeader> {
        return if (internalOnly)
            internalMethodsCache.value
        else
            classMethodsCache.value
    }

    fun getAccessorProperties(internalOnly:Boolean = false) : List<ObjJAccessorProperty> {
        return if (internalOnly)
            internalAccessorProperties.value
        else
            allAccessorProperties.value
    }

    fun getAllSelectors(internalOnly: Boolean) : Set<String> {
        return (getMethods(internalOnly).map { it.selectorString } +
                getAccessorProperties(internalOnly).flatMap { listOf(it.setter, it.getter).filterNotNull() }).toSet()
    }
}

class ObjJImplementationDeclarationCache(classDeclaration: ObjJImplementationDeclaration) : ObjJClassDeclarationsCache(classDeclaration) {

    val superClassCachedValue:CachedValue<List<ObjJImplementationDeclaration>>

    override val classMethodsCache: CachedValue<List<ObjJMethodHeader>>
    override val allAccessorProperties: CachedValue<List<ObjJAccessorProperty>>
    override val internalAccessorProperties: CachedValue<List<ObjJAccessorProperty>>

    init {
        val dependencies:Array<Any> = listOf(myTreeChangeTracker).toTypedArray()
        superClassCachedValue = createSuperClassCachedValue(classDeclaration, manager, dependencies)
        classMethodsCache = createClassMethodsCache(classDeclaration, manager, dependencies)
        allAccessorProperties = createAccessorPropertiesCache(classDeclaration, manager, dependencies)
        internalAccessorProperties = createInternalAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    private fun createSuperClassCachedValue(
            declaration:ObjJImplementationDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<List<ObjJImplementationDeclaration>> {
        val provider = CachedValueProvider<List<ObjJImplementationDeclaration>> {
            val protocols = ObjJInheritanceUtil.getAllSuperClasses(declaration)
            CachedValueProvider.Result.create(protocols, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createClassMethodsCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val classes:MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(superClassCachedValue.value )
            classes.addAll(inheritedProtocols.value)
            classes.add(classDeclaration)
            val methods = getAllMethods(classes)
            CachedValueProvider.Result.create(methods, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createInternalAccessorPropertiesCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            CachedValueProvider.Result.create(getAccessors(classDeclaration), dependencies)
        }
        return manager.createCachedValue(provider)
    }


    private fun createAccessorPropertiesCache(
            classDeclaration: ObjJImplementationDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {

        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(superClassCachedValue.value)
            classes.addAll(inheritedProtocols.value)
            classes.add(classDeclaration)
            CachedValueProvider.Result.create(getAccessors(classes), dependencies)
        }
        return manager.createCachedValue(provider)
    }
}



class ObjJProtocolDeclarationCache(classDeclaration: ObjJProtocolDeclaration) : ObjJClassDeclarationsCache(classDeclaration) {

    override val classMethodsCache: CachedValue<List<ObjJMethodHeader>>
    override val allAccessorProperties: CachedValue<List<ObjJAccessorProperty>>
    override val internalAccessorProperties: CachedValue<List<ObjJAccessorProperty>>

    init {
        val dependencies:Array<Any> = listOf(myTreeChangeTracker).toTypedArray()
        classMethodsCache = createClassMethodsCache(classDeclaration, manager, dependencies)
        allAccessorProperties = createAccessorPropertiesCache(classDeclaration, manager, dependencies)
        internalAccessorProperties = createInternalAccessorPropertiesCache(classDeclaration, manager, dependencies)
    }

    private fun createClassMethodsCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<List<ObjJMethodHeader>> {
        val provider = CachedValueProvider<List<ObjJMethodHeader>> {
            val classes:MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(inheritedProtocols.value)
            classes.add(classDeclaration)
            val methods = getAllMethods(classes)
            CachedValueProvider.Result.create(methods, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createInternalAccessorPropertiesCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            CachedValueProvider.Result.create(getAccessors(classDeclaration), dependencies)
        }
        return manager.createCachedValue(provider)
    }


    private fun createAccessorPropertiesCache(
            classDeclaration: ObjJProtocolDeclaration,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ): CachedValue<List<ObjJAccessorProperty>> {
        val provider = CachedValueProvider<List<ObjJAccessorProperty>> {
            val classes: MutableList<ObjJClassDeclarationElement<*>> = mutableListOf()
            classes.addAll(inheritedProtocols.value)
            classes.add(classDeclaration)
            CachedValueProvider.Result.create(getAccessors(classes), dependencies)
        }
        return manager.createCachedValue(provider)
    }
}


private fun getAllMethods(declarations:List<ObjJClassDeclarationElement<*>>) : List<ObjJMethodHeader> {
    return declarations.flatMap {
        getAllMethods(it)
    }
}

private fun getAllMethods(declaration:ObjJClassDeclarationElement<*>) : List<ObjJMethodHeader> {
    if (declaration is ObjJImplementationDeclaration) {
        return getAllMethodHeaders(declaration)
    }
    if (declaration is ObjJProtocolDeclaration) {
        return getAllMethodHeaders(declaration)
    }
    throw Exception("Unknown class declaration element type encountered")
}

private fun getAccessors(declarations:List<ObjJClassDeclarationElement<*>>) : List<ObjJAccessorProperty> {
    return declarations.flatMap { getAccessors(it) }
}

private fun getAccessors(declaration:ObjJClassDeclarationElement<*>) : List<ObjJAccessorProperty>  {
    if (declaration is ObjJProtocolDeclaration)
        return getAccessors(declaration)
    if (declaration is ObjJImplementationDeclaration)
        return getAccessors(declaration)
    throw Exception("Unknown class declaration element type encountered")
}

private fun getAccessors(declaration:ObjJProtocolDeclaration) : List<ObjJAccessorProperty> {
    return declaration.instanceVariableDeclarationList.flatMap {
        it.accessorPropertyList
    } + declaration.protocolScopedMethodBlockList.flatMap { scopedMethodBlock ->
        scopedMethodBlock.instanceVariableDeclarationList.flatMap { it.accessorPropertyList }
    }
}

private fun getAccessors(declaration:ObjJImplementationDeclaration) : List<ObjJAccessorProperty> {
    return declaration.instanceVariableList?.instanceVariableDeclarationList?.flatMap {
        it.accessorPropertyList
    }.orEmpty()
}