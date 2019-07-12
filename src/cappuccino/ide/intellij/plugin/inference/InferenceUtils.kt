package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
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
    private var lastTag = now

    val timeSinceLastFileChange:Long
        get() {
            val now = now
            if (now - internalTimeSinceLastFileChange > CACHE_EXPIRY * 3) {
                LOGGER.info("TimeDif: ${now - internalTimeSinceLastFileChange}")
                internalTimeSinceLastFileChange = now
            }
            return internalTimeSinceLastFileChange
        }

    internal val nextTag : Long get() {
        return timeSinceLastFileChange
    }

    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile && file !is JsTypeDefFile)
            return
        LOGGER.info("FILE CHANGED")
        internalTimeSinceLastFileChange = now
    }

    internal fun addListenerToProject(project: Project) {
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
internal fun <T: PsiElement> T.getCachedInferredTypes(tag:Long?, setTag:Boolean = true, getIfNull:(()->InferenceResult?)? = null) : InferenceResult? {
    //if (this.getUserData(INFERRED_TYPES_IS_ACCESSING).orFalse())
      //  return null;
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, true)
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    val lastTagged:Long = lastTagged
    val timeSinceTag = max(StatusFileChangeListener.timeSinceLastFileChange, lastTagged) - min(StatusFileChangeListener.timeSinceLastFileChange,lastTagged)

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
    val tagged = tag != null && tagged(tag, setTag)
    if (inferredVersionNumber == INFERRED_TYPES_VERSION && (timeSinceTag < CACHE_EXPIRY || tagged) && textMatches) {
        val inferredTypes = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferredTypes != null || tagged) {
            return inferredTypes
        }
    }
    if (tagged && !textMatches) {
        LOGGER.info("Tagged but text <${this.text}> != <$lastText>")
    }

    val inferredTypes = getIfNull?.invoke() ?: INFERRED_EMPTY_TYPE
    this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
    this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, false)
    this.putUserData(INFERENCE_LAST_RESOLVED, now)
    return inferredTypes
}

internal fun createTag():Long {
    return StatusFileChangeListener.nextTag
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun PsiElement.tagged(tag:Long?, setTag: Boolean = true):Boolean {
    if (tag == null)
        return false

    val currentTag = lastTagged
    LOGGER.info("Tag<$tag>; LastTag<$currentTag>; Diff: <${tag - currentTag}>;")
    if(currentTag <= tag)
        return true;
    if (setTag)
        this.putUserData(INFERENCE_LAST_RESOLVED, tag)
    return false;
}

private val PsiElement.lastTagged:Long get() {
    var lastTagged = this.getUserData(INFERENCE_LAST_RESOLVED)
    if (lastTagged == null) {
        lastTagged = 0
        this.putUserData(INFERENCE_LAST_RESOLVED, lastTagged)
    }
    return lastTagged
}