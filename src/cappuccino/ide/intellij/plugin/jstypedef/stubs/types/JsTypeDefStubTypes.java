package cappuccino.ide.intellij.plugin.jstypedef.stubs.types;

public interface JsTypeDefStubTypes {
    JsTypeDefClassStubType JS_CLASS = new JsTypeDefClassStubType("JS_CLASS_ELEMENT");
    JsTypeDefFileStubType JS_FILE = new JsTypeDefFileStubType();
    JsTypeDefFunctionStubType JS_FUNCTION = new JsTypeDefFunctionStubType("JS_FUNCTION");
    JsTypeDefInterfaceStubType JS_INTERFACE = new JsTypeDefInterfaceStubType("JS_INTERFACE_ELEMENT");
    JsTypeDefKeysListStubType JS_KEY_LIST = new JsTypeDefKeysListStubType("JS_KEY_LIST");
    JsTypeDefModuleStubType JS_MODULE = new JsTypeDefModuleStubType("JS_MODULE");
    JsTypeDefModuleNameStubType JS_MODULE_NAME = new JsTypeDefModuleNameStubType("JS_MODULE_NAME");
    JsTypeDefPropertyStubType JS_PROPERTY = new JsTypeDefPropertyStubType("JS_PROPERTY");
    JsTypeDefTypeAliasStubType JS_TYPE_ALIAS = new JsTypeDefTypeAliasStubType("JS_TYPE_ALIAS");
    JsTypeDefTypeMapStubType JS_TYPE_MAP = new JsTypeDefTypeMapStubType("JS_TYPE_MAP");
    JsTypeDefVariableDeclarationStubType JS_VARIABLE_DECLARATION = new JsTypeDefVariableDeclarationStubType("JS_VARIABLE_DECLARATION");
}
