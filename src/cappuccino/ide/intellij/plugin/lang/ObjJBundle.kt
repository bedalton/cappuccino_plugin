package cappuccino.ide.intellij.plugin.lang

import com.intellij.CommonBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object ObjJBundle {
    @NonNls private const val BUNDLE = "cappuccino.ide.intellij.plugin.lang.objective-j-bundle"
    private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
            CommonBundle.message(bundle, key, *params)
}