package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNode
import cappuccino.ide.intellij.plugin.utils.orFalse
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

    @JvmStatic
    fun getName(moduleName:JsModuleName) : String {
        return moduleName.text ?: ""
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

    @JvmStatic
    fun setName(oldModuleName:JsModuleName, newName:String) : PsiElement {
        val newModuleName = JsTypeDefElementFactory.createModuleName(oldModuleName.project, newName) ?: return oldModuleName
        return oldModuleName.replace(newModuleName)
    }

    // ============================== //
    // ========== Modules =========== //
    // ============================== //
    @JvmStatic
    fun getFullyNamespacedName(module:JsModule) : String {
        return getNamespaceComponents(module).joinToString(".")
    }

    @JvmStatic
    fun getEnclosingNamespaceAsString(module:JsModule) : String {
        return getEnclosingNamespaceComponents(module).joinToString(".")
    }

    @JvmStatic
    fun getEnclosingNamespaceComponents(module:JsModule) : List<String> {
        val components = getNamespaceComponents(module).toMutableList()
        components.removeAt(components.lastIndex)
        return components
    }

    @JvmStatic
    fun getNamespaceComponents(module:JsModule) : List<String> {
        val temp = mutableListOf<String>()
        var currentModule:JsModule? = module
        while (currentModule != null) {
            temp.add(0, currentModule.namespacedModuleName.text)
            currentModule = currentModule.getParentOfType(JsModule::class.java)
        }
        return temp.joinToString(".").split("\\s*\\.\\s*".toRegex())
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
    fun getCollapsedNamespaceComponents(module:JsModule) : List<JsModuleName> {
        val out = module.namespacedModuleName.namespace.moduleNameList.toMutableList()
        out.add(module.namespacedModuleName.moduleName)
        return out
    }


    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefElement) : String {
        val module = element.getParentOfType(JsModule::class.java) ?: return ""
        return getFullyNamespacedName(module)
    }

    @JvmStatic
    fun getPrecedingNamespaceComponents(moduleName:JsModuleName) : List<String> {
        val parentModule = getParentModule(moduleName) ?: return listOf()
        val out = getEnclosingNamespaceComponents(parentModule).toMutableList()
        val namespaceComponents = getCollapsedNamespaceComponents(parentModule)
        val moduleNameIndex = namespaceComponents.indexOf(moduleName)
        if (moduleNameIndex < 0)
            return listOf()
        val directlyPrecedingComponents = namespaceComponents.subList(0, moduleNameIndex).map { it.text }
        out.addAll(directlyPrecedingComponents)
        return out
    }

    @JvmStatic
    fun getPrecedingNamespace(moduleName:JsModuleName) : String {
        return getPrecedingNamespaceComponents(moduleName).joinToString(".")
    }

    @JvmStatic
    fun getFullyNamespacedName(moduleName:JsModuleName) : String {
        val components = getPrecedingNamespaceComponents(moduleName).toMutableList()
        components.add(moduleName.text)
        return components.joinToString(".")
    }

    @JvmStatic
    fun getIndexInDirectNamespace(moduleName:JsModuleName) : Int {
        val parentModule = getParentModule(moduleName) ?: return -1
        val namespaceComponents = getCollapsedNamespaceComponents(parentModule)
        return namespaceComponents.indexOf(moduleName)
    }

    @JvmStatic
    fun getIndexInFullNamespace(moduleName:JsModuleName) : Int {
        val parentModule = getParentModule(moduleName) ?: return -1
        val namespaceComponents = getCollapsedNamespaceComponents(parentModule)
        val moduleNameIndex = namespaceComponents.indexOf(moduleName)
        if (moduleNameIndex < 0)
            return -1
        val numberOfPrecedingNamespaceComponents = getEnclosingNamespaceComponents(parentModule).size
        return numberOfPrecedingNamespaceComponents + moduleNameIndex
    }

    @JvmStatic
    fun getParentModule(moduleName:JsModuleName) : JsModule? {
        return moduleName.getParentOfType(JsModule::class.java)
    }


    // ============================== //
    // ========= Interfaces ========= //
    // ============================== //

    @JvmStatic
    fun getConstructors(interfaceElement:JsTypeInterface) : List<JsFunction> {
        val functions = interfaceElement.interfaceBody?.functionList ?: return listOf()
        return functions.filter {
            it.functionName.const != null
        }
    }


    // ============================== //
    // ========= Properties ========= //
    // ============================== //

    @JvmStatic
    fun getPropertyTypes(property:JsProperty) : List<JsType> {
        return property.propertyType.types.typeList
    }

    @JvmStatic
    fun isNullable(property:JsProperty) : Boolean {
        return isNullable(property.propertyType.types)
    }

    @JvmStatic
    fun isNullableReturnType(function:JsFunction) : Boolean {
        return isNullable(function.functionReturnType)
    }

    @JvmStatic
    fun isNullable(returnType:JsFunctionReturnType?) : Boolean {
        return returnType?.void != null || isNullable(returnType?.propertyType?.types)
    }

    @JvmStatic
    fun isNullable(types:JsTypes?) : Boolean {
        return types?.typeList?.firstOrNull { it.nullType != null } != null
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