package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJBlockElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter

internal val INFERENCE_TAG_LIST = Key<TagList>("objj.userdata.keys.INFERENCE_TAG_LIST")

internal val INFERRED_TYPES_USER_DATA_KEY = Key<InferenceResult>("objj.userdata.keys.INFERRED_TYPES")

internal val INFERRED_TYPES_VERSION_USER_DATA_KEY = Key<Int>("objj.userdata.keys.INFERRED_TYPES_VERSION")

private val INFERRED_TYPES_LAST_TEXT = Key<String>("objj.userdata.keys.INFERRED_TYPES_LAST_TEXT")

private const val INFERRED_TYPES_MINOR_VERSION: Int = 0

private const val INFERRED_TYPES_VERSION = 2 + INFERRED_TYPES_MINOR_VERSION + ObjJIndexService.INDEX_VERSION

private const val CACHE_EXPIRY = 18000

private const val TEXT_DEPTH = 3

fun addStatusFileChangeListener(project: Project) = StatusFileChangeListener.addListenerToProject(project)


private object StatusFileChangeListener : PsiTreeAnyChangeAbstractAdapter() {
    internal var didAddListener = false

    private var internalTimeSinceLastFileChange = now
    private var lastTag = now

    val timeSinceLastFileChange: Long
        get() {
            if (internalTimeSinceLastFileChange - lastTag > CACHE_EXPIRY) {
                lastTag = internalTimeSinceLastFileChange
            }
            return lastTag
        }

    internal val nextTag: Long
        get() {
            return timeSinceLastFileChange
        }

    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile && file !is JsTypeDefFile)
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


/**
 * Gets the cached types values for the given element
 * This should save computation time, but results are uncertain
 */
internal fun <T : PsiElement> T.getCachedInferredTypes(tag: Tag?, getIfNull: (() -> InferenceResult?)? = null): InferenceResult? {
    //if (this.getUserData(INFERRED_TYPES_IS_ACCESSING).orFalse())
    //  return null;
    //val marks = this.getUserData(INFERRED_TYPES_IS_ACCESSING).orElse(0)
    //this.putUserData(INFERRED_TYPES_IS_ACCESSING, marks + 1)
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    // Establish and store last text

    val textParent: PsiElement = this.getParentOfType(ObjJBlock::class.java)?.let {
        if (this.parent is ObjJBlock)
            it.parent
        else
            it
    } ?: this.parent?.parent?.parent ?: this.parent?.parent ?: this.parent ?: this
    val rangeStart = textParent.textRange?.startOffset ?: this.parent.textRange.startOffset
    val rangeEnd = this.textRange.endOffset
    // Prevent being affected by self and later elements
    val thisText = if (rangeStart != rangeEnd) {
        val containingFile = containingFile ?: originalElement?.containingFile
        containingFile?.text?.substring(rangeStart, rangeEnd) ?: textParent.text
    } else {
        textParent.text
    }
    val lastText = this.getUserData(INFERRED_TYPES_LAST_TEXT).orElse("__#__")
    val textIsUnchanged = lastText == thisText
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
    if (tag != null && --tag.depth < 0)
        return null
    //if (marks > MAXIMUM_ACCESS_MARKS)
    //LOGGER.warning(.info("Reached max marks. $marks/$MAXIMUM_ACCESS_MARKS")//return null
    try {
        val inferredTypes = getIfNull?.invoke() ?: this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
        this.tagComplete(tag)
        this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
        this.putUserData(INFERRED_TYPES_LAST_TEXT, thisText)
        return inferredTypes
    } catch (e: Exception) {
        this.clearTag(tag)
        throw e
    }
}

class Tag(val tag: Long, var depth: Int = ObjJPluginSettings.inferenceMaxDepth) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false

        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}

internal fun createTag(offset:Int = 0): Tag {
    return Tag(StatusFileChangeListener.nextTag + offset)
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun PsiElement.tagged(tag: Tag?, setTag: Boolean = true): Boolean {
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
internal fun PsiElement.clearTag(tag: Tag?): Boolean {
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
internal fun PsiElement.tagComplete(tag: Tag?): Boolean {
    if (tag == null)
        return false
    val tagList = getUserData(INFERENCE_TAG_LIST) ?: TagList()
    tagList.tagCompleted(tag)
    putUserData(INFERENCE_TAG_LIST, tagList)
    return false
}

private const val TAG_LIST_LENGTH = 6

internal data class TagList(var tags: Set<Tag> = setOf(), var completed: MutableSet<Tag> = mutableSetOf()) {
    fun tagged(tag: Tag, setTag: Boolean): Boolean {
        if (tag in tags) {
            return true
        }
        if (!setTag)
            return false
        val newTags = tags.toMutableSet()
        if (tags.size > TAG_LIST_LENGTH)
            oldestTag?.let { newTags.remove(it) }
        newTags.add(tag)
        tags = newTags
        return false
    }

    fun clearTag(tag: Tag) {
        tags = tags.filterNot { it == tag }.toSet()
    }

    fun tagCompleted(tag: Tag) {
        completed.add(tag)
    }

    private val oldestTag: Tag?
        get() = tags.minBy { it.tag }
}

private val PsiElement.isTextUnchanged: Boolean
    get() {
        val thisText = this.parent?.text ?: this.text
        val lastText = this.getUserData(INFERRED_TYPES_LAST_TEXT).orElse("")
        this.putUserData(INFERRED_TYPES_LAST_TEXT, thisText)
        return lastText == text
    }