package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNode
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

object JsTypeDefPsiImplUtil {

    private val EOS_TOKENS = listOf(
        JS_SEMI_COLON
    )


    // ============================== //
    // ========== Get Name ========== //
    // ============================== //
    @JvmStatic
    fun getName(functionName:JsFunctionName?) : String {
        return functionName?.text ?: ""
    }

    @JvmStatic
    fun getName(propertyName:JsPropertyName?) : String {
        return propertyName?.text ?: ""
    }

    @JvmStatic
    fun getName(typeName:JsTypeName?) : String {
        return typeName?.text ?: ""
    }

    // ============================== //
    // ========== Set Name ========== //
    // ============================== //
    @JvmStatic
    fun setName(oldFunctionName:JsFunctionName, newName:String) : PsiElement {
        val newNode = JsTypeDefElementFactory.createFunctionName(oldFunctionName.project, newName) ?: return oldFunctionName
        return oldFunctionName.replace(newNode)
    }
    @JvmStatic
    fun setName(oldPropertyName: JsPropertyName, newName:String) : PsiElement {
        val newPropertyName = JsTypeDefElementFactory.createProperty(oldPropertyName.project, newName, "null") ?: return oldPropertyName
        return oldPropertyName.replace(newPropertyName)
    }

    @JvmStatic
    fun setName(oldTypeName:JsTypeName, newName:String) : PsiElement {
        val newTypeName = JsTypeDefElementFactory.createTypeName(oldTypeName.project, newName) ?: return oldTypeName
        return oldTypeName.replace(newTypeName)
    }

    // ============================== //
    // ========== Modules =========== //
    // ============================== //
    @JvmStatic
    fun getNamespacedModuleName(module:JsModule) : String {
        val out = mutableListOf<String>()
        var currentModule:JsModule? = module
        while (currentModule != null) {
            out.add(0, currentModule.qualifiedModuleName.text)
            currentModule = currentModule.getParentOfType(JsModule::class.java)
        }
        return out.joinToString(".")
    }

    @JvmStatic
    fun getAllSubModules(moduleIn:JsModule, recursive:Boolean = true) : List<JsModule> {
        if (!recursive) {
            return moduleIn.moduleList
        }
        val allModules = mutableListOf<JsModule>()
        val currentModules = moduleIn.getChildrenOfType(JsModule::class.java)
        for (module in currentModules) {
            allModules.addAll(getAllSubModules(module))
        }
        return allModules
    }

    @JvmStatic
    fun getEnclosingNamespace(module:JsModule) : List<String> {
        val temp = module.namespacedModuleName.split("\\s*\\.\\s*".toRegex()).toMutableList()
        temp.removeAt(temp.lastIndex)
        return temp
    }

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefElement) : List<String> {
        if (element is JsModule) {
            return getEnclosingNamespace(element)
        }
        val enclosingModule = element.getParentOfType(JsModule::class.java)
        return enclosingModule?.namespacedModuleName?.split("\\s*\\.\\s*".toRegex()) ?: listOf()
    }


    // ============================== //
    // ======== Descriptions ======== //
    // ============================== //
    @JvmStatic
    fun getDescriptiveText(psiElement: JsTypeDefElement) : String {
        return when (psiElement) {
            is JsTypeInterface -> {
                "interface " + (psiElement.typeName?.id?.text ?: "???") + (if (psiElement.extendsStatement?.typeList != null) {
                    val typeListText = psiElement.extendsStatement!!.typeList!!.text
                    " extends $typeListText"
                } else "")
            }
            is JsPropertyName -> {
                val escapedId = psiElement.escapedId?.text
                val propertyName = if (escapedId != null) {
                    escapedId.substring(1, escapedId.length - 2)
                } else {
                    psiElement.text
                }

                "property $propertyName"
            }
            else -> ""
        }
    }

    fun eos(compositeElement: PsiElement?): Boolean {
        if (compositeElement == null) {
            return false
        }
        var ahead = compositeElement.getNextNode()
        if (ahead == null && compositeElement.parent != null) {
            return eos(compositeElement.parent)
        }
        var hadLineTerminator = false
        while (ahead != null && (ahead.elementType === com.intellij.psi.TokenType.WHITE_SPACE || ahead.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR)) {
            if (ahead === ObjJTypes.ObjJ_LINE_TERMINATOR) {
                hadLineTerminator = true
            }
            while (ahead!!.treeNext == null && ahead.treeParent != null) {
                ahead = ahead.treeParent
            }
            ahead = ahead.treeNext
        }
        return ahead != null && eosToken(ahead.elementType, hadLineTerminator)
    }

    fun eosToken(ahead: IElementType?, hadLineTerminator: Boolean): Boolean {
        if (ahead == null) {
            //LOGGER.log(Level.INFO, "EOS assumed as ahead == null")
            return true
        }
        return ahead in EOS_TOKENS
    }

    @Suppress("unused")
    fun PsiElement?.hasNodeType(elementType: IElementType): Boolean {
        return this != null && this.node.elementType === elementType
    }
}