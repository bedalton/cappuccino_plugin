package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey

@Suppress("MemberVisibilityCanBePrivate")
class ObjJAssignedVariableNamesByBlockIndex private constructor() : ObjJStringStubIndexBase<ObjJVariableName>() {

    override val indexedElementClass: Class<ObjJVariableName>
        get() = ObjJVariableName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJVariableName> {
        return KEY
    }

    fun getInRangeStrict(fileName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRangeStrict(fileName, textRange, null, project)
    }

    fun getInRangeStrict(fileName: String, textRange: TextRange, searchScope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        return getInRangeStrict(fileName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeStrict(fileName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        val queryKey = getIndexKey(fileName, elementStartOffset, elementEndOffset)
        return get(queryKey, project, scopeOrDefault(scope, project))
    }

    fun getInRangeFuzzy(fileName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(fileName, textRange, null, project)
    }

    fun getInRangeFuzzy(fileName: String, textRange: TextRange, searchScope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(fileName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeFuzzy(fileName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        val keys = filterKeys(fileName, elementStartOffset, elementEndOffset, project)
        val out = mutableListOf<ObjJVariableName>()
        for(key in keys) {
            out.addAll(get(key, project, scopeOrDefault(scope, project)))
        }
        return out
    }

    fun filterKeys(fileName:String, elementStartOffset: Int, elementEndOffset: Int, project: Project) : List<String> {
        return getAllKeys(project).filter {
            val parts = it.split("-")
            if (parts[0] != fileName)
                return@filter false
            val blockStart = parts.getOrNull(1)?.toInt()
                    ?: return@filter false
            if (elementStartOffset < blockStart)
                return@filter false

            val blockEnd = parts.getOrNull(1)?.toInt()
                    ?: return@filter false
            elementEndOffset <= blockEnd
        }
    }

    companion object {
        val instance = ObjJAssignedVariableNamesByBlockIndex()
        val KEY = IndexKeyUtil.createIndexKey(ObjJAssignedVariableNamesByBlockIndex::class.java)
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
