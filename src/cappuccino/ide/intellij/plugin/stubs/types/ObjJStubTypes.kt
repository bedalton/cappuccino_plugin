package cappuccino.ide.intellij.plugin.stubs.types


object ObjJStubTypes {
    val ACCESSOR_PROPERTY = ObjJAccessorPropertyStubType("ObjJ_ACCESSOR_PROPERTY")
    val CLASS_NAME = ObjJClassNameStubType("ObjJ_CLASS_NAME")
    val FILE = ObjJFileStubType()
    val FUNCTION_DECLARATION = ObjJFunctionDeclarationStubType("ObjJ_FUNCTION_DECLARATION")
    val FUNCTION_LITERAL = ObjJFunctionLiteralStubType("ObjJ_FUNCTION_LITERAL")
    val GLOBAL_VARIABLE = ObjJGlobalVariableDeclarationStubType("ObjJ_GLOBAL_VARIABLE_DECLARATION")
    val IMPLEMENTATION = ObjJImplementationStubType("ObjJ_IMPLEMENTATION")
    val IMPORT_FILE = ObjJImportFileStubType("ObjJ_IMPORT_FILE")
    val IMPORT_FRAMEWORK = ObjJImportFrameworkStubType("ObjJ_IMPORT_FRAMEWORK")
    val INCLUDE_FILE = ObjJIncludeFileStubType("OBjJ_INCLUDE_FILE")
    val INCLUDE_FRAMEWORK = ObjJIncludeFrameworkStubType("ObjJ_INCLUDE_FRAMEWORK")
    val INSTANCE_VAR = ObjJInstanceVariableDeclarationStubType("ObjJ_INSTANCE_VARIABLE_DECLARATION")
    val METHOD_CALL = ObjJMethodCallStubType("ObjJ_METHOD_CALL")
    val METHOD_HEADER = ObjJMethodHeaderStubType("ObjJ_METHOD_HEADER")
    val OBJECT_LITERAL = ObjJObjectLiteralStubType("ObjJ_OBJECT_LITERAL")
    val PREPROCESSOR_FUNCTION = ObjJPreprocessorDefineFunctionStubType("ObjJ_PREPROCESSOR_DEFINE_FUNCTION")
    val PROTOCOL = ObjJProtocolStubType("ObjJ_PROTOCOL")
    val QUALIFIED_REFERENCE = ObjJQualifiedReferenceStubType("ObjJ_QUALIFIED_REFERENCE")
    val SELECTOR_LITERAL = ObjJSelectorLiteralStubType("ObjJ_SELETOR_LITERAL")
    val TYPE_DEF = ObjJTypeDefStubType("ObjJ_TYPE_DEF")
    val VAR_TYPE_ID = ObjJVarTypeIdStubType("ObjJ_VAR_TYPE_ID")
    val VARIABLE_DECLARATION = ObjJVariableDeclarationStubType("ObjJ_VARIABLE_DECLARATION")
    val VARIABLE_NAME = ObjJVariableNameStubType("ObjJ_VARIABLE_NAME")
}

internal const val TYPES_DELIM = "|"