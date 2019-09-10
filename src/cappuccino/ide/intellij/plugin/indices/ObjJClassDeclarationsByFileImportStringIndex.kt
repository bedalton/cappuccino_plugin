package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubIndexKey

class ObjJClassDeclarationsByFileImportStringIndex private constructor() : ObjJStringStubIndexBase<ObjJClassDeclarationElement<*>>() {

    override val indexedElementClass: Class<ObjJClassDeclarationElement<*>>
        get() = ObjJClassDeclarationElement::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    fun getForFramework(frameworkNameIn:String, project: Project) : List<ObjJClassDeclarationElement<*>> {
        return getForImportString(frameworkNameIn, "([^>]+?)", project)
    }

    fun getForImportString(frameworkNameIn:String, fileName:String, project: Project) : List<ObjJClassDeclarationElement<*>> {
        val frameworkName = if (frameworkNameIn == EMPTY_FRAMEWORK_NAME) "([^/]+?)" else frameworkNameIn
        val regex = "<$frameworkName/$fileName>"
        return getByPatternFlat(regex, project)
    }

    companion object {
        val instance = ObjJClassDeclarationsByFileImportStringIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsByFileImportStringIndex::class.java)
        private const val VERSION = 1
    }

}
