package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFile;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJIncludeFile;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJIncludeFramework;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportFrameworkStub;

public class ObjJStubTypes {
    public static final ObjJAccessorPropertyStubType ACCESSOR_PROPERTY = new ObjJAccessorPropertyStubType("ObjJ_ACCESSOR_PROPERTY");
    public static final ObjJFileStubType FILE = new ObjJFileStubType();
    public static final ObjJFunctionDeclarationStubType FUNCTION_DECLARATION = new ObjJFunctionDeclarationStubType("ObjJ_FUNCTION_DECLARATION");
    public static final ObjJFunctionLiteralStubType FUNCTION_LITERAL = new ObjJFunctionLiteralStubType("ObjJ_FUNCTION_LITERAL");
    public static final ObjJGlobalVariableDeclarationStubType GLOBAL_VARIABLE = new ObjJGlobalVariableDeclarationStubType("ObjJ_GLOBAL_VARIABLE_DECLARATION");
    public static final ObjJImplementationStubType IMPLEMENTATION = new ObjJImplementationStubType("ObjJ_IMPLEMENTATION");
    public static final ObjJImportFileStubType IMPORT_FILE = new ObjJImportFileStubType("ObjJ_IMPORT_FILE");
    public static final ObjJImportFrameworkStubType IMPORT_FRAMEWORK = new ObjJImportFrameworkStubType("ObjJ_IMPORT_FRAMEWORK");
    public static final ObjJIncludeFileStubType INCLUDE_FILE = new ObjJIncludeFileStubType("OBjJ_INCLUDE_FILE");
    public static final ObjJIncludeFrameworkStubType INCLUDE_FRAMEWORK = new ObjJIncludeFrameworkStubType("ObjJ_INCLUDE_FRAMEWORK");
    public static final ObjJInstanceVariableDeclarationStubType INSTANCE_VAR = new ObjJInstanceVariableDeclarationStubType("ObjJ_INSTANCE_VAR");
    public static final ObjJMethodCallStubType METHOD_CALL = new ObjJMethodCallStubType("ObjJ_METHOD_CALL");
    public static final ObjJMethodHeaderStubType METHOD_HEADER = new ObjJMethodHeaderStubType("ObjJ_METHOD_HEADER");
    public static final ObjJPreprocessorDefineFunctionStubType PREPROCESSOR_FUNCTION = new ObjJPreprocessorDefineFunctionStubType("ObjJ_PREPROCESSOR_DEFINE_FUNCTION");
    public static final ObjJProtocolStubType PROTOCOL = new ObjJProtocolStubType("ObjJ_PROTOCOL");
    public static final ObjJSelectorLiteralStubType SELECTOR_LITERAL = new ObjJSelectorLiteralStubType("ObjJ_SELETOR_LITERAL");
    public static final ObjJVarTypeIdStubType VAR_TYPE_ID = new ObjJVarTypeIdStubType("ObjJ_VAR_TYPE_ID");
}
