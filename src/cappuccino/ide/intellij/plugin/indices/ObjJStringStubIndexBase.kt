package cappuccino.ide.intellij.plugin.indices

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import cappuccino.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.StringUtil
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import org.codehaus.groovy.runtime.ArrayUtil

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.MatchResult
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class ObjJStringStubIndexBase<ObjJElemT : ObjJCompositeElement> : StringStubIndexExtension<ObjJElemT>() {

    protected abstract val indexedElementClass: Class<ObjJElemT>

    override fun getVersion(): Int {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION
    }

    @Throws(IndexNotReadyRuntimeException::class)
    operator fun get(variableName: String, project: Project): MutableList<ObjJElemT> {
        return get(variableName, project, GlobalSearchScope.allScope(project))
    }
    @Throws(IndexNotReadyRuntimeException::class)
    override operator fun get(variableName: String, project: Project, scope: GlobalSearchScope): MutableList<ObjJElemT> {

        if (DumbService.getInstance(project).isDumb) {
            throw IndexNotReadyRuntimeException()
            //return Collections.emptyList();
        }
        //LOGGER.log(Level.INFO, "Index("+getClass().getSimpleName()+")->get("+variableName+")");
        return ArrayList(StubIndex.getElements(key, variableName, project, scope, indexedElementClass))
    }


    @Throws(IndexNotReadyRuntimeException::class)
    fun getByPattern(start: String?, tail: String?, project: Project): Map<String, List<ObjJElemT>> {
        return getByPattern(start, tail, project, null)
    }

    @Throws(IndexNotReadyRuntimeException::class)
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
            if (StringUtil.startsAndEndsWith(key, start, tail)) {
                //LOGGER.log(Level.INFO, "Found selector matching <"+start+"//"+tail+">: <"+key+">");
                keys.add(key)
            } else {
                notMatchingKeys.add(key)
            }
        }
        return getAllForKeys(keys, project, globalSearchScope)

    }

    @Throws(IndexNotReadyRuntimeException::class)
    fun getByPattern(patternString: String?, project: Project): Map<String, List<ObjJElemT>> {
        return getByPattern(patternString, project, null)
    }


    @Throws(IndexNotReadyRuntimeException::class)
    fun getByPattern(patternString: String?, project: Project, globalSearchScope: GlobalSearchScope?): Map<String, List<ObjJElemT>> {
        return if (patternString == null) {
            Collections.emptyMap()
        } else getAllForKeys(getKeysByPattern(patternString, project, globalSearchScope), project, globalSearchScope)
    }

    @Throws(IndexNotReadyRuntimeException::class)
    @JvmOverloads
    protected fun getAllForKeys(keys: List<String>, project: Project, globalSearchScope: GlobalSearchScope? = null): Map<String, MutableList<ObjJElemT>> {
        val out = HashMap<String, MutableList<ObjJElemT>>() as MutableMap<String, MutableList<ObjJElemT>>
        for (key in keys) {
            if (out.containsKey(key)) {
                out[key]!!.addAll(get(key, project, if (globalSearchScope != null) globalSearchScope else GlobalSearchScope.allScope(project)))
            } else {
                out[key] = get(key, project, if (globalSearchScope != null) globalSearchScope else GlobalSearchScope.allScope(project))
            }
        }
        return out
    }

    fun getStartingWith(pattern: String, project: Project): List<ObjJElemT> {
        return getByPatternFlat("$pattern(.*)", project)
    }

    @JvmOverloads
    fun getByPatternFlat(pattern: String, project: Project, scope: GlobalSearchScope? = null): List<ObjJElemT> {
        val keys = getKeysByPattern(pattern, project, scope)
        return getAllForKeysFlat(keys, project, scope)

    }

    @Throws(IndexNotReadyRuntimeException::class)
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


    @Throws(IndexNotReadyRuntimeException::class)
    @JvmOverloads
    fun getKeysByPattern(patternString: String?, project: Project, globalSearchScope: GlobalSearchScope? = null): List<String> {
        if (patternString == null) {
            return emptyList()
        }
        val matchingKeys = ArrayList<String>()
        val notMatchingKeys = ArrayList<String>()
        var pattern: Pattern
        try {
            pattern = Pattern.compile(patternString)
        } catch (e: Exception) {
            pattern = Pattern.compile(Pattern.quote(patternString))
        }

        for (key in getAllKeys(project)) {
            if (notMatchingKeys.contains(key) || matchingKeys.contains(key)) {
                continue
            }
            if (pattern.matcher(key).matches()) {
                //LOGGER.log(Level.INFO, "Found Matching key for pattern: <"+patternString+">: <"+key+">");
                matchingKeys.add(key)
            } else {
                //LOGGER.log(Level.INFO, "Key <"+key+"> does not match pattern: <"+patternString+">");
                notMatchingKeys.add(key)
            }
        }
        return matchingKeys
    }

    @Throws(IndexNotReadyRuntimeException::class)
    @JvmOverloads
    fun getAll(project: Project, globalSearchScope: GlobalSearchScope? = null): List<ObjJElemT> {
        val out = ArrayList<ObjJElemT>()

        if (DumbService.getInstance(project).isDumb) {
            throw IndexNotReadyRuntimeException()
            //return Collections.emptyList();
        }
        for (key in getAllKeys(project)) {
            out.addAll(get(key, project, scopeOrDefault(globalSearchScope, project)))
        }
        return out
    }

    fun getAllResolveableKeys(project: Project): List<String> {
        return ArrayUtils.filter(ArrayList(getAllKeys(project))) { key -> isResolveableKey(key as String, project) }
    }

    private fun isResolveableKey(key: String, project: Project): Boolean {
        val elementsForKey = get(key, project, scopeOrDefault(null, project))
        for (element in elementsForKey) {
            return ObjJPsiImplUtil.shouldResolve(element)
        }
        return false
    }

    internal fun scopeOrDefault(scope : GlobalSearchScope?, project: Project) : GlobalSearchScope {
        return  if (scope != null)
                    scope
                else
                    GlobalSearchScope.allScope(project)
    }

    companion object {

        private val LOGGER = Logger.getLogger(ObjJStringStubIndexBase::class.java.name)
        protected val emptyList: Map<Any, Any> = emptyMap<Any, Any>()
        private val VERSION = 3
    }
}
