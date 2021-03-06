package cappuccino.ide.intellij.plugin.jstypedef.psi.types

import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes.*
import com.intellij.psi.tree.IElementType

class JsTypeDefTypeFactory {
    companion object {
        @JvmStatic
        fun factory(name: String): IElementType {
            return when (name) {
                "JS_CLASS_ELEMENT" -> JS_CLASS
                "JS_FUNCTION" -> JS_FUNCTION
                "JS_INTERFACE_ELEMENT" -> JS_INTERFACE
                "JS_KEY_LIST" -> JS_KEY_LIST
                "JS_MODULE" -> JS_MODULE
                "JS_MODULE_NAME" -> JS_MODULE_NAME
                "JS_PROPERTY" -> JS_PROPERTY
                "JS_TYPE_ALIAS" -> JS_TYPE_ALIAS
                "JS_TYPE_MAP_ELEMENT" -> JS_TYPE_MAP
                "JS_VARIABLE_DECLARATION" -> JS_VARIABLE_DECLARATION
                else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
            }
        }
    }
}
