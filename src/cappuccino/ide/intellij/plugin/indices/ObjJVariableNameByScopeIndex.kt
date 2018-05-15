package cappuccino.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class ObjJVariableNameByScopeIndex private constructor() : ObjJStringStubIndexBase<ObjJVariableName>() {

    protected override val indexedElementClass: Class<ObjJVariableName>
        get() = ObjJVariableName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJVariableName> {
        return KEY
    }

    fun getInFile(fileName: String, project: Project): List<ObjJVariableName> {
        return getInFile(fileName, project, null)
    }

    fun getInFile(fileName: String, project: Project, searchScope: GlobalSearchScope?): List<ObjJVariableName> {
        return get("$fileName-ALL", project, scopeOrDefault(searchScope, project))
    }

    fun getInRange(fileName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRange(fileName, textRange, null, project)
    }

    fun getInRange(fileName: String, textRange: TextRange, searchScope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        return getInRange(fileName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRange(fileName: String, elementStartOffset: Int, elementEndOffset: Int, project: Project): List<ObjJVariableName> {
        return getInRange(fileName, elementStartOffset, elementEndOffset, null, project)
    }

    fun getInRange(fileName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope?, project: Project): List<ObjJVariableName> {
        val queryKey = getIndexKey(fileName, elementStartOffset, elementEndOffset)
        return get(queryKey, project, scopeOrDefault(scope, project))
        //return getAllForKeysFlat(getKeysInRange(fileName, elementStartOffset, elementEndOffset, project), project, scope);
    }

    private fun getKeysInRange(fileName: String, blockStart: Int, blockEnd: Int, project: Project): List<String> {
        return getKeysInRange(fileName, blockStart, blockEnd, null, project)
    }

    private fun getKeysInRange(fileName: String, blockStart: Int, blockEnd: Int, searchScope: GlobalSearchScope?, project: Project): List<String> {
        val keys = ArrayList<String>()
        val queryKey = getIndexKey(fileName, "(\\d+)", "(\\d+)")

        val pattern = Pattern.compile(queryKey)
        var matcher: Matcher
        for (key in getKeysByPattern(queryKey, project)) {
            matcher = pattern.matcher(key)
            if (matcher.groupCount() < 3) {
                continue
            }
            val startOffset = Integer.parseInt(matcher.group(1))
            if (blockStart < startOffset) {
                continue
            }
            val endOffset = Integer.parseInt(matcher.group(2))
            if (endOffset <= blockEnd) {
                keys.add(key)
            }
        }
        return keys
    }

    companion object {
        val instance = ObjJVariableNameByScopeIndex()
        val KEY = IndexKeyUtil.createIndexKey(ObjJVariableNameByScopeIndex::class.java)
        private val KEY_FORMAT = "%s-%s-%s"
        private val VERSION = 3

        fun getIndexKey(block: ObjJBlock): String {
            val fileName = ObjJFileUtil.getContainingFileName(block.containingFile)
            val textRange = block.textRange
            val startOffset = textRange.startOffset
            val endOffset = textRange.endOffset
            return getIndexKey(fileName, startOffset, endOffset)
        }

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
