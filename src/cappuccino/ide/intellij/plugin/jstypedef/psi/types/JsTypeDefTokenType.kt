package cappuccino.ide.intellij.plugin.jstypedef.psi.types

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import com.intellij.psi.tree.IElementType

class JsTypeDefTokenType(debug: String) : IElementType(debug, JsTypeDefLanguage.instance)
