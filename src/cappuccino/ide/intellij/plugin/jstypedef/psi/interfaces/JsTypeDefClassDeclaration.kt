package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toNamedPropertiesList
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface JsTypeDefClassDeclaration<StubT:StubElement<*>> : JsTypeDefStubBasedElement<StubT>, JsTypeDefElement, JsTypeDefHasNamespace {
    val typeName: JsTypeDefTypeName?
    val extendsStatement: JsTypeDefExtendsStatement
    val interfaceConstructorList: List<JsTypeDefInterfaceConstructor>
    val functionList: List<JsTypeDefFunction>
    val propertyList: List<JsTypeDefProperty>
    val isStatic:Boolean
    val className:String
}

fun JsTypeDefClassDeclaration<*>.toJsClassDefinition() : JsClassDefinition {
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
            properties = this.propertyList.filter{it.staticKeyword == null}.toNamedPropertiesList().toSet(),
            staticProperties = this.propertyList.filter{it.staticKeyword != null}.toNamedPropertiesList().toSet(),
            isStruct = this is JsTypeDefInterfaceElement,
            enclosingNameSpaceComponents = enclosingNamespaceComponents
    )
}