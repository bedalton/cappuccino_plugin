package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

fun getMethodHeaderCache(methodHeader: ObjJMethodHeaderDeclaration<*>) : ObjJMethodHeaderDeclarationCache {
    return when (methodHeader) {
        is ObjJAccessorProperty -> ObjJAccessorCache(methodHeader)
        is ObjJMethodHeader -> ObjJMethodHeaderCache(methodHeader)
        is ObjJSelectorLiteral -> ObjJSelectorLiteralCache(methodHeader)
        else -> throw Exception("Unexpected method header declaration type: <${methodHeader.elementType}>")
    }
}

interface ObjJMethodHeaderDeclarationCache {
    val returnTypes:InferenceResult?
}

class ObjJMethodHeaderCache(val methodHeader:ObjJMethodHeader) : ObjJMethodHeaderDeclarationCache {

    private val returnStatementsCache:CachedValue<Map<ObjJCompositeElement, InferenceResult?>>
    private var returnTypesInternal:InferenceResult? = null
    override val returnTypes:InferenceResult get() {
        var types = returnTypesInternal
        if (types != null)
            return types
        types = returnStatementsCache.value?.values?.filterNotNull()?.collapse()
        returnTypesInternal = types
        return types ?: INFERRED_ANY_TYPE
    }

    init {
        val manager = CachedValuesManager.getManager(methodHeader.project)
        val modificationTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(modificationTracker).toTypedArray()
        returnStatementsCache = createReturnStatementsCache(methodHeader.getParentOfType(ObjJMethodDeclaration::class.java), manager, dependencies)
    }

    private fun createReturnStatementsCache(
            declaration:ObjJMethodDeclaration?,
            manager:CachedValuesManager,
            dependencies:Array<Any>
    ) : CachedValue<Map<ObjJCompositeElement, InferenceResult?>> {
        val provider = CachedValueProvider<Map<ObjJCompositeElement, InferenceResult?>> {
            if (declaration == null) {
                return@CachedValueProvider CachedValueProvider.Result.create(mapOf<ObjJCompositeElement, InferenceResult?>(), dependencies)
            }
            val methodReturnTypeElement = declaration.methodHeader.methodHeaderReturnTypeElement?.formalVariableType
            val returnTypeElementType = methodReturnTypeElement?.varTypeId?.className?.text
                    ?: methodReturnTypeElement?.text
            val map: MutableMap<ObjJCompositeElement, InferenceResult?> = mutableMapOf()
            if (returnTypeElementType != null && returnTypeElementType != "id") {
                map[methodReturnTypeElement!!] = listOfNotNull(returnTypeElementType).toInferenceResult()
                return@CachedValueProvider CachedValueProvider.Result.create(map, dependencies)
            }
            val tag = createTag()
            val returnStatements = declaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
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


class ObjJAccessorCache(val accessor:ObjJAccessorProperty) : ObjJMethodHeaderDeclarationCache  {
    private val variableTypeCache:CachedValue<Map<ObjJCompositeElement, InferenceResult?>>
    private var returnTypesInternal:InferenceResult? = null
    override val returnTypes:InferenceResult get() {
        var types = returnTypesInternal
        if (types != null)
            return types
        types = variableTypeCache.value?.values?.filterNotNull()?.collapse()
        returnTypesInternal = types
        return types ?: INFERRED_ANY_TYPE
    }

    init {
        val manager = CachedValuesManager.getManager(accessor.project)
        val modificationTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(modificationTracker).toTypedArray()
        variableTypeCache = createReturnStatementsCache(accessor, manager, dependencies)
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
    override val returnTypes:InferenceResult get()
            = INFERRED_ANY_TYPE

}