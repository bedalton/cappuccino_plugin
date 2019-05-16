package cappuccino.ide.intellij.plugin.jstypedef.psi.types

import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes.*

class JsTypeDefTypeFactory {
    companion object {
        @JvmStatic
        fun factory(name: String): IElementType {
            when (name) {
                "JS_MODULE" -> return JS_MODULE
                "JS_FUNCTION" -> return JS_FUNCTION
                "JS_KEYS_LIST" -> return JS_KEY_LIST
                "JS_TYPE_MAP" -> return JS_TYPE_MAP
                else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
            }
        }
    }
}
