package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionNameElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

class ObjJFunctionNameCache(functionName: ObjJFunctionNameElement) {
    private val manager by lazy { CachedValuesManager.getManager(functionName.project) }
    private val modificationTracker by lazy { MyModificationTracker() }
    private val dependencies:Array<Any> by lazy {listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT).toTypedArray()}
    private val functionDeclarationsCache:CachedValue<ObjJFunctionDeclarationElement<*>?> by lazy {
        createFunctionDeclarationsCache(functionName, manager, dependencies)
    }
    val parentFunctionDeclarationElement
        get() = functionDeclarationsCache.value

    fun getReturnType(tag:Long):InferenceResult?
            = parentFunctionDeclarationElement?.getCachedReturnType(tag)

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
    val modificationTracker = MyModificationTracker()
    private val returnStatementsCache: CachedValue<Map<ObjJReturnStatement, InferenceResult?>>
    private var returnTypesInternal: InferenceResult? = null

    fun returnTypes(tag:Long): InferenceResult {
        if (modificationTracker.tag == tag)
            return INFERRED_ANY_TYPE
        modificationTracker.tag = tag
        val types = returnStatementsCache.value?.values?.filterNotNull()
        return if (types.isNotNullOrEmpty()) {
            types!!.collapse()
        } else
            INFERRED_VOID_TYPE
    }

    init {
        val manager = CachedValuesManager.getManager(functionDeclaration.project)
        val dependencies:Array<Any> = listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT).toTypedArray()
        returnStatementsCache = createReturnStatementsCache(functionDeclaration, manager, dependencies)
    }

    private fun createReturnStatementsCache(
            declaration: ObjJFunctionDeclarationElement<*>,
            manager: CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<Map<ObjJReturnStatement, InferenceResult?>> {
        val provider = CachedValueProvider<Map<ObjJReturnStatement, InferenceResult?>> {
            val map:MutableMap<ObjJReturnStatement, InferenceResult?> = mutableMapOf()
            val returnStatements = declaration.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
            returnStatements.forEach {
                val type = if (it.expr != null) {
                    when (it.expr?.text.orEmpty()) {
                        "self" -> listOfNotNull(it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName).toInferenceResult()
                        "super" -> listOfNotNull(it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text).toInferenceResult()
                        else -> inferExpressionType(it.expr, modificationTracker.tag)
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