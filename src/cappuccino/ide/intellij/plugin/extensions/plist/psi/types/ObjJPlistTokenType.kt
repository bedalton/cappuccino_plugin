package cappuccino.ide.intellij.plugin.extensions.plist.psi.types

import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.extensions.plist.ObjJPlistLanguage

class ObjJPlistTokenType(debug: String) : IElementType(debug, ObjJPlistLanguage.INSTANCE) {
    override fun toString(): String {
        return "PlistTokenType." + super.toString()
    }
}
