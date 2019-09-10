package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey

@Suppress("MemberVisibilityCanBePrivate")
class ObjJVariableNameByScopeIndex private constructor() : ObjJStringStubIndexBase<ObjJVariableName>() {

    override val indexedElementClass: Class<ObjJVariableName>
        get() = ObjJVariableName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJVariableName> {
        return KEY
    }

    fun getInRange(fileName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRange(fileName, textRange, null, project)
    }

    fun getInRange(fileName: String, textRange: TextRange, searchScope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        return getInRange(fileName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRange(fileName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        val queryKey = getIndexKey(fileName, elementStartOffset, elementEndOffset)
        return get(queryKey, project, scopeOrDefault(scope, project))
    }

    companion object {
        val instance = ObjJVariableNameByScopeIndex()
        val KEY = IndexKeyUtil.createIndexKey(ObjJVariableNameByScopeIndex::class.java)
        private const val KEY_FORMAT = "%s-%s-%s"
        private const val VERSION = 3
        fun getIndexKey(fileName: String, blockRange: Pair<Int, Int>): String {
            return getIndexKey(fileName, blockRange.getFirst(), blockRange.getSecond())
        }

        fun getIndexKey(fileName: String?, startOffset: Int, endOffset: Int): String {
            return getIndexKey(fileName, startOffset.toString() + "", endOffset.toString() + "")
        }

        private fun getIndexKey(fileName: String?, startOffset: String, endOffset: String): String {
            return String.format(KEY_FORMAT, fileName, startOffset, endOffset)
        }
    }
}
