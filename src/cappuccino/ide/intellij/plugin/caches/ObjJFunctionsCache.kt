package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionNameElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

class ObjJFunctionNameCache(functionName: ObjJFunctionNameElement) {
    private val functionDeclarationsCache:CachedValue<ObjJFunctionDeclarationElement<*>?>
    init {
        val manager = CachedValuesManager.getManager(functionName.project)
        val modificationTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(modificationTracker).toTypedArray()
        functionDeclarationsCache = createFunctionDeclarationsCache(functionName, manager, dependencies)
    }

    val parentFunctionDeclarationElement
        get() = functionDeclarationsCache.value

    val returnTypes:InferenceResult?
        get() = (parentFunctionDeclarationElement as ObjJFunctionDeclarationElement<*>).cachedReturnType

    private fun createFunctionDeclarationsCache(
            functionName:ObjJFunctionNameElement,
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

class ObjJFunctionDeclarationCache(functionDeclaration:ObjJFunctionDeclarationElement<*>) {
    private val returnStatementsCache: CachedValue<Map<ObjJReturnStatement, InferenceResult?>>
    private var returnTypesInternal: InferenceResult? = null
    val returnTypes: InferenceResult
        get() {
            var types = returnTypesInternal
            if (types != null)
                return types
            types = returnStatementsCache.value?.values?.filterNotNull()?.collapse()
            returnTypesInternal = types
            return types ?: INFERRED_ANY_TYPE
        }

    init {
        val manager = CachedValuesManager.getManager(functionDeclaration.project)
        val modificationTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(modificationTracker).toTypedArray()
        returnStatementsCache = createReturnStatementsCache(functionDeclaration, manager, dependencies)
    }

    private fun createReturnStatementsCache(
            declaration: ObjJFunctionDeclarationElement<*>,
            manager: CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<Map<ObjJReturnStatement, InferenceResult?>> {
        val provider = CachedValueProvider<Map<ObjJReturnStatement, InferenceResult?>> {
            val map:MutableMap<ObjJReturnStatement, InferenceResult?> = mutableMapOf()
            val tag = createTag()
            val returnStatements = declaration.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
            returnStatements.forEach {
                val type = if (it.expr != null) {
                    when (it.expr?.text.orEmpty()) {
                        "self" -> listOfNotNull(it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName).toInferenceResult()
                        "super" -> listOfNotNull(it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text).toInferenceResult()
                        else -> inferExpressionType(it.expr, tag)
                    }
                } else
                    null
                map[it] = type
            }
            returnTypesInternal = map.values.filterNotNull().collapse()
            CachedValueProvider.Result.create(map, dependencies)
        }
        return manager.createCachedValue(provider)
    }
}