package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter

private const val MAXIMUM_ACCESS_MARKS = 10

internal val INFERENCE_TAG_LIST = Key<TagList>("objj.userdata.keys.INFERENCE_TAG_LIST")

internal val INFERRED_TYPES_USER_DATA_KEY = Key<InferenceResult>("objj.userdata.keys.INFERRED_TYPES")

internal val INFERRED_TYPES_VERSION_USER_DATA_KEY = Key<Int>("objj.userdata.keys.INFERRED_TYPES_VERSION")

private const val INFERRED_TYPES_MINOR_VERSION = 0

private const val INFERRED_TYPES_VERSION = 1 + INFERRED_TYPES_MINOR_VERSION + ObjJIndexService.INDEX_VERSION

private val INFERRED_TYPES_IS_ACCESSING = Key<Int>("objj.userdata.keys.INFERRED_TYPES_IS_ACCESSING")

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
            if (internalTimeSinceLastFileChange - lastTag > CACHE_EXPIRY)
                lastTag = internalTimeSinceLastFileChange
            return lastTag
        }

    internal val nextTag : Long get() {
        return timeSinceLastFileChange
    }

    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile && file !is JsTypeDefFile)
            return
        //LOGGER.info("FILE CHANGED")
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
internal fun <T: PsiElement> T.getCachedInferredTypes(tag: Long?, getIfNull: (() -> InferenceResult?)? = null) : InferenceResult? {
    //if (this.getUserData(INFERRED_TYPES_IS_ACCESSING).orFalse())
      //  return null;
    //val marks = this.getUserData(INFERRED_TYPES_IS_ACCESSING).orElse(0)
    //this.putUserData(INFERRED_TYPES_IS_ACCESSING, marks + 1)
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    // Establish and store last text

    val textIsUnchanged = isTextUnchanged
    // Check cache without tagging
    if (tag == null && textIsUnchanged) {
        val inferred = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferred != null)
            return inferred
    }
    val tagged = tag != null && tagged(tag, false)
    if (inferredVersionNumber == INFERRED_TYPES_VERSION && tagged && textIsUnchanged) {
        val inferredTypes = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferredTypes != null || tagged) {
            return inferredTypes
        }
    }
    //if (marks > MAXIMUM_ACCESS_MARKS)
      //LOGGER.warning(.info("Reached max marks. $marks/$MAXIMUM_ACCESS_MARKS")//return null
    try {
        val inferredTypes = getIfNull?.invoke() ?: this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
        this.tagComplete(tag)
        //this.putUserData(INFERRED_TYPES_IS_ACCESSING, marks - 1)
        this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
        return inferredTypes
    } catch (e:Exception) {
        this.clearTag(tag)
        throw e
    }
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
    val tagList = getUserData(INFERENCE_TAG_LIST) ?: TagList()
    if (tagList.tagged(tag, setTag))
        return true
    putUserData(INFERENCE_TAG_LIST, tagList)
    return false
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun PsiElement.clearTag(tag:Long?):Boolean {
    if (tag == null)
        return false
    val tagList = getUserData(INFERENCE_TAG_LIST) ?: TagList()
    tagList.clearTag(tag)
    putUserData(INFERENCE_TAG_LIST, tagList)
    return false
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun PsiElement.tagComplete(tag: Long?):Boolean {
    if (tag == null)
        return false
    val tagList = getUserData(INFERENCE_TAG_LIST) ?: TagList()
    tagList.tagCompleted(tag)
    putUserData(INFERENCE_TAG_LIST, tagList)
    return false
}

private const val TAG_LIST_LENGTH = 6
internal data class TagList(var tags:Set<Long> = setOf(), var completed:MutableSet<Long> = mutableSetOf()) {
    fun tagged(tag:Long, setTag: Boolean) : Boolean {
        if(tag in tags)
            return true
        if (!setTag)
            return false
        val newTags = tags.toMutableSet()
        if (tags.size > TAG_LIST_LENGTH)
            newTags.remove(youngestTag)
        newTags.add(tag)
        tags = newTags
        return false
    }

    fun clearTag(tag:Long) {
        tags = tags.filterNot {it == tag}.toSet()
    }

    fun tagCompleted(tag:Long) {
        completed.add(tag)
    }

    private val youngestTag:Long
        get() = tags.min() ?: 0
}

private val PsiElement.isTextUnchanged:Boolean get() {
    val lastText =  this.getUserData(INFERRED_TYPES_LAST_TEXT).orElse("")
    this.putUserData(INFERRED_TYPES_LAST_TEXT, this.text)
    return lastText == text
}