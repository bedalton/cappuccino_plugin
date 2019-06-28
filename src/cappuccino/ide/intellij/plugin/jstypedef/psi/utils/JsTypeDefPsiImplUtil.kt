package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasNamespace
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNode
import cappuccino.ide.intellij.plugin.utils.orTrue
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
    fun getEnclosingNamespace(element:JsTypeDefVariableDeclaration) : String =
            element.stub?.enclosingNamespace
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                    ?: ""
    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefProperty) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()

    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefVariableDeclaration) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()
    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefProperty) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.propertyName.text)

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefVariableDeclaration) : List<String>
            = element.stub?.namespaceComponents ?: element.property?.namespaceComponents.orEmpty()

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefProperty) : String
            = element.stub?.propertyName ?: element.propertyName.text

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefVariableDeclaration) : String
            = element.property?.namespaceComponent.orEmpty()

    @JvmStatic
    fun getEnclosingNamespace(elementIn:JsTypeDefFunction) : String {
        val stub = elementIn.stub
        if (stub != null)
            return stub.enclosingNamespace
        val element = elementIn.parent as? JsTypeDefFunctionDeclaration ?: elementIn
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.enclosingNamespace ?: ""
    }

    @JvmStatic
    fun getEnclosingNamespaceComponents(elementIn:JsTypeDefFunctionDeclaration) : List<String>
        = elementIn.function?.enclosingNamespaceComponents.orEmpty()

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
    fun getNamespaceComponents(element:JsTypeDefFunctionDeclaration) : List<String>
            = element.function?.namespaceComponents.orEmpty()

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefFunction) : String
            = element.stub?.functionName ?: element.functionName.text

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefFunctionDeclaration) : String
            = element.function?.namespaceComponent ?: "???"

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
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.className)

    @JvmStatic
    fun getNamespaceComponent(element: JsTypeDefInterfaceElement) : String
            = element.stub?.className ?: element.typeName?.text ?: "???"

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefClassElement) : String =
            element.stub?.enclosingNamespace
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                    ?: ""

    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefClassElement) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefClassElement) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.className)

    @JvmStatic
    fun getNamespaceComponent(element: JsTypeDefClassElement) : String
            = element.stub?.className ?: element.typeName?.text ?: "???"

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
        while (currentModule?.namespacedModuleName != null) {
            temp.add(0, currentModule.namespacedModuleName!!.text)
            currentModule = currentModule.getParentOfType(JsTypeDefModule::class.java)
        }
        return temp.joinToString(".").split(NAMESPACE_SPLITTER_REGEX)
    }

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefModule) : String
            = element.stub?.moduleName ?: element.namespacedModuleName?.moduleName?.text ?: ""


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
        val moduleName = module.namespacedModuleName ?: return emptyList()
        val out = moduleName.namespace.moduleNameList.toMutableList()
        out.add(moduleName.moduleName)
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
        val functions = interfaceElement.functionList
        return functions.filter {
            it.functionName.const != null
        }
    }

    @JvmStatic
    fun getConstructors(interfaceElement: JsTypeDefClassElement) : List<JsTypeDefFunction> {
        val functions = interfaceElement.functionList
        return functions.filter {
            it.functionName.const != null
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun isStatic(declaration:JsTypeDefInterfaceElement) : Boolean = false

    @JvmStatic
    fun isStatic(declaration:JsTypeDefClassElement) : Boolean
        = declaration.staticKeyword != null


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
    fun getPropertyTypes(declaration:JsTypeDefVariableDeclaration) : List<JsTypeDefType> {
        return declaration.property?.propertyTypes.orEmpty()
    }

    @JvmStatic
    fun isNullable(declaration:JsTypeDefVariableDeclaration) : Boolean {
        return declaration.property?.isNullable.orTrue()
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
    fun getMapName(typeMap:JsTypeDefTypeMapElement) : String? {
        val stub = typeMap.stub
        if (stub != null)
            return stub.mapName
        return typeMap.typeMapName?.text
    }

    @JvmStatic
    fun getKeys(typeMap:JsTypeDefTypeMapElement) : List<String> {
        val stub = typeMap.stub
        if (stub != null)
            return stub.values.map { it.key }
        return typeMap.keyValuePairList.map { it.stringLiteral.content }
    }

    @JvmStatic
    fun getTypesForKey(typeMap: JsTypeDefTypeMapElement, key:String) : InferenceResult? {
        val stub = typeMap.stub
        if (stub != null) {
            return stub.getTypesForKey(key)
        }
        return typeMap.keyValuePairList.filter{ it.key == key }.mapNotNull { it.typesList }.combine()
    }

    @JvmStatic
    fun getKeyValuePairs(typeMap: JsTypeDefTypeMapElement) : List<JsTypeDefKeyValuePair>
            = typeMap.keyValuePairList

    @JvmStatic
    fun getKey(keyValuePair: JsTypeDefKeyValuePair) : String {
        return keyValuePair.stringLiteral.content
    }

    @JvmStatic
    fun getTypesList(keyValuePair: JsTypeDefKeyValuePair) : InferenceResult {
        val nullable = isNullable(keyValuePair)
        val types = keyValuePair.typeList.toJsTypeDefTypeListTypes()
        return InferenceResult(types = types, nullable = nullable)
    }

    @JvmStatic
    fun isNullable(keyValuePair: JsTypeDefKeyValuePair) : Boolean {
        return keyValuePair.typeList.any { it.nullType != null }
    }

    // ============================== //
    // ========= Interface ========== //
    // ============================== //

    @JvmStatic
    fun getClassName(typeInterface: JsTypeDefInterfaceElement) : String
            = typeInterface.stub?.className ?: typeInterface.typeName?.text ?: "?"

    @JvmStatic
    fun getClassName(classDeclaration: JsTypeDefClassElement) : String
            = classDeclaration.stub?.className ?: classDeclaration.typeName?.text ?: "?"

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
                val propertyName = escapedId?.substring(1, escapedId.length - 2) ?: (psiElement as JsTypeDefPropertyName).text

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
        return ahead in EOS_TOKENS || hadLineTerminator
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

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun isStatic(functionDeclaration:JsTypeDefFunctionDeclaration) : Boolean {
        return true
    }

    @JvmStatic
    fun isStatic(function:JsTypeDefFunction) : Boolean {
        return function.staticKeyword != null || function.parent is JsTypeDefFunctionDeclaration
    }

    @JvmStatic
    fun getPropertyNameString(property:JsTypeDefProperty) : String {
        val propertyNameElement = property.propertyName
        val escapedId = propertyNameElement.escapedId
        return escapedId?.text?.substring(1, escapedId.text.length - 2) ?: propertyNameElement.text
    }


    @JvmStatic
    fun getPropertyNameString(declaration:JsTypeDefVariableDeclaration) : String {
        return declaration.property?.propertyNameString.orEmpty()
    }


    @JvmStatic
    fun getFunctionNameString(function:JsTypeDefFunction) : String {
        val functionName = function.functionName
        val escapedId = functionName.escapedId
        return escapedId?.text?.substring(1, escapedId.text.length - 2) ?: functionName.text
    }
}

@Suppress("unused")
val TYPE_SPLIT_REGEX = "\\s*\\|\\s*".toRegex()

val NAMESPACE_SPLITTER_REGEX = "\\s*\\.\\s*".toRegex()