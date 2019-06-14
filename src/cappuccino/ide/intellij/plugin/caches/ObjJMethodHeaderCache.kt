package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySibling
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

fun createMethodHeaderCache(methodHeader: ObjJMethodHeaderDeclaration<*>) : ObjJMethodHeaderDeclarationCache {
    return when (methodHeader) {
        is ObjJAccessorProperty -> ObjJAccessorCache(methodHeader)
        is ObjJMethodHeader -> ObjJMethodHeaderCache(methodHeader)
        is ObjJSelectorLiteral -> ObjJSelectorLiteralCache(methodHeader)
        else -> throw Exception("Unexpected method header declaration type: <${methodHeader.elementType}>")
    }
}

interface ObjJMethodHeaderDeclarationCache {
    fun getCachedReturnType(tag:Long):InferenceResult?
}

class ObjJMethodHeaderCache(val methodHeader:ObjJMethodHeader) : ObjJMethodHeaderDeclarationCache {
    private val returnStatementsCache: CachedValue<Map<ObjJCompositeElement, InferenceResult?>> by lazy {
        createReturnStatementsCache(methodHeader, manager, dependencies)
    }
    private val manager: CachedValuesManager by lazy {
        CachedValuesManager.getManager(methodHeader.project)
    }
    private val dependencies: Array<Any> by lazy {
        listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT).toTypedArray()
    }
    private var returnTypesInternal: InferenceResult? = null
    private val modificationTracker = MyModificationTracker()


    override fun getCachedReturnType(tag: Long): InferenceResult {
        modificationTracker.tag = tag
        var types = returnTypesInternal
        if (types != null)
            return types
        types = returnStatementsCache.value?.values?.filterNotNull()?.collapse()
        returnTypesInternal = types
        return types ?: INFERRED_ANY_TYPE
    }

    private fun createReturnStatementsCache(
            methodHeader: ObjJMethodHeader,
            manager: CachedValuesManager,
            dependencies: Array<Any>
    ): CachedValue<Map<ObjJCompositeElement, InferenceResult?>> {
        val provider = CachedValueProvider<Map<ObjJCompositeElement, InferenceResult?>> {
            val methodReturnTypeElement = methodHeader.methodHeaderReturnTypeElement?.formalVariableType
            val returnTypeElementType = methodReturnTypeElement?.varTypeId?.className?.text
                    ?: methodReturnTypeElement?.text
            val map: MutableMap<ObjJCompositeElement, InferenceResult?> = mutableMapOf()
            if (returnTypeElementType != null && returnTypeElementType != "id") {
                map[methodReturnTypeElement!!] = listOfNotNull(returnTypeElementType).toInferenceResult()
                return@CachedValueProvider CachedValueProvider.Result.create(map, dependencies)
            }
            val returnStatements = (methodHeader.getNextNonEmptySibling(true) as? ObjJBlock)?.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).orEmpty()
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

class ObjJAccessorCache(val accessor:ObjJAccessorProperty) : ObjJMethodHeaderDeclarationCache  {
    private val manager by lazy {
        CachedValuesManager.getManager(accessor.project)
    }
    private val modificationTracker = MyModificationTracker()
    private val dependencies:Array<Any>  by lazy {
        listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT).toTypedArray()
    }

    private val variableTypeCache:CachedValue<Map<ObjJCompositeElement, InferenceResult?>> by lazy {
        createReturnStatementsCache(accessor, manager, dependencies)
    }
    private var returnTypesInternal:InferenceResult? = null

    override fun getCachedReturnType(tag:Long):InferenceResult {
        var types = returnTypesInternal
        if (types != null)
            return types
        types = variableTypeCache.value?.values?.filterNotNull()?.collapse()
        returnTypesInternal = types
        return types ?: INFERRED_ANY_TYPE
    }


    private fun createReturnStatementsCache(
            accessor: ObjJAccessorProperty,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<Map<ObjJCompositeElement, InferenceResult?>> {
        val provider = CachedValueProvider<Map<ObjJCompositeElement, InferenceResult?>> {
            val declaration = accessor.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
            val variableType = declaration?.formalVariableType
            if (variableType?.text.isNullOrBlank()) {
                val parent = accessor.parent as? ObjJCompositeElement ?: return@CachedValueProvider CachedValueProvider.Result.create(mapOf<ObjJCompositeElement, InferenceResult?>(), dependencies)
                return@CachedValueProvider CachedValueProvider.Result.create(mapOf<ObjJCompositeElement, InferenceResult?>(parent to null), dependencies)
            }
            val map: MutableMap<ObjJCompositeElement, InferenceResult?> = if (variableType != null) {
                val returnTypeElementType = listOfNotNull(variableType.varTypeId?.className?.text
                        ?: variableType.text).toSet().toInferenceResult()
                mutableMapOf(declaration to returnTypeElementType)
            } else {
                mutableMapOf()
            }
            returnTypesInternal = map.values.firstOrNull()
            CachedValueProvider.Result.create(map, dependencies)
        }
        return manager.createCachedValue(provider)
    }

}


class ObjJSelectorLiteralCache(val selector: ObjJSelectorLiteral) : ObjJMethodHeaderDeclarationCache  {
    override fun getCachedReturnType(tag:Long):InferenceResult
            = INFERRED_ANY_TYPE

}