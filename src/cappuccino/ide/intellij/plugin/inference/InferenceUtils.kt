package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJIndexService
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import com.intellij.openapi.util.Key
import java.util.*


internal val INFERENCE_LOOP_TAG = Key<Long>("objj.userdata.keys.INFERENCE_LOOP_TAG")

internal val INFERRED_TYPES_USER_DATA_KEY = Key<InferenceResult>("objj.userdata.keys.INFERRED_TYPES")

internal val INFERRED_TYPES_VERSION_USER_DATA_KEY = Key<Int>("objj.userdata.keys.INFERRED_TYPES_VERSION")

private val INFERRED_TYPES_MINOR_VERSION = Random().nextInt() * Random().nextInt() // 0

private val INFERRED_TYPES_VERSION = 1 + INFERRED_TYPES_MINOR_VERSION + ObjJIndexService.INDEX_VERSION


/**
 * Gets the cached types values for the given element
 * This should save computation time, but results are uncertain
 */
internal fun <T: ObjJCompositeElement> T.getCachedInferredTypes(getIfNull:(()->InferenceResult?)? = null) : InferenceResult? {
    val inferredVersionNumber = this.getUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY)
    if (inferredVersionNumber == INFERRED_TYPES_VERSION) {
        val inferredTypes = this.getUserData(INFERRED_TYPES_USER_DATA_KEY)
        if (inferredTypes?.classes.orEmpty().isNotEmpty()) {
            LOGGER.info("Got Cached Values: ${inferredTypes!!.toClassList()}")
            return inferredTypes
        }
    }
    val inferredTypes = getIfNull?.invoke()
    if (inferredTypes?.toClassList().isNullOrEmpty()) {
        LOGGER.info("getCachedInferredTypes(): Failed to get types if null on element: <${this.text}>")
        return null
    }
    LOGGER.info("Got inferred types on null")
    this.putUserData(INFERRED_TYPES_USER_DATA_KEY, inferredTypes)
    this.putUserData(INFERRED_TYPES_VERSION_USER_DATA_KEY, INFERRED_TYPES_VERSION)
    return inferredTypes
}

internal fun createTag():Long {
    return Date().time
}

/**
 * Returns true if this item has already been seen this loop
 */
internal fun ObjJCompositeElement.tagged(tag:Long):Boolean {
    val currentTag = this.getUserData(INFERENCE_LOOP_TAG)
    if (currentTag == tag) {
        LOGGER.info("Element(${this.text})'s current tag matches loop tag <$tag>")
        return true
    }
    LOGGER.info("Element(${this.text})'s CurrentTag($currentTag) != New Tag($tag)")
    this.putUserData(INFERENCE_LOOP_TAG, tag)
    return false
}