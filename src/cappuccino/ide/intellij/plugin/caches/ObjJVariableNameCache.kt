package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.inference.withoutAnyType
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager


class ObjJVariableNameCache(variableName: ObjJVariableName) {
    private val resolvedCache: CachedValue<ObjJVariableName?>
    private val possibleMethodsCache: CachedValue<List<ObjJMethodHeaderDeclaration<*>>>
    private val classTypesCache: CachedValue<InferenceResult?>
    private val parentFunctionDeclarationCache: CachedValue<ObjJFunctionDeclarationElement<*>?>

    init {
        val manager: CachedValuesManager = CachedValuesManager.getManager(variableName.project)
        val myTreeChangeTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(myTreeChangeTracker).toTypedArray()
        resolvedCache = createResolvedVariableNameCache(variableName, manager, dependencies)
        classTypesCache = createClassTypesCachedValue(manager, dependencies)
        possibleMethodsCache = createPossibleMethodsCache(variableName, manager, dependencies)
        parentFunctionDeclarationCache = createFunctionDeclarationsCache(variableName, manager, dependencies)
    }

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
        return inferQualifiedReferenceType(variableName.previousSiblings + variableName, createTag())
    }

    private fun createPossibleMethodsCache(variableName: ObjJVariableName, manager: CachedValuesManager, dependencies: Array<Any>) : CachedValue<List<ObjJMethodHeaderDeclaration<*>>> {
        val provider = CachedValueProvider {
            CachedValueProvider.Result.create(getAllMethods(variableName), dependencies)
        }
        return manager.createCachedValue(provider)
    }

    val methods:List<ObjJMethodHeaderDeclaration<*>> get() {
        return possibleMethodsCache.value ?: emptyList()
    }

    val classTypes get()
    = classTypesCache.value

    val cachedParentFunctionDeclaration: ObjJFunctionDeclarationElement<*>?
        get() = parentFunctionDeclarationCache.value

    private fun getAllMethods(variableName: ObjJVariableName) : List<ObjJMethodHeaderDeclaration<*>> {
        val project = variableName.project
        val classes = (classTypesCache.value ?: inferredTypes)?.toClassList(null)?.withoutAnyType() ?: return emptyList()/*?.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project)
        }.orEmpty()*/
        if (classes.isEmpty())
            return emptyList()
        val selectorLiterals:List<ObjJMethodHeaderDeclaration<*>> = ObjJPsiImplUtil.respondsToSelectors(variableName)
        return classes.flatMap {
            ObjJClassDeclarationsIndex.instance[it, project]
        }.flatMap { it.getMethodHeaders(true) } + selectorLiterals
    }

    private fun createFunctionDeclarationsCache(
            functionName: ObjJVariableName,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ) : CachedValue<ObjJFunctionDeclarationElement<*>?> {
        val provider = CachedValueProvider<ObjJFunctionDeclarationElement<*>?> {
            val resolved = ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(functionName)
            CachedValueProvider.Result.create(resolved, dependencies)
        }
        return manager.createCachedValue(provider)
    }
}