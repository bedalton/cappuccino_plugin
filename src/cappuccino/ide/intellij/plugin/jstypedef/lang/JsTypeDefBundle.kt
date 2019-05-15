package cappuccino.ide.intellij.plugin.jstypedef.lang

import com.intellij.CommonBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object JsTypeDefBundle {
    @NonNls
    private const val BUNDLE = "cappuccino.ide.intellij.plugin.jstypedef.bundle"
    private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
            CommonBundle.message(bundle, key, *params)
}