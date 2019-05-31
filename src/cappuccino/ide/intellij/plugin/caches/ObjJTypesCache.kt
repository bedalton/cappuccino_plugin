package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.inferMethodCallType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker


class ObjJExprTypesCache(expr:ObjJExpr) {
    private val typesCache: CachedValue<Set<String>>  = createExprTypesCache(expr)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createExprTypesCache(expr: ObjJExpr) : CachedValue<Set<String>> {
        val types = inferExpressionType(expr, INFERENCE_LEVELS_DEFAULT)?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(expr.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, myTreeChangeTracker)

        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}

class ObjJMethodCallTypesCache(methodCall:ObjJMethodCall) {
    private val typesCache: CachedValue<Set<String>>  = createMethodCallTypesCacheValue(methodCall)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createMethodCallTypesCacheValue(methodCall: ObjJMethodCall) : CachedValue<Set<String>> {
        val types = inferMethodCallType(methodCall, INFERENCE_LEVELS_DEFAULT)?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(methodCall.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, myTreeChangeTracker)
        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}


class ObjJQualifiedReferenceComponentTypesCache(component:ObjJQualifiedReferenceComponent) {
    private val typesCache: CachedValue<Set<String>>  = createQualifiedReferenceComponentTypesCacheValue(component)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createQualifiedReferenceComponentTypesCacheValue(component: ObjJQualifiedReferenceComponent) : CachedValue<Set<String>> {
        val types = inferQualifiedReferenceType(component.previousSiblings + component, false, INFERENCE_LEVELS_DEFAULT)?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(component.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, myTreeChangeTracker)
        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}

private class MyModificationTracker:ModificationTracker {
    internal var myCount: Long = 0;
    override fun getModificationCount(): Long {
        return myCount;
    }
}
/*
class ObjJExprCache(private val expr:ObjJExpr)  {
    val externalExpr:CachedValue<List<ObjJExpr>>
    val externalMethodHeaders:CachedValue<List<ObjJMethodHeader>>
    val externalFunctions:CachedValue<List<ObjJFunctionDeclarationElement<*>>>
    val myTreeChangeTracker = MyModificationTracker()

    init {
        val manager:CachedValuesManager = CachedValuesManager.getManager(expr.project)
        val dependencies = listOf(myTreeChangeTracker)
        val exprCacheProvider = CachedValueProvider<List<ObjJExpr>> {
            CachedValueProvider.Result.create()
        }
        externalExpr = manager.createCachedValue(exprCacheProvider)
        leftCachedValue
    }

    private fun getExternalExpr() : List<ObjJExpr> {

    }

    private fun getInternalExpr(expr:ObjJExpr) : List<ObjJExpr> {
        val out = mutableListOf(expr)
        out.addAll(getExternalVariableNameAssignmentExpr())
        expr.leftExpr.functionCall?.functionDeclarationReference
    }

    private fun getExternalVariableNameAssignmentExpr() : List<ObjJExpr> {
        return getAllInternalVariableNames(expr).flatMap { getAllVariableNameAssignmentExpressions(it) }
    }

    private fun getAllInternalVariableNames(expr: ObjJExpr?) : List<ObjJVariableName> {
        if (expr == null)
            return emptyList()
        val out = mutableListOf<ObjJVariableName?>()
        out.add(expr.leftExpr?.refExpression?.variableName)
        out.add(expr.leftExpr?.derefExpression?.variableName)
        expr.leftExpr?.arrayLiteral?.exprList.orEmpty().flatMap{
            getAllInternalVariableNames(it)
        }
        out.addAll(getAllInternalVariableNames(expr.leftExpr?.parenEnclosedExpr?.expr))
        out.addAll(getAllInternalVariableNames(expr.leftExpr?.variableDeclaration?.expr))
        out.addAll(expr.leftExpr?.qualifiedReference?.variableNameList.orEmpty())
        return out.filterNotNull()
    }

    private class MyModificationTracker:ModificationTracker {
        internal var myCount:Long = 0;
        override fun getModificationCount():Long {
            return myCount;
        }
}*/
