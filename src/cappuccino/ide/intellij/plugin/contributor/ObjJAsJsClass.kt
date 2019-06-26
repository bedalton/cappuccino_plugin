package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsFunction
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType.JsTypeListBasicType as JsTypeListBasicType

data class JsClassDefinition (
        val className: String,
        val extends:List<JsTypeListType>,
        val enclosingNameSpaceComponents: List<String> = listOf(),
        val properties: Set<JsTypeDefNamedProperty> = setOf(),
        val functions: Set<JsFunction> = setOf(),
        val staticProperties: Set<JsTypeDefNamedProperty> = setOf(),
        val staticFunctions: Set<JsFunction> = setOf(),
        val isObjJ:Boolean = false,
        val isStruct:Boolean = true,
        val static:Boolean = false
)


internal fun objJClassAsJsClass(project: Project, className: String): JsClassDefinition? {
    if (DumbService.isDumb(project))
        return null
    val implementations = ObjJImplementationDeclarationsIndex.instance[className, project]
    val properties: MutableList<JsTypeDefNamedProperty> = mutableListOf()
    val extends = mutableListOf<String>()
    for (objClass in implementations) {
        //ProgressManager.checkCanceled()
        val superClassName = objClass.superClassName
        if (superClassName != null)
            extends.add(superClassName)
        extends.addAll(objClass.getInheritedProtocols())
        for (instanceVar in objClass.instanceVariableList?.instanceVariableDeclarationList ?: emptyList()) {
            val property = instanceVar.toJsProperty() ?: continue
            properties.add(property)
        }
    }
    val protocols = ObjJProtocolDeclarationsIndex.instance[className, project]
    for (objClass in protocols) {
        //ProgressManager.checkCanceled()
        extends.addAll(objClass.getInheritedProtocols())
        for (instanceVar in objClass.instanceVariableDeclarationList) {
            val property = instanceVar.toJsProperty() ?: continue
            properties.add(property)
        }
    }
    if (protocols.isEmpty() && implementations.isEmpty())
        return null
    return JsClassDefinition(
            className = className,
            properties = properties.toSet(),
            extends = extends.map { JsTypeListBasicType(it) },
            isObjJ = true,
            isStruct = false,
            static = false
    )
}

private fun ObjJInstanceVariableDeclaration.toJsProperty(): JsTypeDefNamedProperty? {
    val propertyName = this.variableName?.text ?: return null
    val formalVariableType = this.formalVariableType
    val type = if (this.formalVariableType.varTypeId?.className != null)
        formalVariableType.varTypeId!!.className!!.text
    else {
        val typeName = formalVariableType.text
        when {
            typeName.endsWith("Ref") -> typeName.substringFromEnd(0, -3)
            typeName.endsWith("Pointer") -> typeName.substringFromEnd(0, "Pointer".length)
            typeName.endsWith("Reference") -> typeName.substringFromEnd(0, "Reference".length)
            else -> typeName
        }
    }
    return JsTypeDefNamedProperty(
            name = propertyName,
            readonly = false,
            static = false,
            comment = null,
            types = listOf(type).toJsTypeList()
    )
}


fun List<String>.toJsTypeList() : JsTypesList {
    val types = this.map {
        JsTypeListBasicType(it)
    }
    return JsTypesList(types, true)
}

fun JsTypeDefFunction.toJsFunctionType() : JsFunction {
    return JsFunction(
            name = functionNameString,
            comment = null, // @todo implement comment parsing
            parameters = propertiesList?.propertyList?.toNamedPropertiesList() ?: emptyList(),
            returnType = functionReturnType?.toTypeListType() ?: EMPTY_TYPES_LIST
    )
}

fun List<JsTypeDefProperty>.toNamedPropertiesList() : List<JsTypeDefNamedProperty> {
    return map {
        it.toJsNamedProperty()
    }
}

fun JsTypeDefProperty.toJsNamedProperty() : JsTypeDefNamedProperty {
    return JsTypeDefNamedProperty(
            name = propertyNameString,
            comment = docComment?.commentText,
            static = this.staticKeyword != null,
            readonly = this.readonly != null,
            types = JsTypesList(typeList.toJsTypeDefTypeListTypes(), isNullable),
            default = null
    )
}