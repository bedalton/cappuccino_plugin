package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import java.util.*
import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class ObjJMethodHeaderDeclarationsIndexBase<MethodHeaderT : ObjJMethodHeaderDeclaration<*>> : ObjJStringStubIndexBase<MethodHeaderT>() {

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }


    @Throws(IndexNotReadyRuntimeException::class)
    fun getByPatternFuzzy(patternString: String?,
                          part: String?,
                          project: Project): Map<String, List<MethodHeaderT>> {
        return getByPatternFuzzy(patternString, part, project, null)
    }

    @Throws(IndexNotReadyRuntimeException::class)
    fun getByPatternFuzzy(patternString: String?,
                          part: String?,
                          project: Project,
                          globalSearchScope: GlobalSearchScope?): Map<String, List<MethodHeaderT>> {
        return if (patternString == null) {
            Collections.emptyMap()
        } else getAllForKeys(getKeysByPatternFuzzy(patternString, part, project, globalSearchScope), project, globalSearchScope)
    }

    @Suppress("UNUSED_PARAMETER")
    @Throws(IndexNotReadyRuntimeException::class)
    fun getKeysByPatternFuzzy(patternString: String?, selectorPart: String?, project: Project, globalSearchScope: GlobalSearchScope?): List<String> {
        if (patternString == null) {
            return emptyList()
        }
        val parts = getParts(selectorPart)
        val matchingKeys = ArrayList<String>()
        val nonMatchingKeys = ArrayList<String>()
        val pattern = Pattern.compile(patternString)
        var matches: Matcher

        for (key in getAllKeys(project)) {

            //Skip already checked key
            if (matchingKeys.contains(key) || nonMatchingKeys.contains(key)) {
                continue
            }
            //Match current key
            matches = pattern.matcher(key)
            if (!matches.matches() || matches.groupCount() < 2) {
                nonMatchingKeys.add(key)
                continue
            }
            if (parts.isEmpty()) {
                matchingKeys.add(key)
                continue
            }
            val keyPart = matches.group(1).toLowerCase()
            var isMatch = true
            for (part in parts) {
                if (false && !keyPart.contains(part)) {
                    nonMatchingKeys.add(key)
                    isMatch = false
                    break
                }
            }
            if (isMatch) {
                matchingKeys.add(key)
            }
        }
        return matchingKeys
    }

    companion object {

        @Suppress("unused")
        private val LOGGER by lazy {
            Logger.getLogger(ObjJMethodHeaderDeclarationsIndexBase::class.java.canonicalName)
        }
        private const val VERSION = 2
        private val PARTS_PATTERN = Pattern.compile("([a-z]*)?([A-Z0-9][a-z]*)*")

        private fun getParts(parts: String?): List<String> {
            if (parts == null || parts.isEmpty()) {
                return ArrayUtils.EMPTY_STRING_ARRAY
            }
            val matcher = PARTS_PATTERN.matcher(parts)
            if (!matcher.matches()) {
                return ArrayUtils.EMPTY_STRING_ARRAY
            }
            val numMatches = matcher.groupCount()
            val out = ArrayList<String>()
            for (i in 1 until numMatches) {
                out.add(matcher.group(i).toLowerCase())
            }
            return out
        }
    }

}
