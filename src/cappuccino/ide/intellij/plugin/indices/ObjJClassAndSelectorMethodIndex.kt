package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import com.intellij.openapi.project.Project

class ObjJClassAndSelectorMethodIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration<*>>() {

    override val indexedElementClass: Class<ObjJMethodHeaderDeclaration<*>>
        get() = ObjJMethodHeaderDeclaration::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    fun getByClassAndSelector(className:String, selector:String, project: Project) : List<ObjJMethodHeaderDeclaration<*>> {
        val key = getClassMethodKey(className, selector)
        return get(key, project)
    }

    fun getByClassNameAndPattern(className:String, selectorStart:String, tail:String, project: Project) : Map<String, List<ObjJMethodHeaderDeclaration<*>>> {
        val start = getClassMethodKey(className, selectorStart)
        return getByPattern(start, tail, project)
    }

    fun containsKey(className: String, selector: String, project: Project) : Boolean {
        val key = getClassMethodKey(className, selector)
        return containsKey(key, project)
    }


    companion object {
        val KEY = IndexKeyUtil.createIndexKey(ObjJClassAndSelectorMethodIndex::class.java)
        val instance = ObjJClassAndSelectorMethodIndex()
        private const val VERSION = 1
        private val CLASS_AND_SELECTOR_DELIM = "->"
        fun getClassMethodKey(className:String, selector:String) : String {
            return "$className$CLASS_AND_SELECTOR_DELIM$selector"
        }
    }
}
