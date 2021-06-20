package cappuccino.ide.intellij.plugin.psi.types

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.psi.tree.IElementType

open class ObjJElementType(debug: String) : IElementType(debug, ObjJLanguage.instance)
