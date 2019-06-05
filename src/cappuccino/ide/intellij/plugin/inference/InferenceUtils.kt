package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.orElse
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter
import java.util.*
import kotlin.math.abs


internal val INFERENCE_LAST_RESOLVED = Key<Long>("objj.userdata.keys.INFERENCE_LAST_RESOLVED")

internal val INFERRED_TYPES_USER_DATA_KEY = Key<InferenceResult>("objj.userdata.keys.INFERRED_TYPES")

internal val INFERRED_TYPES_VERSION_USER_DATA_KEY = Key<Int>("objj.userdata.keys.INFERRED_TYPES_VERSION")

private val INFERRED_TYPES_MINOR_VERSION = Random().nextInt() * Random().nextInt() // 0

private val INFERRED_TYPES_VERSION = 1 + INFERRED_TYPES_MINOR_VERSION + ObjJIndexService.INDEX_VERSION

private val INFERRED_TYPES_IS_ACCESSING = Key<Boolean>("objj.userdata.keys.INFERRED_TYPES_IS_ACCESSING")


fun addStatusFileChangeListener(project:Project)
    = StatusFileChangeListener.addListenerToProject(project)


private object StatusFileChangeListener: PsiTreeAnyChangeAbstractAdapter() {
    internal var didAddListener = false

    private var internalTimeSinceLastFileChange = Long.MIN_VALUE

    val timeSinceLastFileChange get() = internalTimeSinceLastFileChange


    override fun onChange(file: PsiFile?) {
        if (file !is ObjJFile)
            return
        internalTimeSinceLastFileChange = Date().time
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
internal fun <T: ObjJCompositeElement> T.getCachedInferredTypes(getIfNull:(()->InferenceResult?)? = null) : InferenceResult? {
    //if (this.getUserData(INFERRED_TYPES_IS_ACCESSING).orFalse())
      //  return null;
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, true)
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    val timeSinceTag = StatusFileChangeListener.timeSinceLastFileChange - this.getUserData(INFERENCE_LAST_RESOLVED).orElse(-1)
    if (inferredVersionNumber == INFERRED_TYPES_VERSION && timeSinceTag > 0 && timeSinceTag < 5000) {
        val inferredTypes = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferredTypes != null) {
            return inferredTypes
        }
    }
    val inferredTypes = getIfNull?.invoke() ?: INFERRED_EMPTY_TYPE
    this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
    this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
    this.putUserData(INFERRED_TYPES_IS_ACCESSING, false)
    this.putUserData(INFERENCE_LAST_RESOLVED, StatusFileChangeListener.timeSinceLastFileChange)
    return inferredTypes
}

internal fun createTag():Long {
    return Date().time
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun ObjJCompositeElement.tagged(tag:Long):Boolean {
    val currentTag = this.getUserData(INFERENCE_LAST_RESOLVED)
    if (currentTag == tag) {
        //LOGGER.info("Element(${this.text})'s current tag matches loop tag <$tag>")
        return true
    }
    //LOGGER.info("Element(${this.text})'s CurrentTag($currentTag) != New Tag($tag)")
    this.putUserData(INFERENCE_LAST_RESOLVED, tag)
    return false
}