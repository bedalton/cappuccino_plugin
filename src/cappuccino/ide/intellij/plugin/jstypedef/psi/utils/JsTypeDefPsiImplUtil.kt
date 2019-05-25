package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasNamespace
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNode
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
    fun getName(functionName:JsTypeDefFunctionName?) : String {
        return functionName?.text ?: ""
    }

    @JvmStatic
    fun getName(propertyName:JsTypeDefPropertyName?) : String {
        return propertyName?.text ?: ""
    }

    @JvmStatic
    fun getName(typeName:JsTypeDefTypeName?) : String {
        return typeName?.text ?: ""
    }

    @JvmStatic
    fun getName(moduleName:JsTypeDefModuleName) : String {
        return moduleName.text ?: ""
    }

    // ============================== //
    // ========== Set Name ========== //
    // ============================== //
    @JvmStatic
    fun setName(oldFunctionName:JsTypeDefFunctionName, newName:String) : PsiElement {
        val newNode = JsTypeDefElementFactory.createFunctionName(oldFunctionName.project, newName) ?: return oldFunctionName
        return oldFunctionName.replace(newNode)
    }

    @JvmStatic
    fun setName(oldPropertyName: JsTypeDefPropertyName, newName:String) : PsiElement {
        val newPropertyName = JsTypeDefElementFactory.createProperty(oldPropertyName.project, newName, "null") ?: return oldPropertyName
        return oldPropertyName.replace(newPropertyName)
    }

    @JvmStatic
    fun setName(oldTypeName:JsTypeDefTypeName, newName:String) : PsiElement {
        val newTypeName = JsTypeDefElementFactory.createTypeName(oldTypeName.project, newName) ?: return oldTypeName
        return oldTypeName.replace(newTypeName)
    }

    @JvmStatic
    fun setName(oldModuleName:JsTypeDefModuleName, newName:String) : PsiElement {
        val newModuleName = JsTypeDefElementFactory.createModuleName(oldModuleName.project, newName) ?: return oldModuleName
        return oldModuleName.replace(newModuleName)
    }

    // ============================== //
    // ========== Namespace ========= //
    // ============================== //
    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefElement) : String {
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName ?: ""
    }

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefProperty) : String =
            element.stub?.enclosingNamespace
                ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                ?: ""
    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefProperty) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefProperty) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.propertyName.text)

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefProperty) : String
            = element.stub?.propertyName ?: element.propertyName.text

    @JvmStatic
    fun getEnclosingNamespace(elementIn:JsTypeDefFunction) : String {
        val stub = elementIn.stub
        if (stub != null)
            return stub.enclosingNamespace
        val element = elementIn.parent as? JsTypeDefFunctionDeclaration ?: elementIn
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.enclosingNamespace ?: ""
    }

    @JvmStatic
    fun getEnclosingNamespaceComponents(elementIn:JsTypeDefFunction) : List<String> {
        val stub = elementIn.stub
        if (stub != null)
            return stub.enclosingNamespaceComponents
        val element = elementIn.parent as? JsTypeDefFunctionDeclaration ?: elementIn
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.enclosingNamespaceComponents ?: emptyList()
    }

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefFunction) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.functionName.text)

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefFunction) : String
            = element.stub?.functionName ?: element.functionName.text

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefFunctionName) : String
            = (element.parent as JsTypeDefFunction).enclosingNamespace

    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefFunctionName) : List<String>
            = (element.parent as JsTypeDefFunction).enclosingNamespaceComponents

    @JvmStatic
    fun getNamespaceComponents(functionName:JsTypeDefFunctionName) : List<String>
            = (functionName.parent as JsTypeDefFunction).namespaceComponents

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefFunctionName) : String
            = element.text

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefInterfaceElement) : String =
            element.stub?.enclosingNamespace
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                    ?: ""

    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefInterfaceElement) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefInterfaceElement) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.interfaceName)

    @JvmStatic
    fun getNamespaceComponent(element: JsTypeDefInterfaceElement) : String
            = element.stub?.interfaceName ?: element.typeName?.text ?: "???"

    @JvmStatic
    fun getNamespacedName(element:JsTypeDefHasNamespace) : String {
            val base = element.enclosingNamespace
            return if (base.isEmpty())
                element.namespaceComponent
            else
                "$base.${element.namespaceComponent}"
    }

    // ============================== //
    // ========== Modules =========== //
    // ============================== //

    @JvmStatic
    fun getNamespacedName(module:JsTypeDefModule) : String
            = module.stub?.fullyNamespacedName ?: getNamespaceComponents(module).joinToString(".")

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefModule) : String =
            element.stub?.enclosingNamespace
                    ?: element.enclosingNamespaceComponents.joinToString(".")
                    ?: ""

    @JvmStatic
    fun getEnclosingNamespaceComponents(module:JsTypeDefModule) : List<String> {
        val stub = module.stub
        if (stub != null) {
            return stub.namespaceComponents
        }
        val components = getNamespaceComponents(module).toMutableList()
        components.removeAt(components.lastIndex)
        return components
    }

    @JvmStatic
    fun getNamespaceComponents(module:JsTypeDefModule) : List<String> {
        val stub = module.stub
        if (stub != null)
            return stub.namespaceComponents
        val temp = mutableListOf<String>()
        var currentModule:JsTypeDefModule? = module
        while (currentModule != null) {
            temp.add(0, currentModule.namespacedModuleName.text)
            currentModule = currentModule.getParentOfType(JsTypeDefModule::class.java)
        }
        return temp.joinToString(".").split(NAMESPACE_SPLITTER_REGEX)
    }

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefModule) : String
            = element.stub?.moduleName ?: element.namespacedModuleName.moduleName.text


    @JvmStatic
    fun getAllSubModules(moduleIn:JsTypeDefModule, recursive:Boolean = true) : List<JsTypeDefModule> {
        if (!recursive) {
            return moduleIn.moduleList
        }
        val allModules = mutableListOf<JsTypeDefModule>()
        val currentModules = moduleIn.getChildrenOfType(JsTypeDefModule::class.java)
        for (module in currentModules) {
            allModules.addAll(getAllSubModules(module))
        }
        return allModules
    }

    @JvmStatic
    fun getCollapsedNamespaceComponents(module:JsTypeDefModule) : List<JsTypeDefModuleName> {
        val out = module.namespacedModuleName.namespace.moduleNameList.toMutableList()
        out.add(module.namespacedModuleName.moduleName)
        return out
    }

    @JvmStatic
    fun getEnclosingNamespaceComponents(moduleName:JsTypeDefModuleName) : List<String> {
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
    fun getEnclosingNamespace(moduleName:JsTypeDefModuleName) : String {
        return getEnclosingNamespaceComponents(moduleName).joinToString(".")
    }

    @JvmStatic
    fun getNamespaceComponents(moduleName:JsTypeDefModuleName) : List<String>
            = moduleName.stub?.namespaceComponents ?: getEnclosingNamespaceComponents(moduleName) + moduleName.text

    @JvmStatic
    fun getFullyNamespacedName(moduleName:JsTypeDefModuleName) : String
            = (getEnclosingNamespaceComponents(moduleName) + moduleName.text)
                .joinToString(".")

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefModuleName) : String
            = element.text

    @JvmStatic
    fun getIndexInDirectNamespace(moduleName:JsTypeDefModuleName) : Int {
        val parentModule = getParentModule(moduleName) ?: return -1
        val namespaceComponents = getCollapsedNamespaceComponents(parentModule)
        return namespaceComponents.indexOf(moduleName)
    }

    @JvmStatic
    fun getIndexInNamespace(moduleName:JsTypeDefModuleName) : Int {
        val parentModule = getParentModule(moduleName) ?: return -1
        val namespaceComponents = getCollapsedNamespaceComponents(parentModule)
        val moduleNameIndex = namespaceComponents.indexOf(moduleName)
        if (moduleNameIndex < 0)
            return -1
        val numberOfPrecedingNamespaceComponents = getEnclosingNamespaceComponents(parentModule).size
        return numberOfPrecedingNamespaceComponents + moduleNameIndex
    }

    @JvmStatic
    fun getParentModule(moduleName:JsTypeDefModuleName) : JsTypeDefModule? {
        return moduleName.getParentOfType(JsTypeDefModule::class.java)
    }


    // ============================== //
    // ========= Interfaces ========= //
    // ============================== //

    @JvmStatic
    fun getConstructors(interfaceElement: JsTypeDefInterfaceElement) : List<JsTypeDefFunction> {
        val functions = interfaceElement.interfaceBody?.functionList ?: return listOf()
        return functions.filter {
            it.functionName.const != null
        }
    }


    // ============================== //
    // ========= Properties ========= //
    // ============================== //

    @JvmStatic
    fun getPropertyTypes(property:JsTypeDefProperty) : List<JsTypeDefType> {
        return property.typeList
    }

    @JvmStatic
    fun isNullable(property:JsTypeDefProperty) : Boolean {
        return isNullable(property.typeList)
    }

    @JvmStatic
    fun isNullableReturnType(function:JsTypeDefFunction) : Boolean {
        return isNullable(function.functionReturnType)
    }

    @JvmStatic
    fun isNullable(returnType:JsTypeDefFunctionReturnType?) : Boolean {
        return returnType?.void != null || isNullable(returnType?.typeList)
    }

    @JvmStatic
    fun isNullable(typeList:List<JsTypeDefType>?) : Boolean {
        return typeList?.firstOrNull { it.nullType != null } != null
    }


    // ============================== //
    // ========== TypeMap =========== //
    // ============================== //
    @JvmStatic
    fun getMapName(typeMap:JsTypeDefTypeMap) : String? {
        val stub = typeMap.stub
        if (stub != null)
            return stub.mapName
        return typeMap.typeMapName?.text
    }

    @JvmStatic
    fun getKeys(typeMap:JsTypeDefTypeMap) : List<String> {
        val stub = typeMap.stub
        if (stub != null)
            return stub.values.map { it.key }
        return typeMap.keyValuePairs.map { it.stringLiteral.content }
    }

    @JvmStatic
    fun getTypesForKey(typeMap: JsTypeDefTypeMap, key:String) : JsTypeDefTypesList? {
        val stub = typeMap.stub
        if (stub != null) {
            return stub.getTypesForKey(key)
        }
        return typeMap.keyValuePairs.filter{ it.key == key }.map { it.typesList }.combine()
    }

    @JvmStatic
    fun getKeyValuePairs(typeMap: JsTypeDefTypeMap) : List<JsTypeDefTypeMapKeyValuePair>
            = typeMap.typeMapKeyValuePairList

    @JvmStatic
    fun getKey(keyValuePair: JsTypeDefTypeMapKeyValuePair) : String {
        return keyValuePair.stringLiteral.content
    }

    @JvmStatic
    fun getTypesList(keyValuePair: JsTypeDefTypeMapKeyValuePair) : JsTypeDefTypesList {
        val nullable = isNullable(keyValuePair)
        val types = keyValuePair.typeList.toJsTypeDefTypeListTypes()
        return JsTypeDefTypesList(types, nullable)
    }

    @JvmStatic
    fun isNullable(keyValuePair: JsTypeDefTypeMapKeyValuePair) : Boolean {
        return keyValuePair.typeList.any { it.nullType != null }
    }

    // ============================== //
    // ========= Interface ========== //
    // ============================== //

    @JvmStatic
    fun getInterfaceName(typeInterface: JsTypeDefInterfaceElement) : String
            = typeInterface.stub?.interfaceName ?: typeInterface.typeName?.text ?: "?"


    // ============================== //
    // ======== Descriptions ======== //
    // ============================== //
    @JvmStatic
    fun getDescriptiveText(psiElement: JsTypeDefElement) : String {
        return when (psiElement) {
            is JsTypeDefInterfaceElement -> {
                "interface " + (psiElement.typeName?.id?.text ?: "???") + (if (psiElement.extendsStatement?.typeList != null) {
                    val typeListText = psiElement.extendsStatement!!.typeList.joinToString("|")
                    " extends $typeListText"
                } else "")
            }
            is JsTypeDefPropertyName -> {
                val escapedId = psiElement.escapedId?.text
                val propertyName = if (escapedId != null) {
                    escapedId.substring(1, escapedId.length - 2)
                } else {
                    (psiElement as JsTypeDefPropertyName).text
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

    @JvmStatic
    fun getContent(stringLiteral: JsTypeDefStringLiteral) : String {
        val text = stringLiteral.text
        val textLength = text.length
        if (textLength < 3)
            return ""
        return text.substring(1, textLength-2)
    }

    @JvmStatic
    fun isStatic(functionDeclaration:JsTypeDefFunctionDeclaration) : Boolean {
        return true
    }

    @JvmStatic
    fun isStatic(function:JsTypeDefFunction) : Boolean {
        return function.staticKeyword != null || function.parent is JsTypeDefFunctionDeclaration
    }
}

val TYPE_SPLIT_REGEX = "\\s*\\|\\s*".toRegex()

val NAMESPACE_SPLITTER_REGEX = "\\s*\\.\\s*".toRegex()

typealias JsTypeDefClassName = String

fun List<JsTypeDefTypesList>.combine() : JsTypeDefTypesList {
    val nullable = this.any { it.nullable }
    val types = this.flatMap { it.types }
    return JsTypeDefTypesList(types, nullable)
}

fun JsTypeDefTypesList.plus(otherType: JsTypeDefTypesList) : JsTypeDefTypesList {
    val nullable = this.nullable || otherType.nullable
    val types = this.types + otherType.types
    return JsTypeDefTypesList(types, nullable)
}