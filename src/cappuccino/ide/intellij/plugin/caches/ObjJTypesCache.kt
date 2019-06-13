package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.inferMethodCallType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

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
        temp.addAll(expr.leftExpr?.qualifiedReference?.qualifiedNameParts?.mapNotNull { it as? ObjJVariableName }
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

class ObjJVariableNameCache(variableName:ObjJVariableName) {
    private val resolvedCache:CachedValue<ObjJVariableName?>
    private val possibleMethodsCache:CachedValue<List<ObjJMethodHeaderDeclaration<*>>>
    private val classTypesCache:CachedValue<InferenceResult?>

    init {
        val manager:CachedValuesManager = CachedValuesManager.getManager(variableName.project)
        val myTreeChangeTracker = MyModificationTracker()
        val dependencies:Array<Any> = listOf(myTreeChangeTracker).toTypedArray()
        resolvedCache = createResolvedVariableNameCache(variableName, manager, dependencies)
        classTypesCache = createClassTypesCachedValue(variableName, manager, dependencies)
        possibleMethodsCache = createPossibleMethodsCache(variableName, manager, dependencies)
    }

    private fun createResolvedVariableNameCache(variableName:ObjJVariableName, manager:CachedValuesManager, dependencies:Array<Any>)  : CachedValue<ObjJVariableName?> {
        val provider = CachedValueProvider<ObjJVariableName?> {
            val resolved = variableName.reference.resolve(false) as? ObjJVariableName
            CachedValueProvider.Result.create(resolved, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private fun createClassTypesCachedValue(variableName:ObjJVariableName, manager:CachedValuesManager, dependencies:Array<Any>)  : CachedValue<InferenceResult?> {
        val provider = CachedValueProvider {
            CachedValueProvider.Result.create(inferredTypes, dependencies)
        }
        return manager.createCachedValue(provider)
    }

    private val inferredTypes:InferenceResult? get() {
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

    private fun getAllMethods(variableName: ObjJVariableName) : List<ObjJMethodHeaderDeclaration<*>> {
        val project = variableName.project
        val classes = (classTypesCache.value ?: inferredTypes)?.toClassList(null)?.withoutAnyType()?.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project)
        }.orEmpty()
        if (classes.isEmpty())
            return emptyList()
        return classes.flatMap {
            ObjJClassMethodIndex.instance[it, project]
        } + ObjJPsiImplUtil.respondsToSelectors(variableName)
    }


}



class MyModificationTracker:ModificationTracker {
    internal var myCount: Long = 0
    override fun getModificationCount(): Long {
        return myCount
    }
}
