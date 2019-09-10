package cappuccino.ide.intellij.plugin.jstypedef.lang

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasTreeStructureElement
import cappuccino.ide.intellij.plugin.jstypedef.structure.JsTypeDefStructureViewElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import icons.ObjJIcons
import javax.swing.Icon

class JsTypeDefFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JsTypeDefLanguage.instance), JsTypeDefElement, JsTypeDefHasTreeStructureElement {

    override val containerName get() = ObjJPsiFileUtil.getFileNameSafe(this)

    val interfaces: List<JsTypeDefInterfaceElement>
        get() = getChildrenOfType(JsTypeDefInterfaceElement::class.java)

    val globalVariables : List<JsTypeDefVariableDeclaration>
        get() = getChildrenOfType(JsTypeDefVariableDeclaration::class.java)

    val globalFunctions : List<JsTypeDefFunctionDeclaration>
        get () = getChildrenOfType(JsTypeDefFunctionDeclaration::class.java)

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

    fun getModuleByNameAndNamespace(name:String) : JsTypeDefModule? {
        for(module in getAllModulesFlat()) {
            if (module.namespacedName == name)
                return module
        }
        return null
    }

    fun getModulesWithNamespace(nameSpace:String): List<JsTypeDefModule> {
        val out = mutableListOf<JsTypeDefModule>()
        for(module in getAllModulesFlat()) {
            if (module.namespacedName.startsWith(nameSpace))
                out.add(module)
        }
        return out
    }

    fun getAllModulesFlat() : List<JsTypeDefModule> {
        val out = mutableListOf<JsTypeDefModule>()
        val rootModules = getChildrenOfType(JsTypeDefModule::class.java)
        for (module in rootModules) {
            out.add(module)
            out.addAll(module.moduleList)
        }
        return out
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getParentOfType(this, parentClass)

    override fun createTreeStructureElement(): JsTypeDefStructureViewElement {
        val fileName = ObjJPsiFileUtil.getFileNameSafe(this, "")
        return JsTypeDefStructureViewElement(this, PresentationData(fileName, "", ObjJIcons.JSDEF_DOCUMENT_ICON, null), fileName)
    }

}
