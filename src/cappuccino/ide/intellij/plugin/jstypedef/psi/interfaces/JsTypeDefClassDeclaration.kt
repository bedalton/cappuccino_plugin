package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.contributor.toJsFunctionType
import cappuccino.ide.intellij.plugin.contributor.toNamedPropertiesList
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes

interface JsTypeDefClassDeclaration : JsTypeDefElement, JsTypeDefHasNamespace {
    val typeName: JsTypeDefTypeName?
    val extendsStatement: JsTypeDefExtendsStatement
    val typeNameString: String
    val interfaceConstructorList: List<JsTypeDefInterfaceConstructor>
    val functionList: List<JsTypeDefFunction>
    val propertyList: List<JsTypeDefProperty>
    val isStatic:Boolean
    val className:String
}

fun JsTypeDefClassDeclaration.toJsClassDefinition() : JsClassDefinition {
    val extends = extendsStatement.typeList.toJsTypeDefTypeListTypes()
    return JsClassDefinition(
            className = className,
            extends = extends,
            static = isStatic,
            isObjJ = false,
            functions = functionList.filterNot {
                it.isStatic
            }.map { it.toJsFunctionType() }.toSet(),
            staticFunctions = functionList.filter {
                it.isStatic
            }.map { it.toJsFunctionType() }.toSet(),
            properties = this.propertyList.filter{it.staticKeyword == null}.toNamedPropertiesList().toSet().orEmpty(),
            staticProperties = this.propertyList.filter{it.staticKeyword != null}.toNamedPropertiesList().toSet().orEmpty(),
            isStruct = this is JsTypeDefInterfaceElement,
            enclosingNameSpaceComponents = enclosingNamespaceComponents
    )
}