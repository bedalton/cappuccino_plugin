package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.startsAndEndsWith
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import java.util.*
import java.util.logging.Logger
import java.util.regex.Pattern

abstract class ObjJStringStubIndexBase<ObjJElemT : PsiElement> : StringStubIndexExtension<ObjJElemT>() {

    protected abstract val indexedElementClass: Class<ObjJElemT>

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    operator fun get(variableName: String, project: Project): List<ObjJElemT> {
        return get(variableName, project, GlobalSearchScope.allScope(project))
    }
    override operator fun get(keyString: String, project: Project, scope: GlobalSearchScope): List<ObjJElemT> {
        return StubIndex.getElements(key, keyString, project, scope, indexedElementClass).toList()
    }

    fun getByPattern(start: String?, tail: String?, project: Project): Map<String, List<ObjJElemT>> {
        return getByPattern(start, tail, project, null)
    }

    @Suppress("SameParameterValue")
    private fun getByPattern(
            start: String?,
            tail: String?,
            project: Project,
            globalSearchScope: GlobalSearchScope?): Map<String, List<ObjJElemT>> {

        val keys = ArrayList<String>()
        val notMatchingKeys = ArrayList<String>()
        for (key in getAllKeys(project)) {
            if (notMatchingKeys.contains(key) || keys.contains(key)) {
                continue
            }
            if (key.startsAndEndsWith(start, tail)) {
                keys.add(key)
            } else {
                notMatchingKeys.add(key)
            }
        }
        return getAllForKeys(keys, project, globalSearchScope)

    }

    fun getByPattern(patternString: String?, project: Project): Map<String, List<ObjJElemT>> {
        return getByPattern(patternString, project, null)
    }

    fun getByPattern(patternString: String?, project: Project, globalSearchScope: GlobalSearchScope?): Map<String, List<ObjJElemT>> {
        return if (patternString == null) {
            Collections.emptyMap()
        } else getAllForKeys(getKeysByPattern(patternString, project, globalSearchScope), project, globalSearchScope)
    }

    @JvmOverloads
    protected fun getAllForKeys(keys: List<String>, project: Project, globalSearchScope: GlobalSearchScope? = null): Map<String, MutableList<ObjJElemT>> {
        val out = HashMap<String, MutableList<ObjJElemT>>() as MutableMap<String, MutableList<ObjJElemT>>
        for (key in keys) {
            if (out.containsKey(key)) {
                out[key]!!.addAll(get(key, project, globalSearchScope ?: GlobalSearchScope.allScope(project)))
            } else {
                out[key] = get(key, project, globalSearchScope ?: GlobalSearchScope.allScope(project)).toMutableList()
            }
        }
        return out
    }

    fun containsKey(key:String, project: Project) : Boolean {
        return getAllKeys(project).contains(key)
    }

    fun getStartingWith(pattern: String, project: Project): List<ObjJElemT> {
        return getByPatternFlat("$pattern(.*)", project)
    }

    @JvmOverloads
    fun getByPatternFlat(pattern: String, project: Project, scope: GlobalSearchScope? = null): List<ObjJElemT> {
        val keys = getKeysByPattern(pattern, project, scope)
        return getAllForKeysFlat(keys, project, scope)

    }

    protected fun getAllForKeysFlat(keys: List<String>, project: Project, globalSearchScope: GlobalSearchScope?): List<ObjJElemT> {
        val out = ArrayList<ObjJElemT>()
        val done = ArrayList<String>()
        for (key in keys) {
            if (!done.contains(key)) {
                done.add(key)
                out.addAll(get(key, project, scopeOrDefault(globalSearchScope, project)))
            }
        }
        return out
    }

    @JvmOverloads
    fun getKeysByPattern(patternString: String?, project: Project, @Suppress("UNUSED_PARAMETER") globalSearchScope: GlobalSearchScope? = null): List<String> {
        if (patternString == null) {
            return emptyList()
        }
        val matchingKeys = ArrayList<String>()
        val notMatchingKeys = ArrayList<String>()
        val pattern: Pattern = try {
            Pattern.compile(patternString)
        } catch (e: Exception) {
            Pattern.compile(Pattern.quote(patternString))
        }

        for (key in getAllKeys(project)) {
            if (notMatchingKeys.contains(key) || matchingKeys.contains(key)) {
                continue
            }
            if (pattern.matcher(key).matches()) {
                matchingKeys.add(key)
            } else {
                notMatchingKeys.add(key)
            }
        }
        return matchingKeys
    }

    @JvmOverloads
    fun getAll(project: Project, globalSearchScope: GlobalSearchScope? = null): List<ObjJElemT> {
        val out = ArrayList<ObjJElemT>()
        for (key in getAllKeys(project)) {
            out.addAll(get(key, project, scopeOrDefault(globalSearchScope, project)))
        }
        return out
    }

    fun getAllResolvableKeys(project: Project): List<String> {
        return getAllKeys(project).filter { key -> isResolvableKey(key as String, project) }
    }

    private fun isResolvableKey(key: String, project: Project): Boolean {
        val elementsForKey = get(key, project, scopeOrDefault(null, project))
        for (element in elementsForKey) {
            return ObjJPsiImplUtil.shouldResolve(element)
        }
        return false
    }

    internal fun scopeOrDefault(scope : GlobalSearchScope?, project: Project) : GlobalSearchScope {
        return scope ?: GlobalSearchScope.allScope(project)
    }

    companion object {

        @Suppress("unused")
        private val LOGGER by lazy {
            Logger.getLogger(ObjJStringStubIndexBase::class.java.name)
        }
        protected val emptyList: Map<Any, Any> = emptyMap()
        protected const val VERSION = 3
    }
}
