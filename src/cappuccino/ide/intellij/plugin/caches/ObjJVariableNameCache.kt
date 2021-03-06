package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker


class ObjJVariableNameCache(variableName: ObjJVariableName) {
    private val manager: CachedValuesManager by lazy { CachedValuesManager.getManager(variableName.project) }
    private val dependencies:Array<Any> by lazy { listOf(PsiModificationTracker.MODIFICATION_COUNT).toTypedArray<Any>() }

    private val resolvedCache: CachedValue<ObjJVariableName?> by lazy {
        createResolvedVariableNameCache(variableName, manager, dependencies)
    }
    private val possibleMethodsCache: CachedValue<List<ObjJMethodHeaderDeclaration<*>>> by lazy {
        createPossibleMethodsCache(variableName, manager, dependencies)
    }
    private val classTypesCache: CachedValue<InferenceResult?> by lazy {
        createClassTypesCachedValue(manager, dependencies)
    }
    private val parentFunctionDeclarationCache: CachedValue<ObjJUniversalFunctionElement?> by lazy {
        createFunctionDeclarationsCache(variableName, manager, dependencies)
    }
    val myTreeChangeTracker = MyModificationTracker()


    private fun createResolvedVariableNameCache(variableName: ObjJVariableName, manager: CachedValuesManager, dependencies:Array<Any>)  : CachedValue<ObjJVariableName?> {
        val provider = CachedValueProvider<ObjJVariableName?> {
            val resolved = variableName.reference.resolve(false) as? ObjJVariableName
            CachedValueProvider.Result.create(resolved, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createClassTypesCachedValue(manager: CachedValuesManager, dependencies: Array<Any>)  : CachedValue<InferenceResult?> {
        val provider = CachedValueProvider {
            CachedValueProvider.Result.create(inferredTypes, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private val inferredTypes: InferenceResult? get() {
        val variableName = resolvedCache.value ?: return INFERRED_ANY_TYPE
        return inferQualifiedReferenceType(variableName.previousSiblings + variableName, myTreeChangeTracker.tag)
    }

    private fun createPossibleMethodsCache(variableName: ObjJVariableName, manager: CachedValuesManager, dependencies: Array<Any>) : CachedValue<List<ObjJMethodHeaderDeclaration<*>>> {
        val provider = CachedValueProvider {
            CachedValueProvider.Result.create(getAllMethods(variableName), dependencies)
        }
        return manager.createCachedValue(provider)
    }

    fun getMethods(tag:Tag):List<ObjJMethodHeaderDeclaration<*>> {
        if (myTreeChangeTracker.tagged(tag))
            return emptyList()
        return possibleMethodsCache.value ?: emptyList()
    }

    fun getClassTypes(tag:Tag) : InferenceResult? {
        if (myTreeChangeTracker.tagged(tag))
            return null
        return classTypesCache.value
    }

    val cachedParentFunctionDeclaration: ObjJUniversalFunctionElement?
        get() = parentFunctionDeclarationCache.value

    private fun getAllMethods(variableName: ObjJVariableName) : List<ObjJMethodHeaderDeclaration<*>> {
        val project = variableName.project
        var classes:Set<String> = (classTypesCache.value ?: inferredTypes)?.toClassList(null)?.withoutAnyType() ?: return emptyList()/*?.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project)
        }.orEmpty()*/
        if (classes.isEmpty())
            return emptyList()
        classes = classes.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) }.toSet()
        val selectorLiterals:List<ObjJMethodHeaderDeclaration<*>> = ObjJPsiImplUtil.respondsToSelectors(variableName)
        return classes.flatMap {
            ObjJClassMethodIndex.instance[it, project]
        } + selectorLiterals
    }

    private fun createFunctionDeclarationsCache(
            functionName: ObjJVariableName,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ) : CachedValue<ObjJUniversalFunctionElement?> {
        val provider = CachedValueProvider<ObjJUniversalFunctionElement?> {
            val resolved = ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(functionName)
            CachedValueProvider.Result.create(resolved, dependencies)
        }
        return manager.createCachedValue(provider)
    }
}