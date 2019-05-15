package cappuccino.ide.intellij.plugin.jstypedef.lang

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsFunctionDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeInterface
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsVariableDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasTreeStructureElement
import cappuccino.ide.intellij.plugin.jstypedef.structure.JsTypeDefStructureViewElement
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ide.projectView.PresentationData
import icons.ObjJIcons

import javax.swing.*

class JsTypeDefFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JsTypeDefLanguage.instance), JsTypeDefElement, JsTypeDefHasTreeStructureElement {

    override val containerName get() = ObjJFileUtil.getFileNameSafe(this)

    val interfaces: List<JsTypeInterface>
        get() = getChildrenOfType(JsTypeInterface::class.java)

    val globalVariables : List<JsVariableDeclaration>
        get() = getChildrenOfType(JsVariableDeclaration::class.java)

    val globalFunctions : List<JsFunctionDeclaration>
        get () = getChildrenOfType(JsFunctionDeclaration::class.java)

    override fun toString(): String {
        return "JsTypeDef Language file"
    }

    override fun getIcon(flags: Int): Icon? {
        return ObjJIcons.JSDEF_DOCUMENT_ICON
    }

    override fun getFileType(): FileType {
        return JsTypeDefFileType.INSTANCE
    }

    override fun <PsiT : PsiElement> getChildOfType(childClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getChildOfType(this, childClass)


    override fun <PsiT : PsiElement> getChildrenOfType(childClass: Class<PsiT>): List<PsiT> =
            PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

    fun getModuleByNameAndNamespace(name:String) : JsModule? {
        for(module in getAllModulesFlat()) {
            if (module.namespacedModuleName == name)
                return module
        }
        return null
    }

    fun getModulesWithNamespace(nameSpace:String): List<JsModule> {
        val out = mutableListOf<JsModule>()
        for(module in getAllModulesFlat()) {
            if (module.namespacedModuleName.startsWith(nameSpace))
                out.add(module)
        }
        return out
    }

    fun getAllModulesFlat() : List<JsModule> {
        val out = mutableListOf<JsModule>()
        val rootModules = getChildrenOfType(JsModule::class.java)
        for (module in rootModules) {
            out.add(module)
            out.addAll(module.moduleList)
        }
        return out
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getParentOfType(this, parentClass)

    override fun createTreeStructureElement(): JsTypeDefStructureViewElement {
        val fileName = ObjJFileUtil.getFileNameSafe(this, "")
        return JsTypeDefStructureViewElement(this, PresentationData(fileName, "", ObjJIcons.DOCUMENT_ICON, null), fileName)
    }

}
