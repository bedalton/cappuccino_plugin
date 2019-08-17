package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.inference.TagList
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.util.ModificationTracker

/*
class ObjJExprTypesCache(expr:ObjJExpr) {
    private val typesCache: CachedValue<Set<String>>  = createExprTypesCache(expr)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createExprTypesCache(expr: ObjJExpr) : CachedValue<Set<String>> {
        val types = inferExpressionType(expr, createTag())?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(expr.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, PsiModificationTracker.MODIFICATION_COUNT, myTreeChangeTracker)

        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}

class ObjJMethodCallTypesCache(methodCall:ObjJMethodCall) {
    private val typesCache: CachedValue<Set<String>>  = createMethodCallTypesCacheValue(methodCall)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createMethodCallTypesCacheValue(methodCall: ObjJMethodCall) : CachedValue<Set<String>> {
        val types = inferMethodCallType(methodCall, createTag())?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(methodCall.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, PsiModificationTracker.MODIFICATION_COUNT, myTreeChangeTracker)
        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}


class ObjJQualifiedReferenceComponentTypesCache(component:ObjJQualifiedReferenceComponent) {
    private val typesCache: CachedValue<Set<String>>  = createQualifiedReferenceComponentTypesCacheValue(component)
    private val myTreeChangeTracker = MyModificationTracker()
    val types:Set<String> get() = typesCache.value

    private fun createQualifiedReferenceComponentTypesCacheValue(component: ObjJQualifiedReferenceComponent) : CachedValue<Set<String>> {
        val types = inferQualifiedReferenceType(component.previousSiblings + component, createTag())?.toClassList() ?: emptySet()
        val manager = CachedValuesManager.getManager(component.project)
        val dependencies = arrayOf<Any>(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT, PsiModificationTracker.MODIFICATION_COUNT, myTreeChangeTracker)
        return manager.createCachedValue({ CachedValueProvider.Result.create(types, dependencies) }, false)
    }
}

private fun ObjJExpr.collectSubExpressions() : List<ObjJExpr> {
    val temp = mutableListOf<ObjJExpr?>()
    temp.add(leftExpr?.parenEnclosedExpr?.expr)
    temp.add(leftExpr?.methodCall?.callTarget?.expr)
    temp.add(leftExpr?.newExpression?.expr)
    temp.add(leftExpr?.variableDeclaration?.expr)
    temp.add(prefixedExpr?.expr)
    for (rightExpr in rightExprList) {
        temp.add(rightExpr?.ternaryExprPrime?.ifFalse)
        temp.add(rightExpr?.ternaryExprPrime?.ifTrue)
    }
    val out = temp.filterNotNull().toMutableList()
    out.filterNotNull().forEach {
        out.addAll(it.collectSubExpressions())
    }
    return out
}

private fun ObjJExpr.collectNameElements() : List<ObjJVariableName> {
    val expressions = collectSubExpressions() + this
    val temp = mutableListOf<ObjJVariableName?>()
    expressions.forEach { expr ->
        temp.add(expr.leftExpr?.derefExpression?.variableName)
        temp.add(expr.leftExpr?.refExpression?.variableName)
        temp.addAll(expr.leftExpr?.qualifiedReference?.qualifiedNamesList?.mapNotNull { it as? ObjJVariableName }
                ?: listOf<ObjJVariableName>())
        for (rightExpr in rightExprList) {
            temp.addAll(rightExpr.qualifiedReferencePrime?.variableNameList.orEmpty())
        }
    }
    return temp.filterNotNull()
}

private fun ObjJExpr.collectFunctionDeclarations() : List<ObjJFunctionDeclarationElement<*>> {
    val expressions = collectSubExpressions() + this
    val temp = mutableListOf<ObjJFunctionDeclarationElement<*>?>()
    expressions.forEach { expr ->
        temp.add(expr.leftExpr?.functionCall?.parentFunctionDeclaration)
        temp.addAll(expr.leftExpr?.qualifiedReference?.functionCallList?.mapNotNull{ it.parentFunctionDeclaration}.orEmpty())
    }
    return temp.filterNotNull()
}
*/


class MyModificationTracker:ModificationTracker {
    private var myCount: Long = 0
    internal var tagList:TagList = TagList()
    override fun getModificationCount(): Long {
        return myCount + (tagList.tags.max().orElse(0))
    }
    fun tagged(tag:Long, setTag:Boolean = true) : Boolean {
        return tagList.tagged(tag, setTag)
    }

    val tag:Long get() = tagList.tags.max() ?: createTag()

    fun tick() {
        myCount++
    }
}
