package cappuccino.ide.intellij.plugin.jstypedef.stubs

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefArrayType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefType

val List<JsTypeDefType>.arrayTypes:List<JsTypeDefArrayType> get() {
    return this.mapNotNull { it.arrayType }.map { it. }
}