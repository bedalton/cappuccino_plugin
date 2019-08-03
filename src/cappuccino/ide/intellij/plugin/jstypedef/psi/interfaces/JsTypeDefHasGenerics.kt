package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListGenericType

interface JsTypeDefHasGenerics : JsTypeDefElement {
    val genericsKeys:Set<JsTypeListGenericType>?
}