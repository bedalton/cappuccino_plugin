package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import java.util.*


fun addStatusFileChangeListener(project: Project)
        = StatusFileChangeListener.addListenerToProject(project)


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

private val RESOLVE_CACHE_KEY = Key<SmartPsiElementPointer<PsiElement>>("objj.resolve.CACHE_KEY")
private val LAST_CACHE_TIME = Key<Long>("objj.resolve.LAST_CACHE_TIME_KEY")

internal fun ObjJUniversalPsiElement.resolveFromCache(onNull:((PsiElement)->PsiElement?)?) : PsiElement? {
    addStatusFileChangeListener(project)
    val now = Date().time
    val timeSinceCache = now - getUserData(LAST_CACHE_TIME).orElse(0)
    val timeSinceChange = now - StatusFileChangeListener.timeSinceLastFileChange
    if (timeSinceCache < 5000 && timeSinceChange < 4000) {
        val cachedFile = getUserData(RESOLVE_CACHE_KEY)?.element
        if (cachedFile != null) {
            return cachedFile
        }
    }
    val resolvedElement = onNull?.invoke(this) ?: return null
    val smartPointer = SmartPointerManager.createPointer(resolvedElement)
    putUserData(RESOLVE_CACHE_KEY, smartPointer)
    putUserData(LAST_CACHE_TIME, now)
    return resolvedElement
}
