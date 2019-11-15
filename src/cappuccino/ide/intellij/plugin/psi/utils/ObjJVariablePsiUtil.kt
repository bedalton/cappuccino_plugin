package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import java.util.*

object ObjJVariablePsiUtil {

    fun toString(variableName: ObjJVariableName): String {
        return "ObjJ_VAR_NAME(" + variableName.text + ")"
    }

    fun isNewVariableDec(psiElement: PsiElement): Boolean {
        val variableName = psiElement.getSelfOrParentOfType(ObjJVariableName::class.java)
        if (variableName != null) {
            return when {
                variableName.parent is ObjJGlobalVariableDeclaration -> true
                variableName.parent.parent is ObjJVariableDeclaration -> (variableName.parent.parent as ObjJVariableDeclaration).hasVariableKeyword()
                variableName.parent is ObjJFormalParameterArg -> true
                variableName.parent is ObjJMethodDeclarationSelector -> true
                else -> false
            }
        }
        return false
    }

    fun getVariableType(variable: ObjJGlobalVariableDeclaration): String? {
        val stub = variable.stub
        if (stub?.variableType?.isEmpty() == true) {
            return stub.variableType
        }
        return null
    }

    fun getVariableType(variable:ObjJInstanceVariableDeclaration) : String {
        return variable.stub?.variableType
                ?: variable.formalVariableType.variableTypeId?.className?.text
                ?: variable.formalVariableType.text
    }

    internal fun respondsToSelectors(variableName: ObjJVariableName): List<ObjJSelectorLiteral> {
        return variableName.respondsToSelectorsCache {
            internalRespondsToSelectors(variableName)
        }
    }

    private fun internalRespondsToSelectors(variableName:ObjJVariableName) : List<ObjJSelectorLiteral> {
        return ReferencesSearch.search(variableName).findAll().mapNotNull {
            val methodCall = it.element.getParentOfType(ObjJMethodCall::class.java)
                    ?: return@mapNotNull null
            if (methodCall.selectorString != "respondsToSelector:")
                return@mapNotNull null

            val expr = methodCall.qualifiedMethodCallSelectorList.getOrNull(0)?.exprList?.getOrNull(0) ?:
            return@mapNotNull null
            expr.leftExpr?.selectorLiteral
        }
    }

    internal fun respondsToSelectorStrings(variableName: ObjJVariableName): Set<String> {
        return respondsToSelectors(variableName).map { it.selectorString }.filterNot { it.isBlank() }.toSet()
    }

    internal fun respondsToSelector(variableName: ObjJVariableName, selector:String): Boolean {
        val selectorStrings = respondsToSelectorStrings(variableName)
        return selector in selectorStrings
    }
}

private val LAST_CACHE_TIME = Key<Long>("objj.responds-to-selector.CACHE_TIME")
private val RESPONDS_TO_SELECTOR_CACHE_KEY = Key<List<SmartPsiElementPointer<ObjJSelectorLiteral>>>("objj.responds-to-selector.CACHE_KEY")

private object StatusFileChangeListener: PsiTreeAnyChangeAbstractAdapter() {
    internal var didAddListener = false

    private var internalTimeSinceLastFileChange = Long.MIN_VALUE

    val timeSinceLastFileChange get() = internalTimeSinceLastFileChange


    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile)
            return
        internalTimeSinceLastFileChange = now
    }

    internal fun addListenerToProject(project: Project) {
        if (didAddListener)
            return
        didAddListener = true
        PsiManager.getInstance(project).addPsiTreeChangeListener(this)
    }
}

fun addStatusFileChangeListener(project: Project)
        = StatusFileChangeListener.addListenerToProject(project)


internal fun ObjJVariableName.respondsToSelectorsCache(onNull:((PsiElement)->List<ObjJSelectorLiteral>)?) : List<ObjJSelectorLiteral> {
    addStatusFileChangeListener(project)
    val now = Date().time
    val timeSinceCache = now - getUserData(LAST_CACHE_TIME).orElse(0)
    val timeSinceChange = now - StatusFileChangeListener.timeSinceLastFileChange
    if (timeSinceCache < 5000 && timeSinceChange < 4000) {
        val cachedRespondsToSelector = getUserData(RESPONDS_TO_SELECTOR_CACHE_KEY)
        if (cachedRespondsToSelector != null) {
            return cachedRespondsToSelector.mapNotNull {
                it.element
            }
        }
    }
    val selectors = onNull?.invoke(this) ?: return emptyList()
    val toCache = selectors.map {
        SmartPointerManager.createPointer(it)
    }
    putUserData(RESPONDS_TO_SELECTOR_CACHE_KEY, toCache)
    putUserData(LAST_CACHE_TIME, now)
    return selectors
}