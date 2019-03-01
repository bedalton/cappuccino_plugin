package cappuccino.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement

class ObjJFunctionsIndex private constructor() : ObjJStringStubIndexBase<ObjJFunctionDeclarationElement<*>>() {

    protected override val indexedElementClass: Class<ObjJFunctionDeclarationElement<*>>
        get() = ObjJFunctionDeclarationElement::class.java

    operator fun get(fileName: String, functionName: String, project: Project): ObjJFunctionDeclarationElement<*>? {
        for (declarationElement in get(fileName, project)) {
            if (declarationElement.functionNameAsString == functionName) {
                return declarationElement
            }
        }
        return null
    }

    override fun getKey(): StubIndexKey<String, ObjJFunctionDeclarationElement<*>> {
        return KEY
    }

    override fun getVersion(): Int {
        return ObjJIndexService.INDEX_VERSION + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJFunctionDeclarationElement<*>>(ObjJFunctionsIndex::class.java)
        val instance = ObjJFunctionsIndex()
        private val VERSION = 1
    }
}
