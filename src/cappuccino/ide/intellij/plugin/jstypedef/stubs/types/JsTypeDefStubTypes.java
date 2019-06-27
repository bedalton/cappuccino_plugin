package cappuccino.ide.intellij.plugin.jstypedef.stubs.types;

public interface JsTypeDefStubTypes {
    JsTypeDefFileStubType JS_FILE = new JsTypeDefFileStubType();
    JsTypeDefFunctionStubType JS_FUNCTION = new JsTypeDefFunctionStubType("JS_FUNCTION");
    JsTypeDefKeysListStubType JS_KEY_LIST = new JsTypeDefKeysListStubType("JS_KEY_LIST");
    JsTypeDefInterfaceStubType JS_INTERFACE = JsTypeDefInterfaceStubType()
    JsTypeDefModuleStubType JS_MODULE = new JsTypeDefModuleStubType("JS_MODULE");
    JsTypeDefModuleNameStubType JS_MODULE_NAME = new JsTypeDefModuleNameStubType("JS_MODULE_NAME");
    JsTypeDefPropertyStubType JS_PROPERTY = new JsTypeDefPropertyStubType("JS_PROPERTY");
    JsTypeDefTypeMapStubType JS_TYPE_MAP = new JsTypeDefTypeMapStubType("JS_TYPE_MAP");
    JsTypeDefVariableDeclarationStubType JS_VARIABLE_DECLARATION = new JsTypeDefVariableDeclarationStubType("JS_VARIABLE_DECLARATION");
}
