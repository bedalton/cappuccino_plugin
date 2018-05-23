package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.utils.Strings
import cappuccino.ide.intellij.plugin.utils.upperCaseFirstLetter
import java.util.*

/**
 * Gets the accessor property variable type
 * @param accessorProperty accessor property psi element
 * @return accessor property var type
 */
fun getVarType(accessorProperty:ObjJAccessorProperty): String? =
    accessorProperty.stub.varType ?: accessorProperty.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.formalVariableType?.text

/**
 * Tests whether an accessor property is a getter or not
 * @param accessorProperty accessor property
 * @return `true` if accessor virtual method is a getter, `false` otherwise
 */
fun isGetter(accessorProperty:ObjJAccessorProperty): Boolean {
    val stub = accessorProperty.stub
    if (stub != null) {
        return stub.getter != null
    }
    return when (accessorProperty.accessorPropertyType.text) {
        "getter", "property" -> true
        else -> false
    }
}


/**
 * Gets selectors as strings for an accessor property's virtual methods
 * @param accessorProperty accessor property
 * @return selector strings
 */
fun getSelectorStrings(accessorProperty:ObjJAccessorProperty): List<String> {
    val selector = accessorProperty.accessor?.text
    return if (selector != null) listOf(selector) else listOf()
}

/**
 * Gets list of selector elements
 * @param accessorProperty accessor property
 * @return list of selector psi elements for accessor property virtual method
 */
fun getSelectorList(accessorProperty: ObjJAccessorProperty): List<ObjJSelector> {
    val selector = accessorProperty.accessor
    return if (selector != null) listOf(selector) else listOf()
}

/**
 * Gets selector string for an accessor property virtual method
 * @param accessorProperty accessor property
 * @return virtual method selector string
 */
fun getSelectorString(accessorProperty:ObjJAccessorProperty): String =
    accessorProperty.accessor?.getSelectorString(true) ?: ObjJMethodPsiUtils.EMPTY_SELECTOR


fun getSetter(accessorProperty:ObjJAccessorProperty): String? =
    accessorProperty.stub?.setter ?: accessorProperty.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.setter?.selectorString

/**
 * Build and return setter of an instance variable
 * @param variableDeclaration instance variable declaration psi element
 * @return method header stub
 */
fun getSetter(variableDeclaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
    if (variableDeclaration.variableName == null) {
        return null
    }
    val varType = variableDeclaration.formalVariableType.text
    val variableName = variableDeclaration.variableName!!.text
    val variableNameUpperCaseFirst = Strings.upperCaseFirstLetter(variableName)
    var setter: String? = null
    for (accessorProperty in variableDeclaration.accessorPropertyList) {
        if (accessorProperty.accessorPropertyType.text == "setter") {
            setter = accessorProperty.accessor?.text ?: "set" + variableNameUpperCaseFirst!!
            break
        } else if (accessorProperty.accessorPropertyType.text == "property") {
            val accessor = accessorProperty.accessor?.text
            setter = if (accessor == null) {
                "set" + variableNameUpperCaseFirst!!
            } else {
                if (varType == "BOOL" && accessor.length > 2 && accessor.substring(0, 2) == "is") {
                    "set" + accessor.substring(2)
                } else {
                    "set" + Strings.upperCaseFirstLetter(accessor)!!
                }
            }
        }
    }
    if (setter != null) {
        val selectorStrings = listOf(setter)
        val paramTypes = listOf(varType)
        return ObjJMethodHeaderStubImpl(null, variableDeclaration.containingClassName, false, selectorStrings, paramTypes, null, true, variableDeclaration.shouldResolve())
    }
    return null
}


fun getGetter(accessorProperty:ObjJAccessorProperty): String? =
        accessorProperty.stub?.setter ?: accessorProperty.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.getter?.selectorString


/**
 * Builds and returns getter of an instance variable
 * @param variableDeclaration instance variable variableDeclaration psi element
 * @return method header stub
 */
fun getGetter(variableDeclaration:ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
    val varType = variableDeclaration.stub?.varType ?: variableDeclaration.formalVariableType.text
    val getter: String? =   variableDeclaration.stub?.getter ?:
                            getGetterFromAccessorPropertyList(variableDeclaration.accessorPropertyList) ?:
                            variableDeclaration.stub?.variableName ?:
                            variableDeclaration.variableName?.text ?:
                            return null
    val selectorStrings : List<String> = listOf(getter!!)
    val paramTypes = listOf(varType)
    return ObjJMethodHeaderStubImpl(null, variableDeclaration.containingClassName, false, selectorStrings, paramTypes, varType, true, variableDeclaration.shouldResolve())
}

private fun getGetterFromAccessorPropertyList(accessorProperties:List<ObjJAccessorProperty>) : String? {
    var getter : String? = null
    accessorProperties.forEach (each@{ accessorProperty ->
        val tempGetter = accessorProperty.stub?.getter ?: accessorProperty.accessor?.text
        when (accessorProperty.accessorPropertyType.text) {
            "getter" -> {
                getter = tempGetter
                return@each
            }
            else -> getter = tempGetter
        }
    })
    return getter
}

/**
 * Gets accessor property names for a given accessor property, returns collection in the event property kind = "property"
 * @param variableName variable name
 * @param varType variable type
 * @param accessorProperty property element
 * @return collection of String method selectors
 */
fun getAccessorPropertyMethods(variableName: String, varType: String, accessorProperty: ObjJAccessorProperty): List<String> {
    val propertyMethods = ArrayList<String>()
    //Getter
    val getter = getGetterSelector(variableName, varType, accessorProperty)
    if (getter != null) {
        propertyMethods.add(getter)
    }
    //Setter
    val setter = getSetterSelector(variableName, varType, accessorProperty)
    if (setter != null) {
        propertyMethods.add(setter)
    }
    return propertyMethods
}

/**
 * Builds and returns the getter virtual method selector
 * @param variableName var name as string
 * @param varType var type as string
 * @param accessorProperty accessor property
 * @return instance variable virtual getter method
 */
fun getGetterSelector(variableName: String, varType: String, accessorProperty: ObjJAccessorProperty): String? {
    return accessorProperty.stub?.getter ?: when (accessorProperty.accessorPropertyType.text) {
        "getter", "property", "copy", "readonly" -> (accessorProperty.accessor?.text ?: variableName) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        else -> null
    }
}

/**
 * Builds and returns the getter virtual method selector
 * @param variableName var name as string
 * @param varType var type as string
 * @return instance variable virtual getter method
 */
fun getGetterSelector(variableName: String, varType: String): String {
    return variableName + ObjJMethodPsiUtils.SELECTOR_SYMBOL
}


/**
 * Builds and returns the setter virtual method selector
 * @param variableName var name as string
 * @param varType var type as string
 * @return instance variable virtual setter method
 */
fun getSetterSelector(variableNameIn: String, varType: String): String {
    var variableName = variableNameIn
    var underscorePrefix = ""
    if (variableName.substring(0, 1) == "_") {
        underscorePrefix = "_"
        variableName = variableName.substring(1)
    }
    return if (varType == "BOOL" && variableName.length > 2 && variableName.substring(0, 2) == "is") {
        underscorePrefix + "set" + variableName.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
    } else {
        underscorePrefix + "set" + variableName.upperCaseFirstLetter() + ObjJMethodPsiUtils.SELECTOR_SYMBOL
    }
}

/**
 * Builds and returns the setter virtual method selector
 * @param variableName var name as string
 * @param varType var type as string
 * @param accessorProperty accessor property
 * @return instance variable virtual setter method
 */
fun getSetterSelector(variableName: String, varType: String, accessorProperty: ObjJAccessorProperty): String? {
    //Check stub for setter
    val setter = accessorProperty.stub?.setter
    if (setter != null) {
        return setter
    }
    //get accessor string if available
    var accessor: String? = accessorProperty.accessor?.text
    val propertyType = accessorProperty.accessorPropertyType.text;
    //Accessor is setter specific ie. @accessor(setter=setValue)
    //Return accessor as is
    if (accessor != null && propertyType == "setter") {
        return accessor + ObjJMethodPsiUtils.SELECTOR_SYMBOL
    }

    //Set accessor value to variableName if property name is not set ie @accessors only
    if (accessor == null) {
        accessor = variableName
    }
    //Determine underscore prefix, and substring accessor
    var underscorePrefix = ""
    if (accessor.substring(0, 1) == "_") {
        underscorePrefix = "_"
        accessor = accessor.substring(1)
    }

    return when (propertyType) {
        "setter" -> underscorePrefix + "set" + accessor.upperCaseFirstLetter() + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        "property" -> if (varType == "BOOL" && accessor.length > 2 && accessor.substring(0, 2) == "is") {
            underscorePrefix + "set" + accessor.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        } else {
            underscorePrefix + "set" + accessor.upperCaseFirstLetter() + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        }
        else -> null
    }
}