package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJPropertyName
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey

class ObjJPropertyNamesIndex : ObjJStringStubIndexBase<ObjJPropertyName>() {

    override val indexedElementClass: Class<ObjJPropertyName>
        get() = ObjJPropertyName::class.java

    override fun getKey(): StubIndexKey<String, ObjJPropertyName> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }
    override fun get(keyString: String, project: Project, scope: GlobalSearchScope): List<ObjJPropertyName> {
        return get(keyString.split("\\.".toRegex()), project, scope)
    }

    fun get(namespaceComponents: List<String>, project: Project, scope: GlobalSearchScope): List<ObjJPropertyName> {
        val lastIndex = namespaceComponents.size - 1

        for (i in 0 .. lastIndex) {
            val namespace = namespaceComponents.subList(i, lastIndex).joinToString(".")
            val result = get(namespace, project, scope)
            if (result.isNotNullOrEmpty())
                return result
        }
        return emptyList()
    }

    companion object {

        private const val VERSION = 1
        val instance = ObjJPropertyNamesIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJPropertyName>(ObjJPropertyNamesIndex::class.java)
    }
}
