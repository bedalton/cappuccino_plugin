package org.cappuccino_project.ide.intellij.plugin.stubs.types

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFile
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJIncludeFile
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJIncludeFramework
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportFrameworkStub

object ObjJStubTypes {
    val ACCESSOR_PROPERTY = ObjJAccessorPropertyStubType("ObjJ_ACCESSOR_PROPERTY")
    val FILE = ObjJFileStubType()
    val FUNCTION_DECLARATION = ObjJFunctionDeclarationStubType("ObjJ_FUNCTION_DECLARATION")
    val FUNCTION_LITERAL = ObjJFunctionLiteralStubType("ObjJ_FUNCTION_LITERAL")
    val GLOBAL_VARIABLE = ObjJGlobalVariableDeclarationStubType("ObjJ_GLOBAL_VARIABLE_DECLARATION")
    val IMPLEMENTATION = ObjJImplementationStubType("ObjJ_IMPLEMENTATION")
    val IMPORT_FILE = ObjJImportFileStubType("ObjJ_IMPORT_FILE")
    val IMPORT_FRAMEWORK = ObjJImportFrameworkStubType("ObjJ_IMPORT_FRAMEWORK")
    val INCLUDE_FILE = ObjJIncludeFileStubType("OBjJ_INCLUDE_FILE")
    val INCLUDE_FRAMEWORK = ObjJIncludeFrameworkStubType("ObjJ_INCLUDE_FRAMEWORK")
    val INSTANCE_VAR = ObjJInstanceVariableDeclarationStubType("ObjJ_INSTANCE_VAR")
    val METHOD_CALL = ObjJMethodCallStubType("ObjJ_METHOD_CALL")
    val METHOD_HEADER = ObjJMethodHeaderStubType("ObjJ_METHOD_HEADER")
    val PREPROCESSOR_FUNCTION = ObjJPreprocessorDefineFunctionStubType("ObjJ_PREPROCESSOR_DEFINE_FUNCTION")
    val PROTOCOL = ObjJProtocolStubType("ObjJ_PROTOCOL")
    val SELECTOR_LITERAL = ObjJSelectorLiteralStubType("ObjJ_SELETOR_LITERAL")
    val VAR_TYPE_ID = ObjJVarTypeIdStubType("ObjJ_VAR_TYPE_ID")
    val VARIABLE_NAME = ObjJVariableNameStubType("ObjJ_VARIABLE_NAME")
}
