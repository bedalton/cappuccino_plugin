package org.cappuccino_project.ide.intellij.plugin.psi.types;

import com.intellij.psi.tree.IElementType;
import static org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.*;
import org.jetbrains.annotations.NotNull;

public interface ObjJElementTypeFactory {
    public static IElementType factory(@NotNull String name) {
        switch(name) {
            case "ObjJ_ACCESSOR_PROPERTY": return ACCESSOR_PROPERTY;
            case "ObjJ_FUNCTION_DECLARATION": return FUNCTION_DECLARATION;
            case "ObjJ_FUNCTION_LITERAL": return FUNCTION_LITERAL;
            case "ObjJ_GLOBAL_VARIABLE_DECLARATION": return GLOBAL_VARIABLE;
            case "ObjJ_IMPLEMENTATION_DECLARATION":return IMPLEMENTATION;
            case "ObjJ_IMPORT_FILE": return IMPORT_FILE;
            case "ObjJ_IMPORT_FRAMEWORK": return IMPORT_FRAMEWORK;
            case "ObjJ_INCLUDE_FILE": return INCLUDE_FILE;
            case "ObjJ_INCLUDE_FRAMEWORK": return INCLUDE_FRAMEWORK;
            case "ObjJ_INSTANCE_VARIABLE_DECLARATION": return INSTANCE_VAR;
            case "ObjJ_METHOD_CALL": return METHOD_CALL;
            case "ObjJ_METHOD_HEADER":return METHOD_HEADER;
            case "ObjJ_PREPROCESSOR_DEFINE_FUNCTION": return PREPROCESSOR_FUNCTION;
            case "ObjJ_PROTOCOL_DECLARATION":return PROTOCOL;
            case "ObjJ_SELECTOR_LITERAL": return SELECTOR_LITERAL;
            case "ObjJ_VAR_TYPE_ID": return VAR_TYPE_ID;
            default:
                throw new RuntimeException("Failed to find element type in factory for type <"+name+">");
        }
    }
}
