package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.hints.ObjJFunctionDescription
import cappuccino.ide.intellij.plugin.hints.description
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListGenericType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.TypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasNamespace
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasGenerics
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*
import cappuccino.ide.intellij.plugin.jstypedef.references.JsTypeDefModuleNameReference
import cappuccino.ide.intellij.plugin.jstypedef.references.JsTypeDefTypeGenericsKeyReference
import cappuccino.ide.intellij.plugin.jstypedef.references.JsTypeDefTypeNameReference
import cappuccino.ide.intellij.plugin.jstypedef.references.JsTypeDefTypeMapNameReference
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDEF_CLASS_NAME
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNode
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
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

    @JvmStatic
    fun getName(name:JsTypeDefTypeMapName) : String {
        return name.text
    }

    @JvmStatic
    fun getName(key:JsTypeDefGenericsKey) : String {
        return key.text
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

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun setName(name:JsTypeDefTypeMapName, newName:String) : PsiElement {
        return name
    }

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun setName(key:JsTypeDefGenericsKey, newName:String) : PsiElement {
        return key
    }


    // ============================== //
    // ========== Namespace ========= //
    // ============================== //
    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefElement) : String {
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName ?: ""
    }

    @JvmStatic
    fun getEnclosingNamespace(element: JsTypeDefProperty) : String =
            element.stub?.enclosingNamespace
                ?: (element.parent as? JsTypeDefVariableDeclaration)?.enclosingNamespace
                ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                ?: ""

    @JvmStatic
    fun getEnclosingNamespace(element:JsTypeDefVariableDeclaration) : String =
            element.stub?.enclosingNamespace
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName
                    ?: ""
    @JvmStatic
    fun getEnclosingNamespaceComponents(element: JsTypeDefProperty) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: (element.parent as? JsTypeDefVariableDeclaration)?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()

    @JvmStatic
    fun getEnclosingNamespaceComponents(element:JsTypeDefVariableDeclaration) : List<String> =
            element.stub?.enclosingNamespaceComponents
                    ?: element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents
                    ?: listOf()
    @JvmStatic
    fun getNamespaceComponents(element: JsTypeDefProperty) : List<String>
            = element.stub?.namespaceComponents ?: (element.enclosingNamespaceComponents + element.propertyNameString)

    @JvmStatic
    fun getNamespaceComponents(element:JsTypeDefVariableDeclaration) : List<String>
            = element.stub?.namespaceComponents ?: element.enclosingNamespaceComponents + element.property?.propertyNameString.orEmpty()

    @JvmStatic
    fun getNamespaceComponent(element: JsTypeDefProperty) : String
            = element.stub?.propertyName ?: element.propertyNameString

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefVariableDeclaration) : String
            = element.stub?.variableName ?: element.property?.namespaceComponent.orEmpty()

    @JvmStatic
    fun getEnclosingNamespace(elementIn:JsTypeDefFunction) : String {
        val stub = elementIn.stub
        if (stub != null)
            return stub.enclosingNamespace
        val element = elementIn.parent as? JsTypeDefFunctionDeclaration ?: elementIn
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespacedName ?: ""
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
        return element.getParentOfType(JsTypeDefHasNamespace::class.java)?.namespaceComponents ?: emptyList()
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
        val stubbedNamespace = module.stub?.namespaceComponents
        if (stubbedNamespace != null) {
            return stubbedNamespace
        }
        val components = getNamespaceComponents(module).toMutableList()
        components.removeAt(components.lastIndex)
        return components
    }

    @JvmStatic
    fun getNamespaceComponents(module:JsTypeDefModule) : List<String> {
        val stubbedNamespace = module.stub?.namespaceComponents
        if (stubbedNamespace != null)
            return stubbedNamespace
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
        val stubbedNamespace = moduleName.stub?.enclosingNamespaceComponents
        if (stubbedNamespace != null)
            return stubbedNamespace
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
        return moduleName.stub?.enclosingNamespace ?: getEnclosingNamespaceComponents(moduleName).joinToString(".")
    }

    @JvmStatic
    fun getNamespaceComponents(moduleName:JsTypeDefModuleName) : List<String>
            = moduleName.stub?.namespaceComponents ?: getEnclosingNamespaceComponents(moduleName) + moduleName.text

    @JvmStatic
    fun getFullyNamespacedName(moduleName:JsTypeDefModuleName) : String
            = moduleName.stub?.fullyNamespacedName ?: (getEnclosingNamespaceComponents(moduleName) + moduleName.text)
                .joinToString(".")

    @JvmStatic
    fun getNamespaceComponent(element:JsTypeDefModuleName) : String
            = element.stub?.moduleName ?: element.text

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
/*
    @JvmStatic
    fun getConstructors(interfaceElement: JsTypeDefInterfaceElement) : List<JsTypeDefFunction> {
        val functions = interfaceElement.interfaceConstructorList
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
*/
    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun isStatic(declaration:JsTypeDefInterfaceElement) : Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun isStatic(declaration:JsTypeDefClassElement) : Boolean
        = true


    // ============================== //
    // ========= Properties ========= //
    // ============================== //

    @JvmStatic
    fun getPropertyTypes(property: JsTypeDefProperty) : List<JsTypeDefType> {
        return property.typeList
    }

    @JvmStatic
    fun isNullable(property: JsTypeDefProperty) : Boolean {
        return property.stub?.nullable ?: property.nullable != null || isNullable(property.typeList)
    }


    @JvmStatic
    fun isNullable(property: JsTypeDefArgument) : Boolean {
        return property.nullable != null || isNullable(property.typeList)
    }

    @JvmStatic
    fun getPropertyTypes(declaration:JsTypeDefVariableDeclaration) : List<JsTypeDefType> {
        return declaration.property?.propertyTypes.orEmpty()
    }

    @JvmStatic
    fun isNullable(declaration:JsTypeDefVariableDeclaration) : Boolean {
        return declaration.stub?.nullable ?: declaration.property?.isNullable.orTrue()
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
        return typeMap.stub?.mapName ?: typeMap.typeMapName?.text
    }

    @JvmStatic
    fun getKeys(typeMap:JsTypeDefTypeMapElement) : List<String> {
        return typeMap.stub?.values?.map { it.key }
                ?: typeMap.keyValuePairList.map { it.stringLiteral.stringValue }
    }

    @JvmStatic
    fun getTypesForKey(typeMap: JsTypeDefTypeMapElement, key:String) : InferenceResult? {
        return typeMap.stub?.getTypesForKey(key)
                ?: typeMap.keyValuePairList.filter{ it.key == key }.mapNotNull { it.typesList }.combine()
    }

    @JvmStatic
    fun getKeyValuePairs(typeMap: JsTypeDefTypeMapElement) : List<JsTypeDefKeyValuePair>
            = typeMap.keyValuePairList

    @JvmStatic
    fun getKey(keyValuePair: JsTypeDefKeyValuePair) : String {
        return keyValuePair.stringLiteral.stringValue
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
    // ========= TypeAlias ========== //
    // ============================== //

    @JvmStatic
    fun getTypeNameString(typeAlias:JsTypeDefTypeAlias) : String {
        return typeAlias.stub?.typeName ?: typeAlias.typeName?.text ?: UNDEF_CLASS_NAME
    }

    @JvmStatic
    fun getTypesList(typeAlias:JsTypeDefTypeAlias) : InferenceResult {
        return typeAlias.stub?.types ?: InferenceResult(types = typeAlias.typeList.toJsTypeDefTypeListTypes())
    }


    // ============================== //
    // ====== Class/Interface ======= //
    // ============================== //

    @JvmStatic
    fun getClassName(typeInterface: JsTypeDefInterfaceElement) : String
            = typeInterface.stub?.className ?: typeInterface.typeName?.text ?: "?"

    @JvmStatic
    fun getClassName(classDeclaration: JsTypeDefClassElement) : String
            = classDeclaration.stub?.className ?: classDeclaration.typeName?.text ?: "?"

    @JvmStatic
    fun isSilent(classDeclaration: JsTypeDefClassElement) : Boolean {
        return classDeclaration.stub?.isSilent ?: classDeclaration.atSilent != null
    }

    @JvmStatic
    fun isSilent(classDeclaration: JsTypeDefInterfaceElement) : Boolean {
        return classDeclaration.stub?.isSilent ?: classDeclaration.atSilent != null
    }

    @JvmStatic
    fun isQuiet(classDeclaration: JsTypeDefInterfaceElement) : Boolean {
        return classDeclaration.stub?.isQuiet ?: classDeclaration.atQuiet != null
    }

    @JvmStatic
    fun isQuiet(classDeclaration: JsTypeDefClassElement) : Boolean {
        return classDeclaration.stub?.isQuiet ?: classDeclaration.atQuiet != null
    }
    // ============================== //
    // ======== Descriptions ======== //
    // ============================== //

    @JvmStatic
    fun getDescription(function:JsTypeDefFunction): ObjJFunctionDescription {
        return (function.stub?.asJsFunctionType ?: function.toJsTypeListType()).description
    }

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
                val propertyName = escapedId?.substring(1, escapedId.length - 2) ?: psiElement.text

                "property $propertyName"
            }
            else -> ""
        }
    }

    // ============================== //
    // ========== Property ========== //
    // ============================== //

    @JvmStatic
    fun isStatic(property:JsTypeDefProperty) : Boolean {
        return property.stub?.static ?: property.staticKeyword != null || (property.parent is JsTypeDefVariableDeclaration)
    }
    @JvmStatic
    fun getPropertyNameString(property: JsTypeDefProperty) : String {
        val stubName = property.stub?.propertyName
        if (stubName != null)
            return stubName
        val propertyNameElement = property.propertyName
        val escapedId = propertyNameElement?.escapedId
        return escapedId?.text?.substring(1, escapedId.text.length - 2) ?: propertyNameElement?.stringLiteral?.stringValue ?: propertyNameElement?.text.orEmpty()
    }

    @JvmStatic
    fun getPropertyNameString(declaration:JsTypeDefVariableDeclaration) : String {
        return declaration.stub?.variableName ?: declaration.property?.propertyNameString.orEmpty()
    }

    @JvmStatic
    fun isSilent(declaration:JsTypeDefVariableDeclaration) : Boolean {
        return declaration.stub?.isSilent ?: declaration.atSilent != null
    }

    @JvmStatic
    fun isQuiet(declaration:JsTypeDefVariableDeclaration) : Boolean {
        return declaration.stub?.isQuiet ?: declaration.atQuiet != null
    }

    @JvmStatic
    fun isQuiet(property:JsTypeDefProperty):Boolean {
        return property.stub?.isQuiet != null || property.atQuiet != null || (property.parent as? JsTypeDefVariableDeclaration)?.isQuiet ?: false
    }

    @JvmStatic
    fun isSilent(property:JsTypeDefProperty):Boolean {
        return property.stub?.isSilent != null || property.atSilent != null || (property.parent as? JsTypeDefVariableDeclaration)?.isSilent ?: false
    }


    // ============================== //
    // ========= Functions ========== //
    // ============================== //

    @JvmStatic
    fun getVarArgs(argument: JsTypeDefArgument) : Boolean {
        return argument.ellipsis != null
    }

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun isStatic(functionDeclaration:JsTypeDefFunctionDeclaration) : Boolean {
        return true
    }

    @JvmStatic
    fun isStatic(function:JsTypeDefFunction) : Boolean {
        return function.stub?.static ?: function.staticKeyword != null || function.parent is JsTypeDefFunctionDeclaration
    }

    @JvmStatic
    fun getArgumentNameString(property: JsTypeDefArgument) : String {
        val propertyNameElement = property.propertyName
        val escapedId = propertyNameElement.escapedId
        return escapedId?.text?.substring(1, escapedId.text.length - 2) ?: propertyNameElement.text
    }

    @JvmStatic
    fun getFunctionNameString(function:JsTypeDefFunction) : String {
        val stubName = function.stub?.functionName
        if (stubName != null)
            return stubName
        val functionName = function.functionName
        val escapedId = functionName.escapedId
        return escapedId?.text?.substring(1, escapedId.text.length - 2) ?: functionName.text
    }


    @JvmStatic
    fun isSilent(declaration:JsTypeDefFunction) : Boolean {
        return declaration.stub?.isSilent ?: declaration.atSilent != null || (declaration.parent as? JsTypeDefFunctionDeclaration)?.atSilent != null
    }

    @JvmStatic
    fun isQuiet(declaration:JsTypeDefFunction) : Boolean {
        return declaration.stub?.isQuiet ?: declaration.atQuiet != null || (declaration.parent as? JsTypeDefFunctionDeclaration)?.atQuiet != null
    }

    @JvmStatic
    fun isSilent(declaration:JsTypeDefFunctionDeclaration) : Boolean {
        return declaration.atSilent != null
    }

    @JvmStatic
    fun isQuiet(declaration:JsTypeDefFunctionDeclaration) : Boolean {
        return declaration.atQuiet != null
    }


    // ============================== //
    // ========= References ========= //
    // ============================== //

    @JvmStatic
    fun getReference(name:JsTypeDefModuleName) : PsiPolyVariantReference {
        return JsTypeDefModuleNameReference(name)
    }

    @JvmStatic
    fun getReference(name:JsTypeDefTypeMapName) : PsiPolyVariantReference {
        return JsTypeDefTypeMapNameReference(name)
    }

    @JvmStatic
    fun getReference(name:JsTypeDefTypeName) : PsiPolyVariantReference {
        return JsTypeDefTypeNameReference(name)
    }

    @JvmStatic
    fun getReference(name:JsTypeDefGenericsKey) : PsiPolyVariantReference {
        return JsTypeDefTypeGenericsKeyReference(name)
    }

    // ============================== //
    // ========== Literals ========== //
    // ============================== //
    @JvmStatic
    fun getStringValue(stringLiteral: JsTypeDefStringLiteral): String {
        val rawText = stringLiteral.text
        val quotationMark: String = if (rawText.startsWith("\"")) "\"" else if (rawText.startsWith("'")) "'" else return rawText
        val outText = if (rawText.startsWith(quotationMark)) rawText.substring(1) else rawText
        val offset = if (outText.endsWith(quotationMark)) 1 else 0
        return if (outText.endsWith(quotationMark)) outText.substring(0, outText.length - offset) else outText

    }

    // ============================== //
    // ======== Nodes and PSI ======= //
    // ============================== //

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

    // ============================== //
    // ========= Generics =========== //
    // ============================== //

    @JvmStatic
    fun getEnclosingGenerics(element:PsiElement) : List<String> {
        val out = mutableListOf<String>()
        var parent:JsTypeDefHasGenerics? = element.getParentOfType(JsTypeDefHasGenerics::class.java) ?: return emptyList()
        while (parent != null) {
            out.addAll(parent.genericsKeys.orEmpty().map { it.key })
            parent = parent.getParentOfType(JsTypeDefHasGenerics::class.java)
        }
        return out
    }

    @JvmStatic
    fun getGenericsKeys(declaration:JsTypeDefFunctionDeclaration) : Set<JsTypeListGenericType>? {
        return declaration.function?.genericsKeys
    }

    @JvmStatic
    fun getGenericsKeys(function:JsTypeDefFunction) : Set<JsTypeListGenericType>? {
        val genericsKeys = function.stub?.genericsKeys
        if (genericsKeys != null)
            return genericsKeys.ifEmpty { null }
        return function.genericTypeTypes?.asJsTypeListGenericType()
    }

    @JvmStatic
    fun getGenericsKeys(declaration: JsTypeDefClassDeclaration<*,*>) : Set<JsTypeListGenericType>? {
        val stub = declaration.stub
        // If Stub is not null, then the element would know whether or not
        // to keep the list null or empty.
        // Cannot check for null or empty on list
        // As null is a valid return value
        if (stub != null)
            return stub.genericsKeys
        val baseGenerics = declaration.genericTypeTypes?.asJsTypeListGenericType().orEmpty().toSet()
        val superTypes = declaration.extendsStatement?.typeList.toJsTypeDefTypeListTypes()
        val includedGenerics = superTypes.mapNotNull { it as? JsTypeListGenericType }.toSet()
        return baseGenerics + includedGenerics
    }

    @Suppress("unused")
    fun PsiElement?.hasNodeType(elementType: IElementType): Boolean {
        return this != null && this.node.elementType === elementType
    }

    @JvmStatic
    fun asJsTypeListGenericType(genericTypeTypes: JsTypeDefGenericTypeTypes) : Set<JsTypeListGenericType>? {
        return genericTypeTypes.genericTypesTypeList.mapNotNull {
            val key = it.genericsKey?.text ?: return@mapNotNull null
            val types = it.typeList.toJsTypeDefTypeListTypes().ifEmpty { null }
            JsTypeListGenericType(key, types)
        }.toSet().ifEmpty { null }
    }

}

@Suppress("unused")
val TYPE_SPLIT_REGEX = "\\s*\\|\\s*".toRegex()

val NAMESPACE_SPLITTER_REGEX = "\\s*\\.\\s*".toRegex()