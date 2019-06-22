package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter
import java.util.*
import kotlin.math.max
import kotlin.math.min


internal val INFERENCE_LAST_RESOLVED = Key<Long>("objj.userdata.keys.INFERENCE_LAST_RESOLVED")

internal val INFERRED_TYPES_USER_DATA_KEY = Key<InferenceResult>("objj.userdata.keys.INFERRED_TYPES")

internal val INFERRED_TYPES_VERSION_USER_DATA_KEY = Key<Int>("objj.userdata.keys.INFERRED_TYPES_VERSION")

private val INFERRED_TYPES_MINOR_VERSION = Random().nextInt() * Random().nextInt() // 0

private val INFERRED_TYPES_VERSION = 1 + INFERRED_TYPES_MINOR_VERSION + ObjJIndexService.INDEX_VERSION

private val INFERRED_TYPES_IS_ACCESSING = Key<Boolean>("objj.userdata.keys.INFERRED_TYPES_IS_ACCESSING")

private val INFERRED_TYPES_LAST_TEXT = Key<String>("objj.userdata.keys.INFERRED_TYPES_LAST_TEXT")

private const val CACHE_EXPIRY = 2000

fun addStatusFileChangeListener(project:Project)
    = StatusFileChangeListener.addListenerToProject(project)


private object StatusFileChangeListener: PsiTreeAnyChangeAbstractAdapter() {
    internal var didAddListener = false

    private var internalTimeSinceLastFileChange = now

    val timeSinceLastFileChange get() = internalTimeSinceLastFileChange


    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile)
            return
        internalTimeSinceLastFileChange = now
    }

    internal fun addListenerToProject(project:Project) {
        if (didAddListener)
            return
        didAddListener = true
        PsiManager.getInstance(project).addPsiTreeChangeListener(this)
    }
}

/**
 * Gets the cached types values for the given element
 * This should save computation time, but results are uncertain
 */
internal fun <T: ObjJCompositeElement> T.getCachedInferredTypes(tag:Long?, getIfNull:(()->InferenceResult?)? = null) : InferenceResult? {
    //if (this.getUserData(INFERRED_TYPES_IS_ACCESSING).orFalse())
      //  return null;
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, true)
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    val lastTagged:Long = lastTagged
    val timeSinceTag = max(StatusFileChangeListener.timeSinceLastFileChange,lastTagged) - min(StatusFileChangeListener.timeSinceLastFileChange,lastTagged)

    // Establish and store last text
    val lastText =  this.getUserData(INFERRED_TYPES_LAST_TEXT).orElse("")
    this.putUserData(INFERRED_TYPES_LAST_TEXT, this.text)
    val textMatches = lastText == this.text
    // Check cache without tagging
    if (tag == null && textMatches) {
        val inferred = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferred != null)
            return inferred
    }
    val tagged = tag != null && tagged(tag)
    if (inferredVersionNumber == INFERRED_TYPES_VERSION && (timeSinceTag < CACHE_EXPIRY || tagged) && textMatches) {
        val inferredTypes = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferredTypes != null || tagged) {
            return inferredTypes
        }
    }

    val inferredTypes = getIfNull?.invoke() ?: INFERRED_EMPTY_TYPE
    this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
    this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, false)
    this.putUserData(INFERENCE_LAST_RESOLVED, now)
    return inferredTypes
}

internal fun createTag():Long {
    val now = now
    return if (now - StatusFileChangeListener.timeSinceLastFileChange < CACHE_EXPIRY)
        StatusFileChangeListener.timeSinceLastFileChange
    else
        now
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun ObjJCompositeElement.tagged(tag:Long?):Boolean {
    if (tag == null)
        return false

    val currentTag = lastTagged
    if (currentTag < tag) {
        this.putUserData(INFERENCE_LAST_RESOLVED, tag)
        return true
    }
    return currentTag == tag
}

private val PsiElement.lastTagged:Long get() {
    val now = now
    var lastTagged = this.getUserData(INFERENCE_LAST_RESOLVED)
    if (lastTagged == null) {
        lastTagged = now
        this.putUserData(INFERENCE_LAST_RESOLVED, lastTagged)
    } else if (now - lastTagged < 2000)
        return now + 1
    return lastTagged
}