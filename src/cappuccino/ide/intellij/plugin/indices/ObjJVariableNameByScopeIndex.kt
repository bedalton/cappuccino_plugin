package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.AbstractStubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

@Suppress("MemberVisibilityCanBePrivate")
class ObjJVariableNameByScopeIndex private constructor() : AbstractStubIndex<ObjJRangeKey, ObjJVariableName>() {

    override fun getVersion(): Int {
        return ObjJStubVersions.SOURCE_STUB_VERSION + VERSION
    }

    override fun getKey(): StubIndexKey<ObjJRangeKey, ObjJVariableName> {
        return KEY
    }

    fun getInRangeStrict(file: PsiFile, textRange: TextRange, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getAllKeys(project)
                .flatMap {
                    getInRangeStrict(it.variableName, textRange, fileScope, project)
                }
    }

    fun getInRangeStrict(file: PsiFile, variableName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getInRangeStrict(variableName, textRange, fileScope, project)
    }

    fun getInRangeStrict(variableName: String, textRange: TextRange, searchScope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        return getInRangeStrict(variableName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeStrict(variableName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        val queryKey = ObjJRangeKey(variableName, elementStartOffset, elementEndOffset)
        return get(queryKey, project, scopeOrDefault(scope, project)).toList()
    }

    /*

    fun getInRangeFuzzy(file:PsiFile, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(file, textRange.startOffset, textRange.endOffset, project)
    }

    fun getInRangeFuzzy(file:PsiFile, startOffset: Int, endOffset:Int, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getAll(project, fileScope).filter {
            val variableTextRange = it.textRange
            variableTextRange.startOffset >= startOffset && variableTextRange.endOffset <= endOffset
        }
    }

    fun getInRangeFuzzy(variableName: String, textRange: TextRange, searchScope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(variableName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeFuzzy(variableName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        val keys = filterKeys(variableName, elementStartOffset, elementEndOffset, project)
        val out = mutableListOf<ObjJVariableName>()
        for (key in keys) {
            out.addAll(get(key, project, scopeOrDefault(scope, project)))
        }
        return out
    }

    fun filterKeys(variableName: String, elementStartOffset: Int, elementEndOffset: Int, project: Project): List<ObjJRangeKey> {
        return getAllKeys(project).filter filter@{
            if (variableName != it.variableName)
                return@filter false
            if (elementStartOffset < it.start)
                return@filter false
            elementEndOffset <= it.end
        }
    }
     */

    companion object {
        val instance = ObjJVariableNameByScopeIndex()
        val KEY = IndexKeyUtil.createIndexKey(ObjJVariableNameByScopeIndex::class.java)
        private const val KEY_FORMAT = "%s:%s-%s"
        private const val VERSION = 1
        fun getIndexKey(fileName: String, blockRange: Pair<Int, Int>): String {
            return getIndexKey(fileName, blockRange.getFirst(), blockRange.getSecond())
        }

        fun getIndexKey(fileName: String?, startOffset: Int, endOffset: Int): String {
            return getIndexKey(fileName, startOffset.toString() + "", endOffset.toString() + "")
        }

        private fun getIndexKey(fileName: String?, startOffset: String, endOffset: String): String {
            return String.format(KEY_FORMAT, fileName, startOffset, endOffset)
        }

        internal fun scopeOrDefault(scope : GlobalSearchScope?, project: Project) : GlobalSearchScope {
            return scope ?: GlobalSearchScope.allScope(project)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<ObjJRangeKey> {
        return object:KeyDescriptor<ObjJRangeKey> {
            override fun save(stream: DataOutput, key: ObjJRangeKey?) {
                stream.writeInt(key?.start ?: -1)
                stream.writeInt(key?.end ?: -1)
                stream.writeUTF(key?.variableName ?: "")
            }

            override fun isEqual(aKey: ObjJRangeKey?, otherKey: ObjJRangeKey?): Boolean {
                return aKey == otherKey
            }

            override fun getHashCode(key: ObjJRangeKey?): Int {
                return key.hashCode()
            }

            override fun read(stream: DataInput): ObjJRangeKey {
                val start = stream.readInt()
                val end = stream.readInt()
                val variableName = stream.readUTF()
                return ObjJRangeKey(variableName, start, end)
            }

        }
    }
}

data class ObjJRangeKey(val variableName:String, val start:Int, val end:Int)