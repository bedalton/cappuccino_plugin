package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.utils.afterLast
import cappuccino.ide.intellij.plugin.utils.put
import cappuccino.ide.intellij.plugin.utils.trimFromBeginning

data class NamespaceKeySet(val keys:List<NSComponent>) {
    private val keyMap:Map<NSComponent, MutableList<NSComponent>> by lazy {
        val out = mutableMapOf<NSComponent, MutableList<NSComponent>>()
        for(key in keys) {
            out.put(key.enclosingNamespace, key)
        }
        out
    }
}

data class NamespaceKeySetNode internal constructor(val key:NSComponent = "", val children:List<NamespaceKeySetNode>)

private fun NamespaceKeySet.collapse() : NamespaceKeySetNode {
    val level = 0
    val maxLevel = keys.map { it.split(NAMESPACE_SPLITTER_REGEX).size }.max() ?: return Name
    for (level)
}

private typealias NSComponent = String

private val NSComponent.enclosingNamespace:String
    get() {
        return when {
            !(this.contains(".")) -> this
            this.startsWith(".") -> this.trimFromBeginning(".").afterLast(".")
            else -> this.afterLast(".")
        }
    }

