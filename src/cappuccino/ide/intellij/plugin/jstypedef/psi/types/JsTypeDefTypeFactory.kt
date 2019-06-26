package cappuccino.ide.intellij.plugin.jstypedef.psi.types

import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes.*

class JsTypeDefTypeFactory {
    companion object {
        @JvmStatic
        fun factory(name: String): IElementType {
            return when (name) {
                "JS_FUNCTION" -> JS_FUNCTION
                "JS_KEYS_LIST" -> JS_KEY_LIST
                "JS_MODULE" -> JS_MODULE
                "JS_MODULE_NAME" -> JS_MODULE_NAME
                "JS_PROPERTY" -> JS_PROPERTY
                "JS_TYPE_MAP" -> JS_TYPE_MAP
                "JS_VARIABLE_DECLARATION" -> JS_VARIABLE_DECLARATION
                else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
            }
        }
    }
}
