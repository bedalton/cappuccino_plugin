package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJAccessorProperty;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJAccessorPropertyPsiUtil {

    /**
     * Gets the accessor property variable type
     * @param accessorProperty accessor property psi element
     * @return accessor property var type
     */
    @Nullable
    public static String getVarType(ObjJAccessorProperty accessorProperty) {
        if (accessorProperty.getStub() != null) {
            return accessorProperty.getStub().getVarType();
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration.class);
        if (instanceVariableDeclaration == null) {
            return null;
        }
        return instanceVariableDeclaration.getFormalVariableType().getText();
    }

    /**
     * Tests whether an accessor property is a getter or not
     * @param accessorProperty accessor property
     * @return <code>true</code> if accessor virtual method is a getter, <code>false</code> otherwise
     */
    public static boolean isGetter(ObjJAccessorProperty accessorProperty) {

        if (accessorProperty.getStub() != null) {
            return accessorProperty.getStub().getGetter() != null;
        }
        switch (accessorProperty.getAccessorPropertyType().getText()) {
            case "getter":
            case "property":
                return true;
            default:
                return false;
        }
    }


    /**
     * Gets selectors as strings for an accessor property's virtual methods
     * @param accessorProperty accessor property
     * @return selector strings
     */
    @NotNull
    public static List<String> getSelectorStrings(ObjJAccessorProperty accessorProperty) {
        if (accessorProperty.getAccessor() != null) {
            return Collections.singletonList(accessorProperty.getAccessor().getText());
        }
        return Collections.emptyList();
    }

    /**
     * Gets list of selector elements
     * @param accessorProperty accessor property
     * @return list of selector psi elements for accessor property virtual method
     */
    @NotNull
    public static List<ObjJSelector> getSelectorList(ObjJAccessorProperty accessorProperty) {
        return Collections.singletonList(accessorProperty.getAccessor());
    }

    /**
     * Gets selector string for an accessor property virtual method
     * @param accessorProperty accessor property
     * @return virtual method selector string
     */
    @NotNull
    public static String getSelectorString(ObjJAccessorProperty accessorProperty) {
        return accessorProperty.getAccessor() != null ? accessorProperty.getAccessor().getSelectorString(true) : ObjJMethodPsiUtils.EMPTY_SELECTOR;
    }

    @Nullable
    public static String getSetter(ObjJAccessorProperty accessorProperty) {
        if (accessorProperty.getStub() != null && accessorProperty.getStub().getSetter() != null) {
            return accessorProperty.getStub().getSetter();
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration.class);
        ObjJMethodHeaderStub methodHeader = instanceVariableDeclaration != null ? ObjJAccessorPropertyPsiUtil.getSetter(instanceVariableDeclaration) : null;
        return methodHeader != null ? methodHeader.getSelectorString() : null;
    }

    /**
     * Build and return setter of an instance variable
     * @param declaration instance variable declaration psi element
     * @return method header stub
     */
    @Nullable
    public static ObjJMethodHeaderStub getSetter(ObjJInstanceVariableDeclaration declaration) {
        if (declaration.getVariableName() == null) {
            return null;
        }
        String varType = declaration.getFormalVariableType().getText();
        String variableName = declaration.getVariableName().getText();
        String variableNameUpperCaseFirst = Strings.upperCaseFirstLetter(variableName);
        String setter = null;
        for (ObjJAccessorProperty property : declaration.getAccessorPropertyList()) {
            if (property.getAccessorPropertyType().getText().equals("setter")) {
                setter = property.getAccessor() != null ? property.getAccessor().getText() : "set"+variableNameUpperCaseFirst;
                break;
            } else if (property.getAccessorPropertyType().getText().equals("property")) {
                String accessor = property.getAccessor() != null ? property.getAccessor().getText() : null;
                if (accessor == null) {
                    setter = "set"+variableNameUpperCaseFirst;
                } else {
                    if (varType.equals("BOOL") && accessor.length() > 2 && accessor.substring(0, 2).equals("is")) {
                        setter = "set" + accessor.substring(2);
                    } else {
                        setter = "set" + Strings.upperCaseFirstLetter(accessor);
                    }
                }
            }
        }
        if (setter != null) {
            List<String> selectorStrings = Collections.singletonList(setter);
            List<String> paramTypes = Collections.singletonList(varType);
            return new ObjJMethodHeaderStubImpl(null, declaration.getContainingClassName(), false, selectorStrings, paramTypes, null, true);
        }
        return null;
    }

    @Nullable
    public static String getGetter(ObjJAccessorProperty accessorProperty) {
        if (accessorProperty.getStub() != null && accessorProperty.getStub().getGetter() != null) {
            return accessorProperty.getStub().getGetter();
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration.class);
        ObjJMethodHeaderStub methodHeader = instanceVariableDeclaration != null ? ObjJAccessorPropertyPsiUtil.getGetter(instanceVariableDeclaration) : null;
        return methodHeader != null ? methodHeader.getSelectorString() : null;
    }

    /**
     * Builds and returns getter of an instance variable
     * @param declaration instance variable declaration psi element
     * @return method header stub
     */
    @Nullable
    public static ObjJMethodHeaderStub getGetter(ObjJInstanceVariableDeclaration declaration) {
        if (declaration.getVariableName() == null) {
            return null;
        }
        String varType = declaration.getFormalVariableType().getText();
        String variableName = declaration.getVariableName() != null ? declaration.getVariableName().getText() : "";
        String getter = null;
        for (ObjJAccessorProperty property : declaration.getAccessorPropertyList()) {
            if (property.getAccessorPropertyType().getText().equals("getter")) {
                getter = property.getAccessor() != null ? property.getAccessor().getText() : variableName;
                break;
            } else if (property.getAccessorPropertyType().getText().equals("property")) {
                getter = property.getAccessor() != null ? property.getAccessor().getText() : variableName;
            }
        }
        if (getter != null) {
            List<String> selectorStrings = Collections.singletonList(getter);
            List<String> paramTypes = Collections.emptyList();
            return new ObjJMethodHeaderStubImpl(null, declaration.getContainingClassName(), false, selectorStrings, paramTypes, varType, true);
        }
        return null;
    }

    /**
     * Gets accessor property names for a given accessor property, returns collection in the event property kind = "property"
     * @param variableName variable name
     * @param varType variable type
     * @param property property element
     * @return collection of String method selectors
     */
    @NotNull
    public static List<String> getAccessorPropertyMethods(@NotNull String variableName, @NotNull String varType, ObjJAccessorProperty property) {
        final List<String> propertyMethods = new ArrayList<>();
        //Getter
        final String getter = getGetterSelector(variableName, varType, property);
        if (getter != null) {
            propertyMethods.add(getter);
        }
        //Setter
        final String setter = getSetterSelector(variableName,varType, property);
        if (setter != null) {
            propertyMethods.add(setter);
        }
        return propertyMethods;
    }

    /**
     * Builds and returns the getter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @param property accessor property
     * @return instance variable virtual getter method
     */
    @Nullable
    public static String getGetterSelector(@NotNull String variableName, @SuppressWarnings("unused")@NotNull String varType, ObjJAccessorProperty property) {
        switch (property.getAccessorPropertyType().getText()) {
            case "getter":
            case "property":
            case "copy":
            case "readonly":
                return (property.getAccessor() != null ? property.getAccessor().getText() : variableName) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
            default:
                return null;
        }
    }
    /**
     * Builds and returns the getter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @return instance variable virtual getter method
     */
    @NotNull
    public static String getGetterSelector(@NotNull String variableName, @SuppressWarnings("unused")@NotNull String varType) {
        return variableName + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
    }


    /**
     * Builds and returns the setter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @return instance variable virtual setter method
     */
    @NotNull
    public static String getSetterSelector(@NotNull String variableName, @NotNull String varType) {
        String underscorePrefix = "";
        if (variableName.substring(0,1).equals("_")) {
            underscorePrefix = "_";
            variableName = variableName.substring(1);
        }
        if (varType.equals("BOOL") && variableName.length() > 2 && variableName.substring(0, 2).equals("is")) {
            return underscorePrefix+"set" + variableName.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
        } else {
            return underscorePrefix+"set" + Strings.upperCaseFirstLetter(variableName) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
        }
    }

    /**
     * Builds and returns the setter virtual method selector
     * @param variableName var name as string
     * @param varType var type as string
     * @param property accessor property
     * @return instance variable virtual setter method
     */
    @Nullable
    public static String getSetterSelector(@NotNull String variableName, @NotNull String varType, @NotNull ObjJAccessorProperty property) {
        String accessor = property.getAccessor() != null ? property.getAccessor().getText() : null;

        //Accessor is setter specific
        if (accessor != null && property.getAccessorPropertyType().getText().equals("setter")) {
            return accessor + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
        }
        //Set accessor value to variableName if not set in property
        accessor = accessor != null ? accessor : variableName;
        //Determine underscore prefix, and substring accessor
        String underscorePrefix = "";
        if(accessor.substring(0,1).equals("_")) {
            underscorePrefix = "_";
            accessor = accessor.substring(1);
        }

        switch (property.getAccessorPropertyType().getText()) {
            case "setter":
                return underscorePrefix + "set" + Strings.upperCaseFirstLetter(accessor) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
            case "property":
                if (varType.equals("BOOL") && accessor.length() > 2 && accessor.substring(0, 2).equals("is")) {
                    return underscorePrefix + "set" + accessor.substring(2) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
                } else {
                    return underscorePrefix + "set" + Strings.upperCaseFirstLetter(accessor) + ObjJMethodPsiUtils.SELECTOR_SYMBOL;
                }
            default:
                return null;
        }
    }

}
