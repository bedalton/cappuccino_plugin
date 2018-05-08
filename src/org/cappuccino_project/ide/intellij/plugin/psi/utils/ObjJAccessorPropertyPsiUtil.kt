package org.cappuccino_project.ide.intellij.plugin.psi.utils

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJAccessorProperty
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import org.cappuccino_project.ide.intellij.plugin.utils.Strings

import java.util.ArrayList
import java.util.Collections

object ObjJAccessorPropertyPsiUtil {

    /**
     * Gets the accessor property variable type
     * @param accessorProperty accessor property psi element
     * @return accessor property var type
     */
    fun getVarType(accessorProperty: ObjJAccessorProperty): String? {
        if (accessorProperty.stub != null) {
            return accessorProperty.stub.varType
        }
        val instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration::class.java)
                ?: return null
        return instanceVariableDeclaration.formalVariableType.text
    }

    /**
     * Tests whether an accessor property is a getter or not
     * @param accessorProperty accessor property
     * @return `true` if accessor virtual method is a getter, `false` otherwise
     */
    fun isGetter(accessorProperty: ObjJAccessorProperty): Boolean {

        if (accessorProperty.stub != null) {
            return accessorProperty.stub.getter != null
        }
        when (accessorProperty.accessorPropertyType.text) {
            "getter", "property" -> return true
            else -> return false
        }
    }


    /**
     * Gets selectors as strings for an accessor property's virtual methods
     * @param accessorProperty accessor property
     * @return selector strings
     */
    fun getSelectorStrings(accessorProperty: ObjJAccessorProperty): List<String> {
        return if (accessorProperty.accessor != null) {
            listOf(accessorProperty.accessor!!.text)
        } else emptyList()
    }

    /**
     * Gets list of selector elements
     * @param accessorProperty accessor property
     * @return list of selector psi elements for accessor property virtual method
     */
    fun getSelectorList(accessorProperty: ObjJAccessorProperty): List<ObjJSelector> {
        return listOf<ObjJSelector>(accessorProperty.accessor)
    }

    /**
     * Gets selector string for an accessor property virtual method
     * @param accessorProperty accessor property
     * @return virtual method selector string
     */
    fun getSelectorString(accessorProperty: ObjJAccessorProperty): String {
        return if (accessorProperty.accessor != null) accessorProperty.accessor!!.getSelectorString(true) else ObjJMethodPsiUtils.EMPTY_SELECTOR
    }

    fun getSetter(accessorProperty: ObjJAccessorProperty): String? {
        if (accessorProperty.stub != null && accessorProperty.stub.setter != null) {
            return accessorProperty.stub.setter
        }
        val instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration::class.java)
        val methodHeader = if (instanceVariableDeclaration != null) ObjJAccessorPropertyPsiUtil.getSetter(instanceVariableDeclaration) else null
        return methodHeader?.selectorString
    }

    /**
     * Build and return setter of an instance variable
     * @param declaration instance variable declaration psi element
     * @return method header stub
     */
    fun getSetter(declaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
        if (declaration.variableName == null) {
            return null
        }
        val varType = declaration.formalVariableType.text
        val variableName = declaration.variableName!!.text
        val variableNameUpperCaseFirst = Strings.upperCaseFirstLetter(variableName)
        var setter: String? = null
        for (property in declaration.accessorPropertyList) {
            if (property.accessorPropertyType.text == "setter") {
                setter = if (property.accessor != null) property.accessor!!.text else "set" + variableNameUpperCaseFirst!!
                break
            } else if (property.accessorPropertyType.text == "property") {
                val accessor = if (property.accessor != null) property.accessor!!.text else null
                if (accessor == null) {
                    setter = "set" + variableNameUpperCaseFirst!!
                } else {
                    if (varType == "BOOL" && accessor.length > 2 && accessor.substring(0, 2) == "is") {
                        setter = "set" + accessor.substring(2)
                    } else {
                        setter = "set" + Strings.upperCaseFirstLetter(accessor)!!
                    }
                }
            }
        }
        if (setter != null) {
            val selectorStrings = listOf(setter)
            val paramTypes = listOf(varType)
            return ObjJMethodHeaderStubImpl(null, declaration.containingClassName, false, selectorStrings, paramTypes, null, true, declaration.shouldResolve())
        }
        return null
    }

    fun getGetter(accessorProperty: ObjJAccessorProperty): String? {
        if (accessorProperty.stub != null && accessorProperty.stub.getter != null) {
            return accessorProperty.stub.getter
        }
        val instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration::class.java)
        val methodHeader = if (instanceVariableDeclaration != null) ObjJAccessorPropertyPsiUtil.getGetter(instanceVariableDeclaration) else null
        return methodHeader?.selectorString
    }

    /**
     * Builds and returns getter of an instance variable
     * @param declaration instance variable declaration psi element
     * @return method header stub
     */
    fun getGetter(declaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
        if (declaration.variableName == null) {
            return null
        }
        val varType = declaration.formalVariableType.text
        val variableName = if (declaration.variableName != null) declaration.variableName!!.text else ""
        var getter: String? = null
        for (property in declaration.accessorPropertyList) {
            if (property.accessorPropertyType.text == "getter") {
                getter = if (property.accessor != null) property.accessor!!.text else variableName
                break
            } else if (property.accessorPropertyType.text == "property") {
                getter = if (property.accessor != null) property.accessor!!.text else variableName
            }
        }
        if (getter != null) {
            val selectorStrings = listOf(getter)
            val paramTypes = emptyList<String>()
            return ObjJMethodHeaderStubImpl(null, declaration.containingClassName, false, selectorStrings, paramTypes, varType, true, declaration.shouldResolve())
        }
        return null
    }

    /**
     * Gets accessor property names for a given accessor property, returns collection in the event property kind = "property"
     * @param variableName variable name
     * @param varType variable type
     * @param property property element
     * @return collection of String method selectors
     */
    fun getAccessorPropertyMethods(variableName: String, varType: String, property: ObjJAccessorProperty): List<String> {
        val propertyMethods = ArrayList<String>()
        //Getter
        val getter = getGetterSelector(variableName, varType, property)
        if (getter != null) {
            propertyMethods.add(getter)
        }
        //Setter
        val setter = getSetterSelector(variableName, varType, property)
        if (setter != null) {
            propertyMethods.add(setter)
        }
        return propertyMethods
    }

    /**
     * Builds and returns the getter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @param property accessor property
     * @return instance variable virtual getter method
     */
    fun getGetterSelector(variableName: String, varType: String, property: ObjJAccessorProperty): String? {
        when (property.accessorPropertyType.text) {
            "getter", "property", "copy", "readonly" -> return (if (property.accessor != null) property.accessor!!.text else variableName) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
            else -> return null
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
    fun getSetterSelector(variableName: String, varType: String): String {
        var variableName = variableName
        var underscorePrefix = ""
        if (variableName.substring(0, 1) == "_") {
            underscorePrefix = "_"
            variableName = variableName.substring(1)
        }
        return if (varType == "BOOL" && variableName.length > 2 && variableName.substring(0, 2) == "is") {
            underscorePrefix + "set" + variableName.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        } else {
            underscorePrefix + "set" + Strings.upperCaseFirstLetter(variableName) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        }
    }

    /**
     * Builds and returns the setter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @param property accessor property
     * @return instance variable virtual setter method
     */
    fun getSetterSelector(variableName: String, varType: String, property: ObjJAccessorProperty): String? {
        var accessor: String? = if (property.accessor != null) property.accessor!!.text else null

        //Accessor is setter specific
        if (accessor != null && property.accessorPropertyType.text == "setter") {
            return accessor + ObjJMethodPsiUtils.SELECTOR_SYMBOL
        }
        //Set accessor value to variableName if not set in property
        accessor = if (accessor != null) accessor else variableName
        //Determine underscore prefix, and substring accessor
        var underscorePrefix = ""
        if (accessor.substring(0, 1) == "_") {
            underscorePrefix = "_"
            accessor = accessor.substring(1)
        }

        when (property.accessorPropertyType.text) {
            "setter" -> return underscorePrefix + "set" + Strings.upperCaseFirstLetter(accessor) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
            "property" -> return if (varType == "BOOL" && accessor.length > 2 && accessor.substring(0, 2) == "is") {
                underscorePrefix + "set" + accessor.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
            } else {
                underscorePrefix + "set" + Strings.upperCaseFirstLetter(accessor) + ObjJMethodPsiUtils.SELECTOR_SYMBOL
            }
            else -> return null
        }
    }

}
