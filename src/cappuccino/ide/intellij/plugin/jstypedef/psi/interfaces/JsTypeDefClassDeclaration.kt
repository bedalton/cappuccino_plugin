package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toNamedPropertiesList
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.psi.stubs.StubElement

interface JsTypeDefClassDeclaration<StubT:StubElement<*>> : JsTypeDefStubBasedElement<StubT>, JsTypeDefElement, JsTypeDefHasNamespace {
    val typeName: JsTypeDefTypeName?
    val genericTypeTypes: JsTypeDefGenericTypeTypes?
    val extendsStatement: JsTypeDefExtendsStatement?
    val interfaceConstructorList: List<JsTypeDefInterfaceConstructor>
    val functionList: List<JsTypeDefFunction>
    val propertyList: List<JsTypeDefProperty>
    val isStatic:Boolean
    val className:String
}

fun JsTypeDefClassDeclaration<*>.toJsClassDefinition() : JsClassDefinition {
    val extends = extendsStatement?.typeList.toJsTypeDefTypeListTypes()
    return JsClassDefinition(
            className = className,
            extends = extends,
            static = isStatic,
            isObjJ = false,
            functions = functionList.filterNot {
                it.isStatic && it.functionNameString.isNotNullOrBlank()
            }.map { it.toJsTypeListType() }.toSet(),
            staticFunctions = functionList.filter {
                it.isStatic && it.functionNameString.isNotNullOrBlank()
            }.map { it.toJsTypeListType() }.toSet(),
            properties = this.propertyList.filter{it.staticKeyword == null && it.propertyNameString.isNotNullOrBlank()}.toNamedPropertiesList().toSet(),
            staticProperties = this.propertyList.filter{it.staticKeyword != null && it.propertyNameString.isNotNullOrBlank()}.toNamedPropertiesList().toSet(),
            isStruct = this is JsTypeDefInterfaceElement,
            enclosingNameSpaceComponents = enclosingNamespaceComponents
    )
}