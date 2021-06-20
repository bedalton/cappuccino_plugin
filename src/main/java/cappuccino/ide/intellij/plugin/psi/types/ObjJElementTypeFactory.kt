package cappuccino.ide.intellij.plugin.psi.types

import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.ACCESSOR_PROPERTY
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.CLASS_NAME
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FUNCTION_DECLARATION
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FUNCTION_LITERAL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FUNCTION_CALL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.GLOBAL_VARIABLE
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPLEMENTATION
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPORT_FILE
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPORT_FRAMEWORK
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.INCLUDE_FILE
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.INCLUDE_FRAMEWORK
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.INSTANCE_VAR
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.METHOD_CALL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.METHOD_HEADER
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.OBJECT_LITERAL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.PREPROCESSOR_FUNCTION
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.PROPERTY_NAME
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.PROTOCOL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.QUALIFIED_REFERENCE
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.SELECTOR_LITERAL
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.TYPE_DEF
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.VARIABLE_DECLARATION
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.VARIABLE_NAME
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.VARIABLE_TYPE_ID
import com.intellij.psi.tree.IElementType

class ObjJElementTypeFactory {
    companion object {
        @JvmStatic
        fun factory(name: String): IElementType {
            return when (name) {
                "ObjJ_ACCESSOR_PROPERTY" -> ACCESSOR_PROPERTY
                "ObjJ_CLASS_NAME" -> CLASS_NAME
                "ObjJ_FUNCTION_DECLARATION" -> FUNCTION_DECLARATION
                "ObjJ_FUNCTION_LITERAL" -> FUNCTION_LITERAL
                "ObjJ_FUNCTION_CALL" -> FUNCTION_CALL
                "ObjJ_GLOBAL_VARIABLE_DECLARATION" -> GLOBAL_VARIABLE
                "ObjJ_IMPLEMENTATION_DECLARATION" -> IMPLEMENTATION
                "ObjJ_IMPORT_FILE" -> IMPORT_FILE
                "ObjJ_IMPORT_FRAMEWORK" -> IMPORT_FRAMEWORK
                "ObjJ_INCLUDE_FILE" -> INCLUDE_FILE
                "ObjJ_INCLUDE_FRAMEWORK" -> INCLUDE_FRAMEWORK
                "ObjJ_INSTANCE_VARIABLE_DECLARATION" -> INSTANCE_VAR
                "ObjJ_METHOD_CALL" -> METHOD_CALL
                "ObjJ_METHOD_HEADER" -> METHOD_HEADER
                "ObjJ_OBJECT_LITERAL" -> OBJECT_LITERAL
                "ObjJ_PREPROCESSOR_DEFINE_FUNCTION" -> PREPROCESSOR_FUNCTION
                "ObjJ_PROPERTY_NAME" -> PROPERTY_NAME
                "ObjJ_PROTOCOL_DECLARATION" -> PROTOCOL
                "ObjJ_QUALIFIED_REFERENCE" -> QUALIFIED_REFERENCE
                "ObjJ_SELECTOR_LITERAL" -> SELECTOR_LITERAL
                "ObjJ_TYPE_DEF" -> TYPE_DEF
                "ObjJ_VARIABLE_TYPE_ID" -> VARIABLE_TYPE_ID
                "ObjJ_VARIABLE_DECLARATION" -> VARIABLE_DECLARATION
                "ObjJ_VARIABLE_NAME" -> VARIABLE_NAME
                else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
            }
        }
    }
}
