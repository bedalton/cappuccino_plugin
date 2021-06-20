package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX

interface JsTypeDefHasNamespace : JsTypeDefElement {
    val enclosingNamespace:String
    val enclosingNamespaceComponents:List<String> get() = enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX)
    val namespaceComponents:List<String>
    val namespaceComponent:String
    val namespacedName:String
}